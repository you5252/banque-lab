package com.banquelab.batch.job;

import com.banquelab.batch.model.ImportedTransaction;
import com.banquelab.batch.model.TransactionCsvLine;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Job "importTransactionsJob" : CSV → validation → table imported_transaction.
 *
 * Traitement par "chunk" (paquet) : on lit 1000 lignes, on les traite, on les écrit
 * en une seule transaction, puis on recommence. Si le job plante à la ligne 650 000,
 * il peut redémarrer à partir du dernier paquet validé (tâche S5-5).
 *
 * Version volontairement mono-thread : tâche S5-3 = le rendre plus rapide.
 */
@Configuration
public class ImportTransactionsJobConfig {

    static final int CHUNK_SIZE = 1000;

    @Bean
    @StepScope
    public FlatFileItemReader<TransactionCsvLine> transactionReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        return new FlatFileItemReaderBuilder<TransactionCsvLine>()
                .name("transactionCsvReader")
                .resource(new FileSystemResource(inputFile))
                .linesToSkip(1) // en-tête
                .delimited()
                .names("transactionId", "accountIban", "amount", "currency", "type", "operationDate", "label")
                .fieldSetMapper(fs -> new TransactionCsvLine(
                        fs.readString("transactionId"),
                        fs.readString("accountIban"),
                        new BigDecimal(fs.readString("amount")),
                        fs.readString("currency"),
                        fs.readString("type"),
                        LocalDate.parse(fs.readString("operationDate")),
                        fs.readString("label")))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<ImportedTransaction> transactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<ImportedTransaction>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO imported_transaction
                            (transaction_id, account_iban, amount, currency, type, operation_date, label, suspicious, imported_at)
                        VALUES
                            (:transactionId, :accountIban, :amount, :currency, :type, :operationDate, :label, :suspicious, CURRENT_TIMESTAMP)
                        """)
                .itemSqlParameterSourceProvider(t -> new MapSqlParameterSource()
                        .addValue("transactionId", t.transactionId())
                        .addValue("accountIban", t.accountIban())
                        .addValue("amount", t.amount())
                        .addValue("currency", t.currency())
                        .addValue("type", t.type())
                        .addValue("operationDate", Date.valueOf(t.operationDate()))
                        .addValue("label", t.label())
                        .addValue("suspicious", t.suspicious()))
                .build();
    }

    @Bean
    public Step importTransactionsStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       FlatFileItemReader<TransactionCsvLine> transactionReader,
                                       TransactionProcessor processor,
                                       JdbcBatchItemWriter<ImportedTransaction> transactionWriter) {
        return new StepBuilder("importTransactionsStep", jobRepository)
                .<TransactionCsvLine, ImportedTransaction>chunk(CHUNK_SIZE, transactionManager)
                .reader(transactionReader)
                .processor(processor)
                .writer(transactionWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(NumberFormatException.class)
                .skip(IllegalArgumentException.class)
                .skipLimit(1000)
                .build();
    }

    @Bean
    public Job importTransactionsJob(JobRepository jobRepository,
                                     Step importTransactionsStep,
                                     JobDurationListener listener) {
        return new JobBuilder("importTransactionsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(importTransactionsStep)
                .build();
    }
}

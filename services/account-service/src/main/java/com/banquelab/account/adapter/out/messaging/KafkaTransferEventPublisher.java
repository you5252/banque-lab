package com.banquelab.account.adapter.out.messaging;

import com.banquelab.account.application.port.out.PublishTransferEventPort;
import com.banquelab.account.domain.model.TransferCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Adaptateur SORTANT Kafka (PRODUCER).
 * Clé du message = compte débiteur → tous les virements d'un même compte vont dans
 * la même partition, donc sont lus DANS L'ORDRE par les consumers.
 */
@Component
class KafkaTransferEventPublisher implements PublishTransferEventPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaTransferEventPublisher.class);

    private final KafkaTemplate<String, TransferEventMessage> kafkaTemplate;
    private final String topic;

    KafkaTransferEventPublisher(KafkaTemplate<String, TransferEventMessage> kafkaTemplate,
                                @Value("${banquelab.kafka.topics.transfers}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(TransferCompleted e) {
        var message = new TransferEventMessage(
                e.transferId(), e.from().value(), e.to().value(),
                e.amount().amount(), e.amount().currency(), e.occurredAt());

        kafkaTemplate.send(topic, e.from().value().toString(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Échec d'envoi Kafka du virement {}", e.transferId(), ex);
                    } else {
                        log.info("Virement {} publié (partition={}, offset={})", e.transferId(),
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }

    /** Contrat du message JSON (versionné dans le nom du topic : bank.transfers.v1). */
    public record TransferEventMessage(UUID transferId, UUID fromAccountId, UUID toAccountId,
                                BigDecimal amount, String currency, Instant occurredAt) {
    }
}

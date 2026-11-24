package com.banquelab.batch.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Affiche un bilan à la fin du job : durée, lignes lues/écrites/filtrées/rejetées. */
@Component
public class JobDurationListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobDurationListener.class);

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
        log.info("=== Job {} terminé : statut={} en {} s ===",
                jobExecution.getJobInstance().getJobName(), jobExecution.getStatus(), duration.toSeconds());
        for (StepExecution step : jobExecution.getStepExecutions()) {
            log.info("Step {} : lues={} écrites={} filtrées={} rejetées={} commits={}",
                    step.getStepName(), step.getReadCount(), step.getWriteCount(),
                    step.getFilterCount(), step.getSkipCount(), step.getCommitCount());
        }
    }
}

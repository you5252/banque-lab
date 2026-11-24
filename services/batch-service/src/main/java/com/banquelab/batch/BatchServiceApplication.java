package com.banquelab.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lancement : java -jar batch-service.jar inputFile=../../data/transactions.csv
 * Spring Boot démarre le job, transmet "inputFile" comme paramètre de job, puis s'arrête.
 */
@SpringBootApplication
public class BatchServiceApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(BatchServiceApplication.class, args)));
    }
}

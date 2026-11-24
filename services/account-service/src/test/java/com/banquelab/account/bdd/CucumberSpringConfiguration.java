package com.banquelab.account.bdd;

import com.banquelab.account.application.port.out.PublishTransferEventPort;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Démarre la vraie application (serveur HTTP sur un port aléatoire, base H2).
 * Kafka est remplacé par un "mock" : grâce à l'architecture hexagonale,
 * il suffit de remplacer UN port pour tester sans Kafka.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    @MockitoBean
    PublishTransferEventPort publishTransferEventPort;
}

package com.banquelab.account.config;

import com.banquelab.account.application.port.in.GetAccountQuery;
import com.banquelab.account.application.port.in.TransferMoneyUseCase;
import com.banquelab.account.application.port.out.LoadAccountPort;
import com.banquelab.account.application.port.out.PublishTransferEventPort;
import com.banquelab.account.application.port.out.SaveAccountPort;
import com.banquelab.account.application.service.GetAccountService;
import com.banquelab.account.application.service.TransferMoneyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * "Câblage" de l'hexagone : on branche les adaptateurs (JPA, Kafka) sur les ports
 * des cas d'usage. Les classes de l'application n'ont ainsi pas besoin de @Service.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    TransferMoneyUseCase transferMoneyUseCase(LoadAccountPort load,
                                              SaveAccountPort save,
                                              PublishTransferEventPort publish) {
        return new TransferMoneyService(load, save, publish);
    }

    @Bean
    GetAccountQuery getAccountQuery(LoadAccountPort load) {
        return new GetAccountService(load);
    }
}

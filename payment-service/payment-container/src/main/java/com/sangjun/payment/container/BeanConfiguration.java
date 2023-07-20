package com.sangjun.payment.container;

import com.sangjun.payment.domain.PaymentCancelDomainService;
import com.sangjun.payment.domain.PaymentInitDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public PaymentInitDomainService paymentInitDomainService() {
        return new PaymentInitDomainService();
    }

    @Bean
    public PaymentCancelDomainService paymentCancelDomainService() {
        return new PaymentCancelDomainService();
    }
}

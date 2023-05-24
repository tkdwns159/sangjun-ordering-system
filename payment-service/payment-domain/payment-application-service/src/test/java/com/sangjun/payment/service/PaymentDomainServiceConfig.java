package com.sangjun.payment.service;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.payment.domain.PaymentDomainService;
import com.sangjun.payment.domain.event.PaymentCompletedEvent;
import com.sangjun.payment.domain.event.PaymentFailedEvent;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentFailedMessagePublisher;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentDomainServiceConfig {

    @Bean
    public PaymentDomainService paymentDomainService() {
        return new PaymentDomainServiceImpl();
    }

    @Bean
    public DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventDomainEventPublisher() {
        return Mockito.mock(PaymentCompletedMessagePublisher.class);
    }

    @Bean
    DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher() {
        return Mockito.mock(PaymentFailedMessagePublisher.class);
    }

}

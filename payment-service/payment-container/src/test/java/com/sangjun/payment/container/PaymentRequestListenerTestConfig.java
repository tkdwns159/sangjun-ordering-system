package com.sangjun.payment.container;

import com.sangjun.payment.service.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentFailedMessagePublisher;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.sangjun.payment.dataaccess", "com.sangjun.common.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.payment.domain", "com.sangjun.common.dataaccess", "com.sangjun.common.domain"})
@ComponentScan(basePackages = {"com.sangjun.payment"},
        excludeFilters = {@ComponentScan.Filter(classes = {ComponentScan.class}),
                @ComponentScan.Filter(type = FilterType.REGEX,
                        pattern = "com.sangjun.payment.messaging.*")})
@SpringBootConfiguration
@EnableAutoConfiguration
public class PaymentRequestListenerTestConfig {
    @Bean
    public PaymentCompletedMessagePublisher paymentCompletedMessagePublisher() {
        return Mockito.mock(PaymentCompletedMessagePublisher.class);
    }

    @Bean
    public PaymentCancelledMessagePublisher paymentCancelledMessagePublisher() {
        return Mockito.mock(PaymentCancelledMessagePublisher.class);
    }

    @Bean
    public PaymentFailedMessagePublisher paymentFailedMessagePublisher() {
        return Mockito.mock(PaymentFailedMessagePublisher.class);
    }
}

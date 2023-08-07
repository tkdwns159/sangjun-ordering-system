package com.sangjun.restaurant.application.ports.input.message.listener;

import com.sangjun.restaurant.application.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.sangjun.restaurant.application.ports.output.message.publisher.OrderRejectedMessagePublisher;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.sangjun.restaurant.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.restaurant.domain", "com.sangjun.common.domain"})
@ComponentScan(basePackages = {"com.sangjun.restaurant"},
        excludeFilters = {@ComponentScan.Filter(classes = {ComponentScan.class}),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.sangjun.restaurant.messaging.*")})
@SpringBootConfiguration
@EnableAutoConfiguration
public class TestConfig {
    @Bean
    public OrderApprovedMessagePublisher orderApprovedMessagePublisher() {
        return Mockito.mock(OrderApprovedMessagePublisher.class);
    }

    @Bean
    public OrderRejectedMessagePublisher orderRejectedMessagePublisher() {
        return Mockito.mock(OrderRejectedMessagePublisher.class);
    }
}

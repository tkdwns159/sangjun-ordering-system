package com.sangjun.order.container;

import com.sangjun.order.domain.service.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.sangjun.order.domain.service.ports.output.message.publisher.restaurant.OrderPaidRestaurantRequestMessagePublisher;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.context.annotation.ComponentScan.Filter;

@EnableJpaRepositories(basePackages = {"com.sangjun.order.dataaccess", "com.sangjun.common.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.order.dataaccess", "com.sangjun.common.dataaccess"})
@ComponentScan(basePackages = {"com.sangjun.order", "com.sangjun.kafka"},
        excludeFilters = {@Filter(type = FilterType.ANNOTATION, classes = {ComponentScan.class})
        })
@SpringBootConfiguration
@EnableAutoConfiguration
public class OrderIntegrationTestConfig {
    @Bean
    public OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher() {
        return Mockito.mock(OrderCancelledPaymentRequestMessagePublisher.class);
    }

    @Bean
    public OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher() {
        return Mockito.mock(OrderPaidRestaurantRequestMessagePublisher.class);

    }
}

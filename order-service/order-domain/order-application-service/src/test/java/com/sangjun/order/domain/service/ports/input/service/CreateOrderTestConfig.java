package com.sangjun.order.domain.service.ports.input.service;


import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.OrderDomainServiceImpl;
import com.sangjun.order.domain.service.ports.input.message.listener.payment.PaymentResponseMessageListener;
import com.sangjun.order.domain.service.ports.input.message.listener.restaurant.RestaurantApprovalMessageListener;
import com.sangjun.order.domain.service.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.sangjun.order.domain.service.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.sangjun.order.domain.service.ports.output.message.publisher.restaurant.OrderPaidRestaurantRequestMessagePublisher;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.sangjun.order.dataaccess", "com.sangjun.common.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.order.domain", "com.sangjun.common.domain"})
@ComponentScan(basePackages = {"com.sangjun.order"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {ComponentScan.class}),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.sangjun\\.order\\.messaging.*")
        })
@SpringBootConfiguration
@EnableAutoConfiguration
public class CreateOrderTestConfig {

    @Bean
    public OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher() {
        return Mockito.mock(OrderCreatedPaymentRequestMessagePublisher.class);
    }

    @Bean
    public OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher() {
        return Mockito.mock(OrderCancelledPaymentRequestMessagePublisher.class);
    }

    @Bean
    public OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher() {
        return Mockito.mock(OrderPaidRestaurantRequestMessagePublisher.class);
    }

    @Bean
    public PaymentResponseMessageListener paymentResponseMessageListener() {
        return Mockito.mock(PaymentResponseMessageListener.class);
    }

    @Bean
    public RestaurantApprovalMessageListener restaurantApprovalMessageListener() {
        return Mockito.mock(RestaurantApprovalMessageListener.class);
    }

    @Bean
    public RestaurantRepository restaurantRepository() {
        return Mockito.mock(RestaurantRepository.class);
    }

    @Bean
    public CustomerRepository customerRepository() {
        return Mockito.mock(CustomerRepository.class);
    }


    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }
}

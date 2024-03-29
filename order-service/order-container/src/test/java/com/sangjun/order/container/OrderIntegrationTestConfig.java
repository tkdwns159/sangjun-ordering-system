package com.sangjun.order.container;

import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.ProductRepository;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.context.annotation.ComponentScan.Filter;

@EnableJpaRepositories(basePackages = {"com.sangjun.order.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.order.domain"})
@ComponentScan(basePackages = {"com.sangjun.order", "com.sangjun.kafka"},
        excludeFilters = {@Filter(type = FilterType.ANNOTATION, classes = {ComponentScan.class})
        })
@SpringBootConfiguration
@EnableAutoConfiguration
public class OrderIntegrationTestConfig {

    @Bean
    public ProductRepository productRepository() {
        return Mockito.mock(ProductRepository.class);
    }

    @Bean
    public CustomerRepository customerRepository() {
        return Mockito.mock(CustomerRepository.class);
    }

}

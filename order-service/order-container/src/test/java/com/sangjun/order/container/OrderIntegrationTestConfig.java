package com.sangjun.order.container;

import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import com.sangjun.order.domain.service.ports.output.service.RestaurantService;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.context.annotation.ComponentScan.Filter;

@EnableJpaRepositories(basePackages = {"com.sangjun.order.dataaccess", "com.sangjun.common.dataaccess" })
@EntityScan(basePackages = {"com.sangjun.order.domain", "com.sangjun.common.domain" })
@ComponentScan(basePackages = {"com.sangjun.order", "com.sangjun.kafka" },
        excludeFilters = {@Filter(type = FilterType.ANNOTATION, classes = {ComponentScan.class})
        })
@SpringBootConfiguration
@EnableAutoConfiguration
public class OrderIntegrationTestConfig {

    @Bean
    public RestaurantRepository restaurantRepository() {
        return Mockito.mock(RestaurantRepository.class);
    }

    @Bean
    public CustomerRepository customerRepository() {
        return Mockito.mock(CustomerRepository.class);
    }

    @Bean
    public RestaurantService restaurantService() {
        return Mockito.mock(RestaurantService.class);
    }

}

package com.sangjun.restaurant.container;

import com.sangjun.restaurant.application.RestaurantDomainServiceImpl;
import com.sangjun.restaurant.domain.RestaurantDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RestaurantDomainService restaurantDomainService() {
        return new RestaurantDomainServiceImpl();
    }
}

package com.sangjun.restaurant.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.sangjun.restaurant.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.restaurant.domain", "com.sangjun.common.domain"})
@SpringBootApplication(scanBasePackages = "com.sangjun")
public class RestaurantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}

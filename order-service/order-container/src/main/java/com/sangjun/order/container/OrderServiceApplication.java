package com.sangjun.order.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableJpaRepositories(basePackages = {"com.sangjun.order.dataaccess", "com.sangjun.common.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.order.dataaccess", "com.sangjun.common.dataaccess"})
@SpringBootApplication(scanBasePackages = "com.sangjun")
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}

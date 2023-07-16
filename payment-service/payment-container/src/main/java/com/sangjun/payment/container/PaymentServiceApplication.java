package com.sangjun.payment.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"com.sangjun.payment.domain", "com.sangjun.common.domain"})
@EnableJpaRepositories(basePackages = {"com.sangjun.payment.dataaccess", "com.sangjun.common.dataaccess"})
@SpringBootApplication(scanBasePackages = {"com.sangjun"})
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}

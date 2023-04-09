package com.sangjun.payment.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@EnableJpaRepositories(basePackages = "com.sangjun.payment.dataaccess")
@EntityScan(basePackages = "com.sangjun.payment.dataaccess")
@SpringBootApplication(scanBasePackages = "com.sangjun")
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}

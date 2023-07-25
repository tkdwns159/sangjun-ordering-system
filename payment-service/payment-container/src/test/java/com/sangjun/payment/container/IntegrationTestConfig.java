package com.sangjun.payment.container;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableJpaRepositories(basePackages = {"com.sangjun.payment.dataaccess", "com.sangjun.common.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.payment.domain", "com.sangjun.common.dataaccess", "com.sangjun.common.domain"})
@ComponentScan(basePackages = {"com.sangjun.payment", "com.sangjun.kafka"},
        excludeFilters = @ComponentScan.Filter(classes = {ComponentScan.class}))
@SpringBootConfiguration
@EnableAutoConfiguration
public class IntegrationTestConfig {
}

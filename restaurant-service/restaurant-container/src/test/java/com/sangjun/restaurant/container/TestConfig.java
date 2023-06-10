package com.sangjun.restaurant.container;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.sangjun.restaurant.dataaccess", "com.sangjun.common.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.restaurant.dataaccess", "com.sangjun.common.dataaccess"})
@ComponentScan(basePackages = {"com.sangjun.restaurant", "com.sangjun.kafka"},
        excludeFilters = {@ComponentScan.Filter(classes = {ComponentScan.class})})
@SpringBootConfiguration
@EnableAutoConfiguration
public class TestConfig {
}

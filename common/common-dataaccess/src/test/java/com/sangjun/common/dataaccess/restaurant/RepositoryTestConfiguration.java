package com.sangjun.common.dataaccess.restaurant;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.sangjun.common.dataaccess"})
@EntityScan(basePackages = {"com.sangjun.common.dataaccess"})
@SpringBootApplication(scanBasePackages = {"com.sangjun.common.dataacces"})
public class RepositoryTestConfiguration {
}

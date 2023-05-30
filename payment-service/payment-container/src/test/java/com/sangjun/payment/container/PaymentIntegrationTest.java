package com.sangjun.payment.container;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@EmbeddedKafka(
        count = 3,
        partitions = 3,
        bootstrapServersProperty = "kafka-config.bootstrap-servers",
        zookeeperPort = 2182,
        zkSessionTimeout = 1000,
        zkConnectionTimeout = 1000
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.Random.class)
@Transactional
@SpringBootTest(classes = TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PaymentIntegrationTest {

    @Test
    void contextLoads() {

    }
}

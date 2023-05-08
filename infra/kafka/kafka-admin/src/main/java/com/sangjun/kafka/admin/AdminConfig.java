package com.sangjun.kafka.admin;

import com.sangjun.kafka.config.data.KafkaConfigData;
import com.sangjun.kafka.config.data.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AdminConfig {

    private final KafkaConfigData kafkaConfigData;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
        return new KafkaAdmin(props);
    }

    @Bean
    public KafkaAdmin.NewTopics newTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder
                        .name(KafkaTopics.PAYMENT_REQUEST_TOPIC.name)
                        .partitions(3)
                        .replicas(3)
                        .build(),
                TopicBuilder
                        .name(KafkaTopics.PAYMENT_RESPONSE_TOPIC.name)
                        .partitions(3)
                        .replicas(3)
                        .build(),
                TopicBuilder
                        .name(KafkaTopics.RESTAURANT_APPROVAL_REQUEST_TOPIC.name)
                        .partitions(3)
                        .replicas(3)
                        .build(),
                TopicBuilder
                        .name(KafkaTopics.RESTAURANT_APPROVAL_RESPONSE_TOPIC.name)
                        .partitions(3)
                        .replicas(3)
                        .build());
    }

}

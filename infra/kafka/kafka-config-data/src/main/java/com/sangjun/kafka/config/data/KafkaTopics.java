package com.sangjun.kafka.config.data;

import com.sangjun.kafka.order.avro.model.PaymentRequestAvroModel;
import com.sangjun.kafka.order.avro.model.PaymentResponseAvroModel;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum KafkaTopics {
    PAYMENT_REQUEST_TOPIC("payment-request", PaymentRequestAvroModel.SCHEMA$),
    PAYMENT_RESPONSE_TOPIC("payment-response", PaymentResponseAvroModel.SCHEMA$),
    RESTAURANT_APPROVAL_REQUEST_TOPIC("restaurant-approval-request", RestaurantApprovalRequestAvroModel.SCHEMA$),
    RESTAURANT_APPROVAL_RESPONSE_TOPIC("restaurant-approval-response", RestaurantApprovalResponseAvroModel.SCHEMA$);

    private static final Logger log = LoggerFactory.getLogger(KafkaTopics.class);
    public final String name;
    public final Schema schema$;

    KafkaTopics(String name, Schema schema$) {
        this.name = name;
        this.schema$ = schema$;
    }

    public static KafkaTopics findByName(String name) {
        for (KafkaTopics topic : KafkaTopics.values()) {
            if (topic.name.equals(name)) {
                return topic;
            }
        }

        log.error("Invalid topic name: {}", name);
        throw new IllegalArgumentException("Invalid topic name: " + name);
    }
}

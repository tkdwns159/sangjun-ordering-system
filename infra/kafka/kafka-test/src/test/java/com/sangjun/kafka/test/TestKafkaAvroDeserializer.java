package com.sangjun.kafka.test;

import com.sangjun.kafka.config.data.KafkaTopics;
import com.sangjun.kafka.order.avro.model.PaymentRequestAvroModel;
import com.sangjun.kafka.order.avro.model.PaymentResponseAvroModel;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import java.io.IOException;
import java.util.Map;

public class TestKafkaAvroDeserializer extends KafkaAvroDeserializer {

    private static final String SUBJECT_VALUE_SUFFIX = "-value";

    public TestKafkaAvroDeserializer() {
        this.schemaRegistry = new MockSchemaRegistryClient();
        setCustomSchema();
    }

    public TestKafkaAvroDeserializer(SchemaRegistryClient client) {
        super(new MockSchemaRegistryClient());
        setCustomSchema();
    }

    public TestKafkaAvroDeserializer(SchemaRegistryClient client, Map<String, ?> props) {
        super(new MockSchemaRegistryClient(), props);
        setCustomSchema();
    }

    private void setCustomSchema() {
        try {
            this.schemaRegistry.register(KafkaTopics.PAYMENT_REQUEST_TOPIC.name + SUBJECT_VALUE_SUFFIX, PaymentRequestAvroModel.SCHEMA$);
            this.schemaRegistry.register(KafkaTopics.PAYMENT_RESPONSE_TOPIC.name + SUBJECT_VALUE_SUFFIX, PaymentResponseAvroModel.SCHEMA$);
            this.schemaRegistry.register(KafkaTopics.RESTAURANT_APPROVAL_REQUEST_TOPIC.name + SUBJECT_VALUE_SUFFIX, RestaurantApprovalRequestAvroModel.SCHEMA$);
            this.schemaRegistry.register(KafkaTopics.RESTAURANT_APPROVAL_RESPONSE_TOPIC.name + SUBJECT_VALUE_SUFFIX, RestaurantApprovalResponseAvroModel.SCHEMA$);
        } catch (IOException | RestClientException e) {
            throw new RuntimeException(e);
        }
    }
}

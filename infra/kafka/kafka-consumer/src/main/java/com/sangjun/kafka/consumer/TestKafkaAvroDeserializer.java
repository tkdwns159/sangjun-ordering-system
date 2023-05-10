package com.sangjun.kafka.consumer;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import java.util.Map;

public class TestKafkaAvroDeserializer extends KafkaAvroDeserializer {

    private static final String SUBJECT_VALUE_SUFFIX = "-value";

    public TestKafkaAvroDeserializer() {
        this.schemaRegistry = new TestMockSchemaRegistryClient();
    }

    public TestKafkaAvroDeserializer(SchemaRegistryClient client) {
        super(new TestMockSchemaRegistryClient());
    }

    public TestKafkaAvroDeserializer(SchemaRegistryClient client, Map<String, ?> props) {
        super(new TestMockSchemaRegistryClient(), props);
    }
}

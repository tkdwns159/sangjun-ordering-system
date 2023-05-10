package com.sangjun.kafka.consumer;

import com.sangjun.kafka.config.data.KafkaTopics;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

import java.io.IOException;

public class TestMockSchemaRegistryClient extends MockSchemaRegistryClient {
    @Override
    public ParsedSchema getSchemaBySubjectAndId(String subject, int id) throws IOException, RestClientException {
        KafkaTopics topic = KafkaTopics.findByName(subject.substring(0, subject.length() - 6));
        return new AvroSchema(topic.schema$);
    }
}

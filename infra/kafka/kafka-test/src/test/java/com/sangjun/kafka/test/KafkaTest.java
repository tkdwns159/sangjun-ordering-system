package com.sangjun.kafka.test;

import com.sangjun.kafka.config.data.KafkaTopics;
import com.sangjun.kafka.order.avro.model.PaymentOrderStatus;
import com.sangjun.kafka.order.avro.model.PaymentRequestAvroModel;
import com.sangjun.kafka.producer.service.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@Slf4j
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@EmbeddedKafka(
        count = 3,
        partitions = 3,
        bootstrapServersProperty = "kafka-config.bootstrap-servers",
        zookeeperPort = 2182,
        zkSessionTimeout = 3000,
        zkConnectionTimeout = 3000
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = KafkaTestConfig.class)
public class KafkaTest {

    @Autowired
    KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;

    @Autowired
    ConsumerFactory<String, PaymentRequestAvroModel> cf;

    @Autowired
    KafkaTemplate<String, PaymentRequestAvroModel> kafkaTemplate;

    @Value("${kafka-consumer-config.payment-consumer-group-id}")
    String consumerGroupId;

    @Test
    void contextLoads() {
    }

    @Test
    void 메세지가_전송된다() throws InterruptedException {
        //given
        String requestId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        BigDecimal price = new BigDecimal("1000.00");
        String sagaId = UUID.randomUUID().toString();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(requestId)
                .setCustomerId(customerId)
                .setOrderId(orderId)
                .setPrice(price)
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .setSagaId(sagaId)
                .setCreatedAt(now)
                .build();

        BlockingQueue<SendResult<String, PaymentRequestAvroModel>> mq = new LinkedBlockingQueue<>();

        //when
        kafkaProducer.send(KafkaTopics.PAYMENT_REQUEST_TOPIC.name, orderId, msg, new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                throw new RuntimeException(ex);
            }

            @Override
            public void onSuccess(SendResult<String, PaymentRequestAvroModel> result) {
                mq.add(result);
            }
        });

        //then
        SendResult<String, PaymentRequestAvroModel> result = mq.poll(1, TimeUnit.SECONDS);
        String key = result.getProducerRecord().key();
        PaymentRequestAvroModel value = result.getProducerRecord().value();

        assertEquals(orderId, key);
        assertEquals(requestId, value.getId());
        assertEquals(customerId, value.getCustomerId());
        assertEquals(orderId, value.getOrderId());
        assertEquals(price, value.getPrice());
        assertEquals(sagaId, value.getSagaId());
        assertEquals(now, value.getCreatedAt());
        assertEquals(PaymentOrderStatus.PENDING, value.getPaymentOrderStatus());
    }

    @Test
    void 메세지가_수신된다() {
        //given
        Consumer<String, PaymentRequestAvroModel> consumer = cf.createConsumer(consumerGroupId, "1");
        consumer.subscribe(List.of(KafkaTopics.PAYMENT_REQUEST_TOPIC.name));

        String requestId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        BigDecimal price = new BigDecimal("1000.00");
        String sagaId = UUID.randomUUID().toString();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(requestId)
                .setCustomerId(customerId)
                .setOrderId(orderId)
                .setPrice(price)
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .setSagaId(sagaId)
                .setCreatedAt(now)
                .build();

        kafkaTemplate.send(KafkaTopics.PAYMENT_REQUEST_TOPIC.name, orderId, msg);

        //when
        ConsumerRecord<String, PaymentRequestAvroModel> reply = KafkaTestUtils.getSingleRecord(consumer, KafkaTopics.PAYMENT_REQUEST_TOPIC.name);

        //then
        assertEquals(orderId, reply.key());
        assertEquals(requestId, reply.value().getId());
        assertEquals(customerId, reply.value().getCustomerId());
        assertEquals(orderId, reply.value().getOrderId());
        assertEquals(price, reply.value().getPrice());
        assertEquals(sagaId, reply.value().getSagaId());
        assertEquals(now, reply.value().getCreatedAt());
        assertEquals(PaymentOrderStatus.PENDING, reply.value().getPaymentOrderStatus());
    }


}

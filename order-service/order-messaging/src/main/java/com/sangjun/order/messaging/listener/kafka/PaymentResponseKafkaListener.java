package com.sangjun.order.messaging.listener.kafka;

import com.sangjun.common.domain.valueobject.PaymentStatus;
import com.sangjun.kafka.consumer.KafkaConsumer;
import com.sangjun.kafka.order.avro.model.PaymentResponseAvroModel;
import com.sangjun.order.domain.service.ports.input.message.listener.payment.PaymentResponseMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.sangjun.order.messaging.mapper.OrderMessageMapper.MAPPER;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseAvroModel> {

    private final PaymentResponseMessageListener paymentResponseMessageListener;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
            topics = "${order-service.payment-response-topic-name}")
    public void receive(@Payload List<PaymentResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of payment responses received with keys: {}, partitions: {}, and offsets: {} ",
                messages.size(),
                keys.toString(),
                partitions.toArray(),
                offsets.toString());

        processOnCompletedPayments(messages);
        processOnFailedPayments(messages);
    }

    private void processOnCompletedPayments(List<PaymentResponseAvroModel> messages) {
        messages.stream()
                .map(MAPPER::toPaymentResponse)
                .filter(message -> message.getPaymentStatus() == PaymentStatus.COMPLETED)
                .forEach(message -> {
                    log.info("Processing successful payment for order id: {}", message.getOrderId());
                    paymentResponseMessageListener.paymentCompleted(message);
                });
    }

    private void processOnFailedPayments(List<PaymentResponseAvroModel> messages) {
        messages.stream()
                .map(MAPPER::toPaymentResponse)
                .filter(message -> message.getPaymentStatus() == PaymentStatus.FAILED
                        || message.getPaymentStatus() == PaymentStatus.CANCELLED)
                .forEach(message -> {
                    log.info("Processing unsuccessful payment for order id: {}", message.getOrderId());
                    paymentResponseMessageListener.paymentCancelled(message);
                });
    }
}

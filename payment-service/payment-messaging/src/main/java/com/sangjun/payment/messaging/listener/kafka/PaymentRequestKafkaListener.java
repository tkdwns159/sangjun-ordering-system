package com.sangjun.payment.messaging.listener.kafka;

import com.sangjun.kafka.consumer.KafkaConsumer;
import com.sangjun.kafka.order.avro.model.PaymentOrderStatus;
import com.sangjun.kafka.order.avro.model.PaymentRequestAvroModel;
import com.sangjun.payment.messaging.mapper.PaymentMessagingDataMapper;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.ports.input.message.listener.PaymentRequestMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestKafkaListener implements KafkaConsumer<PaymentRequestAvroModel> {
    private final PaymentRequestMessageListener paymentRequestMessageListener;
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
            topics = "${payment-service.payment-request-topic-name}")
    public void receive(@Payload List<PaymentRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of payment requests received with keys: {}, partitions: {}, and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        completePayments(messages);
        cancelPayments(messages);
    }


    private void completePayments(List<PaymentRequestAvroModel> messages) {
        messages.stream()
                .filter(message -> message.getPaymentOrderStatus() == PaymentOrderStatus.PENDING)
                .forEach(this::completePayment);
    }

    private void completePayment(PaymentRequestAvroModel message) {
        log.info("Processing payment for order id: {}", message.getOrderId());
        try {
            PaymentRequest pr = paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(message);
            paymentRequestMessageListener.completePayment(pr);
        } catch (RuntimeException ex) {
            log.error("Error processing payment for order id: {}", message.getOrderId(), ex);
        }
    }

    private void cancelPayments(List<PaymentRequestAvroModel> messages) {
        messages.stream()
                .filter(message -> message.getPaymentOrderStatus() == PaymentOrderStatus.CANCELLED)
                .forEach(message -> {
                    log.info("Cancelling payment for order id: {}", message.getOrderId());
                    paymentRequestMessageListener.cancelPayment(
                            paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(message)
                    );
                });
    }
}

package com.sangjun.payment.messaging.publisher.kafka;

import com.sangjun.kafka.order.avro.model.PaymentResponseAvroModel;
import com.sangjun.kafka.producer.KafkaMessageHelper;
import com.sangjun.kafka.producer.service.KafkaProducer;
import com.sangjun.payment.domain.event.PaymentFailedEvent;
import com.sangjun.payment.messaging.mapper.PaymentMessageMapper;
import com.sangjun.payment.service.config.PaymentServiceConfigData;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentFailedMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFailedKafkaMessagePublisher implements PaymentFailedMessagePublisher {

    private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
    private final PaymentServiceConfigData paymentServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    @Override
    public void publish(PaymentFailedEvent domainEvent) {
        String orderId = domainEvent.getPayment().getOrderId().getValue().toString();
        log.info("Received PaymentFailedEvent for order id : {}", orderId);

        try {
            PaymentResponseAvroModel paymentResponseAvroModel =
                    PaymentMessageMapper.MAPPER.toPaymentResponseAvroModel(domainEvent);
            kafkaProducer.send(
                    paymentServiceConfigData.getPaymentResponseTopicName(),
                    orderId,
                    paymentResponseAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            paymentServiceConfigData.getPaymentResponseTopicName(),
                            paymentResponseAvroModel,
                            orderId,
                            "PaymentResponseAvroModel"
                    )
            );

            log.info("PaymentResponseAvroModel sent to kafka for order id : {}", orderId);
        } catch (Exception e) {
            log.error("Error while sending PaymentResponseAvroModel message to kafka" +
                    " with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}

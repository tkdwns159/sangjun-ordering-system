package com.sangjun.restaurant.messaging.publisher.kafka;

import com.sangjun.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.sangjun.kafka.producer.KafkaMessageHelper;
import com.sangjun.kafka.producer.service.KafkaProducer;
import com.sangjun.restaurant.application.config.RestaurantServiceConfigData;
import com.sangjun.restaurant.application.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.sangjun.restaurant.domain.event.OrderApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.sangjun.restaurant.messaging.mapper.RestaurantMessageMapper.MAPPER;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovedKafkaMessagePublisher implements OrderApprovedMessagePublisher {

    private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer;

    private final RestaurantServiceConfigData restaurantServiceConfigData;

    private final KafkaMessageHelper kafkaMessageHelper;


    @Override
    public void publish(OrderApprovedEvent domainEvent) {
        String orderId = domainEvent.getPendingOrder().getOrderId().toString();

        log.info("Received OrderApprovedEvent for order id :{}", orderId);

        try {
            var responseAvroModel = MAPPER.toRestaurantApprovalResponseAvroModel(domainEvent);

            kafkaProducer.send(
                    restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
                    orderId,
                    responseAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
                            responseAvroModel,
                            orderId,
                            "RestaurantApprovalResponseAvroModel"
                    )
            );

            log.info("RestaurantApprovalResponseAvroModel sent to kafka at: {}", System.nanoTime());
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalResponseAvroModel message " +
                    "to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}

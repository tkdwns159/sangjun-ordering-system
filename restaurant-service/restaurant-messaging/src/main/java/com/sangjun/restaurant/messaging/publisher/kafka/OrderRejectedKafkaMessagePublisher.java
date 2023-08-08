package com.sangjun.restaurant.messaging.publisher.kafka;

import com.sangjun.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.sangjun.kafka.producer.KafkaMessageHelper;
import com.sangjun.kafka.producer.service.KafkaProducer;
import com.sangjun.restaurant.application.config.RestaurantServiceConfigData;
import com.sangjun.restaurant.application.ports.output.message.publisher.OrderRejectedMessagePublisher;
import com.sangjun.restaurant.domain.event.OrderRejectedEvent;
import com.sangjun.restaurant.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRejectedKafkaMessagePublisher implements OrderRejectedMessagePublisher {
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
    private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer;
    private final RestaurantServiceConfigData restaurantServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;


    @Override
    public void publish(OrderRejectedEvent orderRejectedEvent) {
        String orderId = orderRejectedEvent.getOrderApproval().getOrderId().getValue().toString();

        log.info("Received OrderRejectedEvent for order id: {}", orderId);

        try {
            RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel =
                    restaurantMessagingDataMapper
                            .orderRejectedEventToRestaurantApprovalResponseAvroModel(orderRejectedEvent);

            kafkaProducer.send(restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
                    orderId,
                    restaurantApprovalResponseAvroModel,
                    kafkaMessageHelper.getKafkaCallback(restaurantServiceConfigData
                                    .getRestaurantApprovalResponseTopicName(),
                            restaurantApprovalResponseAvroModel,
                            orderId,
                            "RestaurantApprovalResponseAvroModel"));

            log.info("RestaurantApprovalResponseAvroModel sent to kafka at: {}", System.nanoTime());
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalResponseAvroModel message" +
                    " to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}

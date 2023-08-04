package com.sangjun.order.messaging.listener.kafka;

import com.sangjun.kafka.consumer.KafkaConsumer;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.sangjun.order.domain.service.ports.input.message.listener.restaurant.RestaurantApprovalMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.sangjun.common.domain.valueobject.OrderApprovalStatus.APPROVED;
import static com.sangjun.common.domain.valueobject.OrderApprovalStatus.REJECTED;
import static com.sangjun.common.utils.CommonConstants.FAILURE_MESSAGE_DELIMITER;
import static com.sangjun.order.messaging.mapper.OrderMessageMapper.MAPPER;


@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<RestaurantApprovalResponseAvroModel> {
    private final RestaurantApprovalMessageListener restaurantApprovalMessageListener;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}", topics = "${order-service.restaurant-approval-response-topic-name}")
    public void receive(@Payload List<RestaurantApprovalResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of restaurant approval responses received with keys: {}, partitions: {}, and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        processOnAcceptedRestaurantApprovals(messages);
        processOnRejectedRestaurantApprovals(messages);
    }

    private void processOnAcceptedRestaurantApprovals(List<RestaurantApprovalResponseAvroModel> messages) {
        messages.stream()
                .map(MAPPER::toRestaurantApprovalResponse)
                .filter(message -> message.getOrderApprovalStatus() == APPROVED)
                .forEach(message -> {
                    log.info("Processing approved order for order id: {}",
                            message.getOrderId());

                    restaurantApprovalMessageListener.orderApproved(message);
                });
    }

    private void processOnRejectedRestaurantApprovals(List<RestaurantApprovalResponseAvroModel> messages) {
        messages.stream()
                .map(MAPPER::toRestaurantApprovalResponse)
                .filter(message -> message.getOrderApprovalStatus() == REJECTED)
                .forEach(message -> {
                    log.info("Processing rejected order for order id: {} with failure messages: {}",
                            message.getOrderId(), String.join(FAILURE_MESSAGE_DELIMITER, message.getFailureMessages()));

                    restaurantApprovalMessageListener.orderRejected(message);
                });
    }
}

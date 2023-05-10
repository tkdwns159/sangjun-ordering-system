package com.sangjun.order.messaging.listener.kafka;

import com.sangjun.kafka.consumer.KafkaConsumer;
import com.sangjun.kafka.order.avro.model.OrderApprovalStatus;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.sangjun.order.domain.service.ports.input.message.listener.restaurant.RestaurantApprovalMessageListener;
import com.sangjun.order.messaging.mapper.OrderMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.sangjun.common.utils.CommonConstants.FAILURE_MESSAGE_DELIMITER;


@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<RestaurantApprovalResponseAvroModel> {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
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

        messages.forEach(restaurantApprovalResponseAvroModel -> {
            if (OrderApprovalStatus.APPROVED == restaurantApprovalResponseAvroModel
                    .getOrderApprovalStatus()) {
                log.info("Processing approved order for order id: {}",
                        restaurantApprovalResponseAvroModel.getOrderId());

                restaurantApprovalMessageListener.orderApproved(orderMessagingDataMapper
                        .restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
                                restaurantApprovalResponseAvroModel));

            } else if (OrderApprovalStatus.REJECTED == restaurantApprovalResponseAvroModel
                    .getOrderApprovalStatus()) {
                log.info("Processing rejected order for order id: {} with failure messages: {}",
                        restaurantApprovalResponseAvroModel.getOrderId(),
                        String.join(FAILURE_MESSAGE_DELIMITER,
                                restaurantApprovalResponseAvroModel
                                        .getFailureMessages()));

                restaurantApprovalMessageListener.orderRejected(orderMessagingDataMapper
                        .restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
                                restaurantApprovalResponseAvroModel));
            }
        });

    }
}

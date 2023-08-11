package com.sangjun.restaurant.messaging.listener.kafka;

import com.sangjun.kafka.consumer.KafkaConsumer;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.sangjun.kafka.order.avro.model.RestaurantOrderStatus;
import com.sangjun.restaurant.application.ports.input.message.listener.OrderApprovalRequestMessageListener;
import com.sangjun.restaurant.application.ports.input.message.listener.PendingOrderCancelRequestMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.sangjun.restaurant.messaging.mapper.RestaurantMessageMapper.MAPPER;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalRequestKafkaListener implements KafkaConsumer<RestaurantApprovalRequestAvroModel> {
    private final OrderApprovalRequestMessageListener orderApprovalRequestMessageListener;
    private final PendingOrderCancelRequestMessageListener pendingOrderCancelRequestMessageListener;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
            topics = "${restaurant-service.restaurant-approval-request-topic-name}")
    public void receive(@Payload List<RestaurantApprovalRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of orders approval requests received with keys {}, partitions {} and offsets {}" +
                        ", sending for restaurant approval",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        registerPendingOrders(messages);
        cancelPendingOrders(messages);
    }

    private void registerPendingOrders(List<RestaurantApprovalRequestAvroModel> messages) {
        messages.stream()
                .filter(message -> message.getRestaurantOrderStatus() == RestaurantOrderStatus.PAID)
                .forEach(message -> {
                    log.info("Processing order approval for order id: {}", message.getOrderId());
                    orderApprovalRequestMessageListener.registerPendingOrder(MAPPER.toRestaurantApprovalRequest(message));
                });
    }

    private void cancelPendingOrders(List<RestaurantApprovalRequestAvroModel> messages) {
        messages.stream()
                .filter(message -> message.getRestaurantOrderStatus() == RestaurantOrderStatus.CANCELLED)
                .forEach(message -> {
                    log.info("Cancelling order approval for order id: {}", message.getOrderId());
                    pendingOrderCancelRequestMessageListener.cancelPendingOrder(MAPPER.toPendingOrderCancelRequest(message));
                });
    }


}

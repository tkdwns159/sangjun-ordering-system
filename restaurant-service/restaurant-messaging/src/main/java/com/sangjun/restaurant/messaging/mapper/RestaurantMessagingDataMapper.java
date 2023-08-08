package com.sangjun.restaurant.messaging.mapper;

import com.sangjun.kafka.order.avro.model.OrderApprovalStatus;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.sangjun.restaurant.domain.event.OrderApprovedEvent;
import com.sangjun.restaurant.domain.event.OrderRejectedEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RestaurantMessagingDataMapper {
    public RestaurantApprovalResponseAvroModel
    orderApprovedEventToRestaurantApprovalResponseAvroModel(OrderApprovedEvent orderApprovedEvent) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setOrderId(orderApprovedEvent.getOrderApproval().getOrderId().getValue().toString())
                .setRestaurantId(orderApprovedEvent.getRestaurantId().getValue().toString())
                .setCreatedAt(orderApprovedEvent.getCreatedAt().toInstant())
                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(orderApprovedEvent.
                        getOrderApproval().getOrderApprovalStatus().name()))
                .setFailureMessages(orderApprovedEvent.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponseAvroModel orderRejectedEventToRestaurantApprovalResponseAvroModel(OrderRejectedEvent orderRejectedEvent) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setOrderId(orderRejectedEvent.getOrderApproval().getOrderId().getValue().toString())
                .setRestaurantId(orderRejectedEvent.getRestaurantId().getValue().toString())
                .setCreatedAt(orderRejectedEvent.getCreatedAt().toInstant())
                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(orderRejectedEvent.
                        getOrderApproval().getOrderApprovalStatus().name()))
                .setFailureMessages(orderRejectedEvent.getFailureMessages())
                .build();
    }
}
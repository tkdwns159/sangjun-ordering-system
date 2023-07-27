package com.sangjun.order.messaging.mapper;

import com.sangjun.common.domain.valueobject.OrderApprovalStatus;
import com.sangjun.common.domain.valueobject.PaymentStatus;
import com.sangjun.kafka.order.avro.model.*;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderCancelledEvent;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.service.dto.message.PaymentResponse;
import com.sangjun.order.domain.service.dto.message.RestaurantApprovalResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class OrderMessagingDataMapper {

    public PaymentRequestAvroModel orderCreatedEventToPaymentRequestAvroModel(OrderCreatedEvent orderCreatedEvent) {
        Order order = orderCreatedEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setCustomerId(order.getCustomerId().getValue().toString())
                .setOrderId(order.getId().getValue().toString())
                .setCreatedAt(orderCreatedEvent.getCreatedAt().toInstant())
                .setPrice(order.getPrice().getAmount())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();
    }

    public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(
            OrderCancelledEvent orderCancelledEvent) {
        Order order = orderCancelledEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setCustomerId(order.getCustomerId().getValue().toString())
                .setOrderId(order.getId().getValue().toString())
                .setCreatedAt(orderCancelledEvent.getCreatedAt().toInstant())
                .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
                .setPrice(orderCancelledEvent.getOrder().getPrice().getAmount())
                .build();
    }

    public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantApprovalRequestAvroModel(
            OrderPaidEvent orderPaidEvent) {
        Order order = orderPaidEvent.getOrder();
        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(order.getId().getValue().toString())
                .setSagaId("")
                .setRestaurantId(order.getRestaurantId().getValue().toString())
                .setPrice(order.getPrice().getAmount())
                .setCreatedAt(orderPaidEvent.getCreatedAt().toInstant())
                .setProducts(order.getItems().stream()
                        .map(item -> Product.newBuilder()
                                .setId(item.getProductId().getValue().toString())
                                .setQuantity(item.getQuantity())
                                .build())
                        .toList())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();
    }

    public PaymentResponse paymentResponseAvroModelToPaymentResponse(
            PaymentResponseAvroModel paymentResponseAvroModel) {
        return PaymentResponse.builder()
                .id(paymentResponseAvroModel.getId())
                .sagaId(paymentResponseAvroModel.getSagaId())
                .orderId(paymentResponseAvroModel.getOrderId())
                .paymentId(paymentResponseAvroModel.getPaymentId())
                .customerId(paymentResponseAvroModel.getCustomerId())
                .paymentStatus(PaymentStatus
                        .valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
                .createdAt(paymentResponseAvroModel.getCreatedAt())
                .failureMessages(paymentResponseAvroModel.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponse restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
            RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
        return RestaurantApprovalResponse.builder()
                .id(restaurantApprovalResponseAvroModel.getId())
                .sagaId(restaurantApprovalResponseAvroModel.getSagaId())
                .orderId(restaurantApprovalResponseAvroModel.getOrderId())
                .restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
                .orderApprovalStatus(OrderApprovalStatus.valueOf(
                        restaurantApprovalResponseAvroModel.getOrderApprovalStatus().name()))
                .failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
                .createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
                .build();
    }
}

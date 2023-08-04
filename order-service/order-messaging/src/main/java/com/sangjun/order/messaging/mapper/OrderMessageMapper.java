package com.sangjun.order.messaging.mapper;

import com.sangjun.common.domain.mapper.CentralConfig;
import com.sangjun.common.domain.mapper.CommonMapper;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.kafka.order.avro.model.*;
import com.sangjun.order.domain.event.OrderEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.service.dto.message.PaymentResponse;
import com.sangjun.order.domain.service.dto.message.RestaurantApprovalResponse;
import com.sangjun.order.domain.valueobject.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(config = CentralConfig.class, uses = CommonMapper.class)
public interface OrderMessageMapper {
    OrderMessageMapper MAPPER = Mappers.getMapper(OrderMessageMapper.class);

    default UUID toUUID(ProductId productId) {
        return productId.getValue();
    }

    @Mapping(target = "id", source = "productId")
    Product toProduct(OrderItem orderItem);


    default RestaurantOrderStatus toRestaurantOrderStatus(OrderStatus orderStatus) {
        if (orderStatus == OrderStatus.CANCELLING) {
            return RestaurantOrderStatus.CANCELLED;
        }

        return RestaurantOrderStatus.PAID;
    }

    @Mapping(target = ".", source = "order")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "products", source = "order.items")
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "sagaId", constant = "")
    @Mapping(target = "restaurantOrderStatus", source = "order.orderStatus")
    RestaurantApprovalRequestAvroModel toRestaurantApprovalRequestAvroModel(OrderPaidEvent orderPaidEvent);

    default PaymentOrderStatus toPaymentOrderStatus(OrderStatus orderStatus) {
        if (orderStatus == OrderStatus.CANCELLING) {
            return PaymentOrderStatus.CANCELLED;
        }

        return PaymentOrderStatus.PENDING;
    }

    @Mapping(target = ".", source = "order")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "sagaId", constant = "")
    @Mapping(target = "paymentOrderStatus", source = "order.orderStatus")
    PaymentRequestAvroModel toPaymentRequestAvroModel(OrderEvent orderEvent);


    PaymentResponse toPaymentResponse(PaymentResponseAvroModel paymentResponseAvroModel);

    RestaurantApprovalResponse toRestaurantApprovalResponse(RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel);

}

package com.sangjun.restaurant.messaging.mapper;

import com.sangjun.common.domain.mapper.CentralConfig;
import com.sangjun.common.domain.mapper.CommonMapper;
import com.sangjun.common.domain.valueobject.RestaurantOrderStatus;
import com.sangjun.kafka.order.avro.model.OrderApprovalStatus;
import com.sangjun.kafka.order.avro.model.Product;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.sangjun.restaurant.application.dto.PendingOrderCancelRequest;
import com.sangjun.restaurant.application.dto.ProductDto;
import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import com.sangjun.restaurant.domain.event.OrderApprovalEvent;
import com.sangjun.restaurant.domain.valueobject.PendingOrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(config = CentralConfig.class, uses = CommonMapper.class)
public interface RestaurantMessageMapper {
    RestaurantMessageMapper MAPPER = Mappers.getMapper(RestaurantMessageMapper.class);

    default RestaurantOrderStatus toRestaurantOrderStatus(com.sangjun.kafka.order.avro.model.RestaurantOrderStatus restaurantOrderStatus) {
        return RestaurantOrderStatus.PAID;
    }

    @Mapping(target = "productId", source = "id")
    ProductDto toProductDto(Product product);

    RestaurantApprovalRequest toRestaurantApprovalRequest(RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel);

    PendingOrderCancelRequest toPendingOrderCancelRequest(RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel);

    default OrderApprovalStatus toOrderApprovalStatus(PendingOrderStatus pendingOrderStatus) {
        return switch (pendingOrderStatus) {
            case APPROVED -> OrderApprovalStatus.APPROVED;
            default -> OrderApprovalStatus.REJECTED;
        };
    }

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = ".", source = "pendingOrder")
    @Mapping(target = "orderApprovalStatus", source = "pendingOrder.status")
    @Mapping(target = "sagaId", constant = "")
    @Mapping(target = "failureMessages", expression = "java(java.util.Collections.emptyList())")
    RestaurantApprovalResponseAvroModel toRestaurantApprovalResponseAvroModel(OrderApprovalEvent orderApprovalEvent);
}

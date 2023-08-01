package com.sangjun.order.messaging.mapper;

import com.sangjun.common.domain.mapper.CentralConfig;
import com.sangjun.common.domain.mapper.CommonMapper;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.kafka.order.avro.model.Product;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.sangjun.order.domain.event.OrderPaidEvent;
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

    @Mapping(target = ".", source = "order")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "products", source = "order.items")
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "sagaId", constant = "")
    @Mapping(target = "restaurantOrderStatus", expression = "java(com.sangjun.kafka.order.avro.model.RestaurantOrderStatus.PAID)")
    RestaurantApprovalRequestAvroModel toRestaurantApprovalRequestAvroModel(OrderPaidEvent orderPaidEvent);
}

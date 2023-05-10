package com.sangjun.order.domain.service.mapper;

import com.sangjun.common.utils.CentralConfig;
import com.sangjun.common.utils.CommonMapstructMapper;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.create.OrderAddress;
import com.sangjun.order.domain.service.dto.create.OrderItem;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.valueobject.StreetAddress;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(config = CentralConfig.class, uses = CommonMapstructMapper.class)
public interface OrderMapstructMapper {

    OrderMapstructMapper MAPPER = Mappers.getMapper(OrderMapstructMapper.class);

    TrackingId toTrackingId(UUID value);

    @Mapping(target = "id", source = "restaurantId")
    Restaurant toRestaurant(CreateOrderCommand createOrderCommand);

    default Product toProduct(com.sangjun.order.domain.entity.OrderItem orderItem) {
        return orderItem.getProduct();
    }

    @Mapping(target = "id", source = "restaurantId")
    @Mapping(target = "products", source = "items")
    Restaurant toRestaurant(Order order);

    @Mapping(target = "id", source = "productId")
    Product toProduct(OrderItem orderItem);

    @Mapping(target = "product.id", source = "productId")
    com.sangjun.order.domain.entity.OrderItem toOrderItem(OrderItem orderItem);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    StreetAddress toStreetAddress(OrderAddress orderAddress);

    @Mapping(target = "deliveryAddress", source = "orderAddress")
    Order toOrder(CreateOrderCommand createOrderCommand);


    @Mapping(target = "orderTrackingId", source = "trackingId.value")
    @Mapping(target = "message", constant = "Order created successfully")
    CreateOrderResponse toCreateOrderResponse(Order order);

    @Mapping(target = "orderTrackingId", source = "trackingId.value")
    TrackOrderResponse toTrackOrderResponse(Order order);
}

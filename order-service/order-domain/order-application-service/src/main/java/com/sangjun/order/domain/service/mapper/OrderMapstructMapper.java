package com.sangjun.order.domain.service.mapper;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.common.domain.valueobject.RestaurantId;
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

import java.math.BigDecimal;
import java.util.UUID;

@Mapper
public interface OrderMapstructMapper {

    OrderMapstructMapper MAPPER = Mappers.getMapper(OrderMapstructMapper.class);

    @Mapping(target = "value", source = "id")
    RestaurantId toRestaurantId(UUID id);

    @Mapping(target = "value", source = "id")
    ProductId toProductId(UUID id);

    @Mapping(target = "value", source = "id")
    CustomerId toCustomerId(UUID id);

    @Mapping(target = "value", source = "id")
    TrackingId toTrackingId(UUID id);

    @Mapping(target = "id", source = "restaurantId")
    Restaurant toRestaurant(CreateOrderCommand createOrderCommand);

    default Product toProduct(com.sangjun.order.domain.entity.OrderItem orderItem) {
        return orderItem.getProduct();
    }

    @Mapping(target = "id", source = "restaurantId")
    @Mapping(target = "products", source = "items")
    Restaurant toRestaurant(Order order);

    @Mapping(target = ".", source = "amount")
    Money toMoney(BigDecimal amount);

    @Mapping(target = "id", source = "productId")
    Product toProduct(OrderItem orderItem);

    @Mapping(target = "product.id", source = "productId")
    com.sangjun.order.domain.entity.OrderItem toOrderItem(OrderItem orderItem);

    StreetAddress toStreetAddress(OrderAddress orderAddress);

    @Mapping(target = "deliveryAddress", source = "orderAddress")
    Order toOrder(CreateOrderCommand createOrderCommand);


    @Mapping(target = "orderTrackingId", source = "trackingId.value")
    CreateOrderResponse toCreateOrderResponse(Order order);

    @Mapping(target = "orderTrackingId", source = "trackingId.value")
    TrackOrderResponse toTrackOrderResponse(Order order);
}
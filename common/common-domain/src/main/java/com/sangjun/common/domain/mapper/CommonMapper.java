package com.sangjun.common.domain.mapper;

import com.sangjun.common.domain.valueobject.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(config = CentralConfig.class)
public interface CommonMapper {

    CommonMapper MAPPER = Mappers.getMapper(CommonMapper.class);

    OrderId toOrderId(UUID value);

    RestaurantId toRestaurantId(UUID value);

    ProductId toProductId(UUID value);

    CustomerId toCustomerId(UUID value);

    @Mapping(target = ".", source = "amount")
    Money toMoney(BigDecimal amount);

    default BigDecimal toBigDecimal(Money price) {
        return price.getAmount();
    }

    default UUID toUUID(OrderId orderId) {
        return orderId.getValue();
    }

    default UUID toUUID(CustomerId customerId) {
        return customerId.getValue();
    }

    default UUID toUUID(RestaurantId restaurantId) {
        return restaurantId.getValue();
    }
}

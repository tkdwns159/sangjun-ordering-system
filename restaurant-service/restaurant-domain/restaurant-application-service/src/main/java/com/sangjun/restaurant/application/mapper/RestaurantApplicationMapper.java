package com.sangjun.restaurant.application.mapper;

import com.sangjun.common.domain.mapper.CentralConfig;
import com.sangjun.common.domain.mapper.CommonMapper;
import com.sangjun.common.domain.valueobject.RestaurantOrderStatus;
import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import com.sangjun.restaurant.domain.entity.PendingOrder;
import com.sangjun.restaurant.domain.valueobject.PendingOrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(config = CentralConfig.class, uses = CommonMapper.class)
public interface RestaurantApplicationMapper {
    RestaurantApplicationMapper MAPPER = Mappers.getMapper(RestaurantApplicationMapper.class);

    default PendingOrderStatus toPendingOrderStatus(RestaurantOrderStatus restaurantOrderStatus) {
        return PendingOrderStatus.PENDING;
    }

    @Mapping(target = "status", source = "restaurantOrderStatus")
    PendingOrder toPendingOrder(RestaurantApprovalRequest restaurantApprovalRequest);
}

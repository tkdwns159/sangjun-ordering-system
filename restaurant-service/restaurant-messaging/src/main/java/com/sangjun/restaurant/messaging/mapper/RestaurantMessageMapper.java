package com.sangjun.restaurant.messaging.mapper;

import com.sangjun.common.domain.mapper.CentralConfig;
import com.sangjun.common.domain.mapper.CommonMapper;
import com.sangjun.common.domain.valueobject.RestaurantOrderStatus;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(config = CentralConfig.class, uses = CommonMapper.class)
public interface RestaurantMessageMapper {
    RestaurantMessageMapper MAPPER = Mappers.getMapper(RestaurantMessageMapper.class);

    default RestaurantOrderStatus toRestaurantOrderStatus(com.sangjun.kafka.order.avro.model.RestaurantOrderStatus restaurantOrderStatus) {
        return RestaurantOrderStatus.PAID;
    }

    RestaurantApprovalRequest toRestaurantApprovalRequest(RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel);
}

package com.sangjun.order.dataaccess.order.mapper;

import com.sangjun.common.domain.mapper.CentralConfig;
import com.sangjun.common.domain.mapper.CommonMapper;
import com.sangjun.common.utils.CommonConstants;
import com.sangjun.order.dataaccess.order.entity.OrderEntity;
import com.sangjun.order.dataaccess.order.entity.OrderItemEntity;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.mapper.OrderMapstructMapper;
import com.sangjun.order.domain.valueobject.OrderItem;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Mapper(config = CentralConfig.class,
        uses = {CommonMapper.class, OrderMapstructMapper.class},
        imports = {CommonConstants.class, Collections.class, Optional.class, Arrays.class},
        builder = @Builder(disableBuilder = true))
@DecoratedWith(OrderDataMapperDecorator.class)
public interface OrderDataMapstructMapper {

    OrderDataMapstructMapper MAPPER = Mappers.getMapper(OrderDataMapstructMapper.class);

    default UUID toUUID(TrackingId trackingId) {
        return trackingId.getValue();
    }


    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "id", source = "id.orderItemId")
    OrderItemEntity toOrderItemEntity(OrderItem orderItem);

    @InheritInverseConfiguration
    OrderItem toOrderItem(OrderItemEntity orderItemEntity);

    @Mapping(target = "address", source = "deliveryAddress")
    @Mapping(target = "failureMessages",
            expression = "java(String.join(CommonConstants.FAILURE_MESSAGE_DELIMITER," +
                    " Optional.ofNullable(order.getFailureMessages()).orElse(Collections.emptyList())))")
    OrderEntity toOrderEntity(Order order);


//    @InheritInverseConfiguration
//    @Mapping(target = "failureMessages",
//            expression = "java(Optional.ofNullable(orderEntity.getFailureMessages())" +
//                    ".map(s -> s.split(CommonConstants.FAILURE_MESSAGE_DELIMITER))" +
//                    ".map(Arrays::asList)" +
//                    ".orElse(Collections.emptyList()))")
//    Order toOrder(OrderEntity orderEntity);
}

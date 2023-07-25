package com.sangjun.payment.messaging.mapper;

import com.sangjun.common.domain.mapper.CentralConfig;
import com.sangjun.common.domain.mapper.CommonMapper;
import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.kafka.order.avro.model.PaymentResponseAvroModel;
import com.sangjun.payment.domain.event.PaymentCompletedEvent;
import com.sangjun.payment.domain.valueobject.payment.PaymentId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.ZonedDateTime;

@Mapper(config = CentralConfig.class, uses = CommonMapper.class)
public interface PaymentMessageMapper {
    PaymentMessageMapper MAPPER = Mappers.getMapper(PaymentMessageMapper.class);

    default String toString(PaymentId paymentId) {
        return paymentId.getValue().toString();
    }

    default String toString(CustomerId customerId) {
        return customerId.getValue().toString();
    }

    default String toString(RestaurantId restaurantId) {
        return restaurantId.getValue().toString();
    }

    default String toString(OrderId orderId) {
        return orderId.getValue().toString();
    }

    default Instant toInstant(ZonedDateTime time) {
        return time.toInstant();
    }


    @Mapping(target = ".", source = "payment")
    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "sagaId", constant = "")
    @Mapping(target = "createdAt", source = "createdAt")
    PaymentResponseAvroModel toPaymentResponseAvroModel(PaymentCompletedEvent paymentCompletedEvent);
}

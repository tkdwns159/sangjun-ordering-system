package com.sangjun.payment.messaging.mapper;

import com.sangjun.common.domain.mapper.CentralConfig;
import com.sangjun.common.domain.mapper.CommonMapper;
import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.kafka.order.avro.model.PaymentRequestAvroModel;
import com.sangjun.kafka.order.avro.model.PaymentResponseAvroModel;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.valueobject.payment.PaymentId;
import com.sangjun.payment.service.dto.PaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

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


    @Mapping(target = ".", source = "payment")
    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "sagaId", constant = "")
    PaymentResponseAvroModel toPaymentResponseAvroModel(PaymentEvent paymentEvent);

    @Mapping(target = "paymentStatus", source = "paymentOrderStatus")
    PaymentRequest toPaymentRequest(PaymentRequestAvroModel paymentRequestAvroModel);
}

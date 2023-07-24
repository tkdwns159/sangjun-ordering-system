package com.sangjun.payment.service.ports.input.message.listener;

import com.sangjun.common.domain.mapper.CentralConfig;
import com.sangjun.common.domain.mapper.CommonMapper;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.service.dto.PaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(config = CentralConfig.class, uses = CommonMapper.class)
public interface PaymentRequestMapper {
    PaymentRequestMapper MAPPER = Mappers.getMapper(PaymentRequestMapper.class);

    Payment toPayment(PaymentRequest paymentRequest);
}

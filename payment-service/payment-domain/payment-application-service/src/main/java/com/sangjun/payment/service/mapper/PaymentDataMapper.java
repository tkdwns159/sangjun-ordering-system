package com.sangjun.payment.service.mapper;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.service.dto.PaymentRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentDataMapper {
    public Payment paymentRequestToPayment(PaymentRequest paymentRequest) {
        return Payment.builder(
                        new OrderId(UUID.fromString(paymentRequest.getOrderId())),
                        new CustomerId(UUID.fromString(paymentRequest.getCustomerId())),
                        new Money(paymentRequest.getPrice()))
                .build();
    }
}

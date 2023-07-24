package com.sangjun.payment.domain.exception;

import com.sangjun.common.domain.exception.DomainException;

import java.util.UUID;

public class PaymentNotFoundException extends DomainException {
    public PaymentNotFoundException(UUID orderId) {
        super(String.format("payment for orderId(%s)", orderId.toString()));
    }

}

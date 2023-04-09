package com.sangjun.payment.domain.exception;

import com.sangjun.common.domain.exception.DomainException;

public class PaymentNotFoundException extends DomainException {
    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

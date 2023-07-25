package com.sangjun.payment.domain.exception;

public class IllegalPaymentStateException extends RuntimeException {
    public IllegalPaymentStateException(String msg) {
        super(msg);
    }
}

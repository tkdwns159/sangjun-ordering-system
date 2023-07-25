package com.sangjun.payment.domain.ex;

public class IllegalPaymentStateException extends RuntimeException {
    public IllegalPaymentStateException(String msg) {
        super(msg);
    }
}

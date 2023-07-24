package com.sangjun.payment.domain.event;

import com.sangjun.payment.domain.entity.payment.Payment;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class PaymentFailedEvent extends PaymentEvent {

    public PaymentFailedEvent(Payment payment, ZonedDateTime createdAt) {
        super(payment, createdAt, new ArrayList<>());
    }
}

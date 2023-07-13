package com.sangjun.payment.domain.event;

import com.sangjun.common.domain.event.DomainEvent;
import com.sangjun.payment.domain.entity.payment.Payment;

import java.time.ZonedDateTime;
import java.util.List;

public abstract class PaymentEvent implements DomainEvent<Payment> {
    private final Payment payment;
    private final ZonedDateTime createdAt;
    private final List<String> failureMessages;

    public PaymentEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMessages) {
        this.payment = payment;
        this.createdAt = createdAt;
        this.failureMessages = failureMessages;
    }

    public Payment getPayment() {
        return payment;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }
}

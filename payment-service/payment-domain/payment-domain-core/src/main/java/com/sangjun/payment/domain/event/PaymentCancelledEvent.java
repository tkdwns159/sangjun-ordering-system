package com.sangjun.payment.domain.event;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.payment.domain.entity.Payment;

import java.time.ZonedDateTime;
import java.util.Collections;

public class PaymentCancelledEvent extends PaymentEvent {
    private final DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher;

    public PaymentCancelledEvent(Payment payment, ZonedDateTime createdAt, DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher) {
        super(payment, createdAt, Collections.emptyList());
        this.paymentCancelledEventDomainEventPublisher = paymentCancelledEventDomainEventPublisher;
    }

    @Override
    public void fire() {
        paymentCancelledEventDomainEventPublisher.publish(this);
    }
}

package com.sangjun.payment.service;

import com.sangjun.payment.domain.event.PaymentCancelledEvent;
import com.sangjun.payment.domain.event.PaymentCompletedEvent;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.event.PaymentFailedEvent;
import com.sangjun.payment.domain.exception.PaymentDomainException;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentFailedMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventShooter {

    private final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher;
    private final PaymentFailedMessagePublisher paymentFailedMessagePublisher;
    private final PaymentCancelledMessagePublisher paymentCancelledMessagePublisher;


    public void fire(PaymentEvent event) {
        if (event instanceof PaymentCompletedEvent) {
            paymentCompletedMessagePublisher.publish((PaymentCompletedEvent) event);
        } else if (event instanceof PaymentCancelledEvent) {
            paymentCancelledMessagePublisher.publish((PaymentCancelledEvent) event);
        } else if (event instanceof PaymentFailedEvent) {
            paymentFailedMessagePublisher.publish((PaymentFailedEvent) event);
        } else {
            log.error("Invalid PaymentEvent: {}", event.getClass().toString());
            throw new PaymentDomainException("Invalid OrderEvent: " + event.getClass());
        }

    }

}

package com.sangjun.payment.service;

import com.sangjun.payment.domain.event.PaymentCompletedEvent;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.exception.PaymentDomainException;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventShooter {

    private final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher;


    public void fire(PaymentEvent event) {
        if (event instanceof PaymentCompletedEvent) {
            paymentCompletedMessagePublisher.publish((PaymentCompletedEvent) event);
        } else {
            log.error("Invalid PaymentEvent: {}", event.getClass().toString());
            throw new PaymentDomainException("Invalid OrderEvent: " + event.getClass());
        }

    }

}

package com.sangjun.payment.service.ports.output.message.publisher;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.payment.domain.event.PaymentCompletedEvent;

public interface PaymentCompletedMessagePublisher extends DomainEventPublisher<PaymentCompletedEvent> {
}



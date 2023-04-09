package com.sangjun.payment.service.ports.output.message.publisher;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.payment.domain.event.PaymentFailedEvent;

public interface PaymentFailedMessagePublisher extends DomainEventPublisher<PaymentFailedEvent> {


}

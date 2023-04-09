package com.sangjun.payment.service.ports.output.message.publisher;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.payment.domain.event.PaymentCancelledEvent;
import com.sangjun.payment.domain.event.PaymentEvent;

public interface PaymentCancelledMessagePublisher extends DomainEventPublisher<PaymentCancelledEvent> {


}

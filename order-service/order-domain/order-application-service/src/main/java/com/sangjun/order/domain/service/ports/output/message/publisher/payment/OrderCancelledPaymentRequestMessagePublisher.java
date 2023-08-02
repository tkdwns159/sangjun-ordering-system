package com.sangjun.order.domain.service.ports.output.message.publisher.payment;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.order.domain.event.OrderCancellingEvent;

public interface OrderCancelledPaymentRequestMessagePublisher extends DomainEventPublisher<OrderCancellingEvent> {
}

package com.sangjun.restaurant.application.ports.output.message.publisher;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.restaurant.domain.event.OrderApprovedEvent;

public interface OrderApprovedMessagePublisher extends DomainEventPublisher<OrderApprovedEvent> {
}

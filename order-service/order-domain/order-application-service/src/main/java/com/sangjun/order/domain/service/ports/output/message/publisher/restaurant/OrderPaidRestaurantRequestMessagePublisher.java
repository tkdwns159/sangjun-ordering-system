package com.sangjun.order.domain.service.ports.output.message.publisher.restaurant;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.order.domain.event.OrderPaidEvent;

public interface OrderPaidRestaurantRequestMessagePublisher extends DomainEventPublisher<OrderPaidEvent> {
}

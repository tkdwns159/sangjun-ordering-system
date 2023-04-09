package com.sangjun.restaurant.domain;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.restaurant.domain.entity.Restaurant;
import com.sangjun.restaurant.domain.event.OrderApprovalEvent;
import com.sangjun.restaurant.domain.event.OrderApprovedEvent;
import com.sangjun.restaurant.domain.event.OrderRejectedEvent;

import java.util.List;

public interface RestaurantDomainService {
    OrderApprovalEvent validateOrder(Restaurant restaurant,
                                     List<String> failureMessages,
                                     DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher,
                                     DomainEventPublisher<OrderRejectedEvent> orderRejectedEventDomainEventPublisher);
    
}

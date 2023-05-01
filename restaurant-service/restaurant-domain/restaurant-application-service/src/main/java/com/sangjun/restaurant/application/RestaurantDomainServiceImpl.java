package com.sangjun.restaurant.application;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.common.domain.valueobject.OrderApprovalStatus;
import com.sangjun.restaurant.domain.RestaurantDomainService;
import com.sangjun.restaurant.domain.entity.Restaurant;
import com.sangjun.restaurant.domain.event.OrderApprovalEvent;
import com.sangjun.restaurant.domain.event.OrderApprovedEvent;
import com.sangjun.restaurant.domain.event.OrderRejectedEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;

@Slf4j
public class RestaurantDomainServiceImpl implements RestaurantDomainService {
    @Override
    public OrderApprovalEvent validateOrder(Restaurant restaurant, List<String> failureMessages, DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher, DomainEventPublisher<OrderRejectedEvent> orderRejectedEventDomainEventPublisher) {
        log.info("Validating order with id: {}", restaurant.getOrderDetail().getId().getValue());
        restaurant.validateOrder(failureMessages);

        if (!failureMessages.isEmpty()) {
            return getOrderRejectedEvent(restaurant, failureMessages, orderRejectedEventDomainEventPublisher);
        }

        return getOrderApprovedEvent(restaurant, failureMessages, orderApprovedEventDomainEventPublisher);
    }

    private static OrderRejectedEvent getOrderRejectedEvent(Restaurant restaurant, List<String> failureMessages, DomainEventPublisher<OrderRejectedEvent> orderRejectedEventDomainEventPublisher) {
        log.info("Order is rejected for order id: {}", restaurant.getOrderDetail().getId().getValue());
        restaurant.setOrderApproval(OrderApprovalStatus.REJECTED);
        return new OrderRejectedEvent(
                restaurant.getOrderApproval(),
                restaurant.getId(),
                failureMessages,
                ZonedDateTime.now(ZoneId.of(ZONE_ID)),
                orderRejectedEventDomainEventPublisher
        );
    }

    private static OrderApprovedEvent getOrderApprovedEvent(Restaurant restaurant, List<String> failureMessages, DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher) {
        log.info("Order is approved for order id: {}", restaurant.getOrderDetail().getId().getValue());
        restaurant.setOrderApproval(OrderApprovalStatus.APPROVED);
        return new OrderApprovedEvent(
                restaurant.getOrderApproval(),
                restaurant.getId(),
                failureMessages,
                ZonedDateTime.now(ZoneId.of(ZONE_ID)),
                orderApprovedEventDomainEventPublisher
        );
    }
}

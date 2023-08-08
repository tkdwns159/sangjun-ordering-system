package com.sangjun.restaurant.application;

import com.sangjun.restaurant.application.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.sangjun.restaurant.application.ports.output.message.publisher.OrderRejectedMessagePublisher;
import com.sangjun.restaurant.domain.event.OrderApprovalEvent;
import com.sangjun.restaurant.domain.event.OrderApprovedEvent;
import com.sangjun.restaurant.domain.event.OrderRejectedEvent;
import com.sangjun.restaurant.domain.exception.RestaurantDomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderApprovalEventShooter {
    private final OrderApprovedMessagePublisher orderApprovedMessagePublisher;
    private final OrderRejectedMessagePublisher orderRejectedMessagePublisher;

    public void fire(OrderApprovalEvent event) {
        if (event instanceof OrderApprovedEvent) {
            orderApprovedMessagePublisher.publish((OrderApprovedEvent) event);
        } else if (event instanceof OrderRejectedEvent) {
            orderRejectedMessagePublisher.publish((OrderRejectedEvent) event);
        } else {
            log.error("Invalid PaymentEvent: {}", event.getClass().toString());
            throw new RestaurantDomainException("Invalid OrderEvent: " + event.getClass());
        }

    }

}

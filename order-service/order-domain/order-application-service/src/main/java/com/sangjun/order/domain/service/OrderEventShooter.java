package com.sangjun.order.domain.service;

import com.sangjun.order.domain.event.OrderCancelledEvent;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.event.OrderEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.service.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.sangjun.order.domain.service.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.sangjun.order.domain.service.ports.output.message.publisher.restaurant.OrderPaidRestaurantRequestMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventShooter {

    private final OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher;
    private final OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher;
    private final OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher;

    public void fire(OrderEvent event) {
        if (event instanceof OrderCreatedEvent) {
            orderCreatedPaymentRequestMessagePublisher.publish((OrderCreatedEvent) event);
        } else if (event instanceof OrderCancelledEvent) {
            orderCancelledPaymentRequestMessagePublisher.publish((OrderCancelledEvent) event);
        } else if (event instanceof OrderPaidEvent) {
            orderPaidRestaurantRequestMessagePublisher.publish((OrderPaidEvent) event);
        } else {
            log.error("Invalid OrderEvent: {}", event.getClass().toString());
            throw new OrderDomainException("Invalid OrderEvent: " + event.getClass());
        }

    }

}

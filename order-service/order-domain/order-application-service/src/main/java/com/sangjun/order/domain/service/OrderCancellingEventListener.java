package com.sangjun.order.domain.service;

import com.sangjun.order.domain.event.OrderCancellingEvent;
import com.sangjun.order.domain.service.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCancellingEventListener implements ApplicationListener<OrderCancellingEvent> {
    private final OrderCancelledPaymentRequestMessagePublisher messagePublisher;

    @Override
    public void onApplicationEvent(OrderCancellingEvent event) {
        messagePublisher.publish(event);
    }
}

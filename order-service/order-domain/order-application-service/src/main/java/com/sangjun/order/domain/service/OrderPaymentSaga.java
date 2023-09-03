package com.sangjun.order.domain.service;

import com.sangjun.common.domain.event.DomainEvent;
import com.sangjun.common.domain.event.EmptyEvent;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.dto.message.PaymentResponse;
import com.sangjun.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentSaga implements SagaStep<PaymentResponse, DomainEvent, EmptyEvent> {
    private final OrderSagaHelper orderSagaHelper;

    @Override
    @Transactional
    public DomainEvent process(PaymentResponse data) {
        log.info("Completing payment with order id: {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        if (order.getOrderStatus() == OrderStatus.CANCELLING) {
            log.info("Order with id: {} is being cancelled", data.getOrderId());
            return EmptyEvent.INSTANCE;
        }
        var orderPaidEvent = order.pay();
        orderSagaHelper.loadOrderItems(orderPaidEvent.getOrder());
        return orderPaidEvent;
    }

    @Override
    @Transactional
    public EmptyEvent rollback(PaymentResponse data) {
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        order.cancel();
        return EmptyEvent.INSTANCE;
    }
}

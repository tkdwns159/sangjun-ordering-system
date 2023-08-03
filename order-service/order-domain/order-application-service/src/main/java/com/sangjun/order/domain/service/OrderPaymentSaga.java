package com.sangjun.order.domain.service;

import com.sangjun.common.domain.event.DomainEvent;
import com.sangjun.common.domain.event.EmptyEvent;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderPaidEvent;
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
    private final OrderDomainService orderDomainService;
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

        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);
        orderSagaHelper.saveOrder(order);
        log.info("Order with id: {} is paid", order.getId().getValue());
        orderSagaHelper.loadOrderItems(order);

        return orderPaidEvent;
    }

    @Override
    @Transactional
    public EmptyEvent rollback(PaymentResponse data) {
        log.info("Cancelling order with id: {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        orderDomainService.cancelOrder(order, data.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        log.info("Order with id: {} is cancelled", data.getOrderId());

        return EmptyEvent.INSTANCE;
    }


}

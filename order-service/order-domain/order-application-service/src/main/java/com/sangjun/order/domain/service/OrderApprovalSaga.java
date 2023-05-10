package com.sangjun.order.domain.service;

import com.sangjun.common.domain.event.EmptyEvent;
import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderCancelledEvent;
import com.sangjun.order.domain.service.dto.message.RestaurantApprovalResponse;
import com.sangjun.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse, EmptyEvent, OrderCancelledEvent> {
    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;

    @Transactional
    @Override
    public EmptyEvent process(RestaurantApprovalResponse data) {
        log.info("Approving order with id: {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        orderDomainService.approveOrder(order);
        orderSagaHelper.saveOrder(order);
        log.info("Order with id: {} is approved", order.getId().getValue());

        return EmptyEvent.INSTANCE;
    }

    @Override
    public OrderCancelledEvent rollback(RestaurantApprovalResponse data) {
        log.info("Cancelling order with id: {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        OrderCancelledEvent orderCancelledEvent = orderDomainService.initiateOrderCancel(
                order,
                data.getFailureMessages()
        );
        orderSagaHelper.saveOrder(order);
        log.info("Order with id: {} is cancelling", order.getId().getValue());

        return orderCancelledEvent;
    }
}

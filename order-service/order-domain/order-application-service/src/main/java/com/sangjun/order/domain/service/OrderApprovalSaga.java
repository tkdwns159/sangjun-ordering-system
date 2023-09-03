package com.sangjun.order.domain.service;

import com.sangjun.common.domain.event.EmptyEvent;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderCancellingEvent;
import com.sangjun.order.domain.service.dto.message.RestaurantApprovalResponse;
import com.sangjun.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse, EmptyEvent, OrderCancellingEvent> {
    private final OrderSagaHelper orderSagaHelper;

    @Transactional
    @Override
    public EmptyEvent process(RestaurantApprovalResponse data) {
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        order.approve();

        return EmptyEvent.INSTANCE;
    }

    @Transactional
    @Override
    public OrderCancellingEvent rollback(RestaurantApprovalResponse data) {
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        return order.initCancel();
    }
}

package com.sangjun.order.domain.service;

import com.sangjun.common.utils.CommonConstants;
import com.sangjun.order.domain.event.OrderCancelledEvent;
import com.sangjun.order.domain.service.dto.message.RestaurantApprovalResponse;
import com.sangjun.order.domain.service.ports.input.message.listener.restaurant.RestaurantApprovalMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class RestaurantApprovalMessageListenerImpl implements RestaurantApprovalMessageListener {
    private final OrderApprovalSaga orderApprovalSaga;

    @Override
    public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {
        orderApprovalSaga.process(restaurantApprovalResponse);
        log.info("Order is approved for order id: {}", restaurantApprovalResponse.getOrderId());
    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {
        OrderCancelledEvent orderCancelledEvent = orderApprovalSaga.rollback(restaurantApprovalResponse);
        log.info("Publishing order cancelled event for order id: {} with failure messages: {}",
                orderCancelledEvent.getOrder().getId().getValue(),
                String.join(CommonConstants.FAILURE_MESSAGE_DELIMITER, restaurantApprovalResponse.getFailureMessages()));
        orderCancelledEvent.fire();
    }
}

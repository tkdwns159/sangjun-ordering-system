package com.sangjun.restaurant.application;

import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import com.sangjun.restaurant.application.ports.input.message.listener.RestaurantApprovalRequestMessageListener;
import com.sangjun.restaurant.domain.event.OrderApprovalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantApprovalRequestMessageListenerImpl implements RestaurantApprovalRequestMessageListener {
    private final RestaurantApprovalRequestHelper restaurantApprovalRequestHelper;
    private final OrderApprovalEventShooter orderApprovalEventShooter;

    @Override
    public void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest) {
        OrderApprovalEvent orderApprovalEvent = restaurantApprovalRequestHelper
                .persistOrderApproval(restaurantApprovalRequest);
        orderApprovalEventShooter.fire(orderApprovalEvent);
    }
}

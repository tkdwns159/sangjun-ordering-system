package com.sangjun.restaurant.application;

import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import com.sangjun.restaurant.application.ports.input.message.listener.RestaurantApprovalRequestMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantApprovalRequestMessageListenerImpl implements RestaurantApprovalRequestMessageListener {
    private final RestaurantApprovalRequestHelper restaurantApprovalRequestHelper;

    @Override
    public void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest) {
        restaurantApprovalRequestHelper
                .persistOrderApproval(restaurantApprovalRequest)
                .fire();
    }
}

package com.sangjun.restaurant.application.ports.input.message.listener;

import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;

public interface OrderApprovalRequestMessageListener {
    void registerPendingOrder(RestaurantApprovalRequest restaurantApprovalRequest);
}

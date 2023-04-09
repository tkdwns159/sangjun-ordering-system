package com.sangjun.order.domain.service.ports.input.message.listener.restaurant;

import com.sangjun.order.domain.service.dto.message.RestaurantApprovalResponse;

public interface RestaurantApprovalMessageListener {

    void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse);

    void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);

}

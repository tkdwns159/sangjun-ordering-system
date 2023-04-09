package com.sangjun.restaurant.domain.entity;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.OrderApprovalStatus;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.restaurant.domain.valueobject.OrderApprovalId;

public class OrderApproval extends BaseEntity<OrderApprovalId> {
    private final RestaurantId restaurantId;
    private final OrderId orderId;
    private final OrderApprovalStatus orderApprovalStatus;

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public OrderApprovalStatus getOrderApprovalStatus() {
        return orderApprovalStatus;
    }

    private OrderApproval(Builder builder) {
        setId(builder.id);
        restaurantId = builder.restaurantId;
        orderId = builder.orderId;
        orderApprovalStatus = builder.orderApprovalStatus;
    }

    public static Builder builder(RestaurantId restaurantId, OrderId orderId, OrderApprovalStatus orderApprovalStatus) {
        return new Builder(restaurantId, orderId, orderApprovalStatus);
    }

    public static final class Builder {
        private OrderApprovalId id;
        private final RestaurantId restaurantId;
        private final OrderId orderId;
        private final OrderApprovalStatus orderApprovalStatus;

        private Builder(RestaurantId restaurantId, OrderId orderId, OrderApprovalStatus orderApprovalStatus) {
            this.restaurantId = restaurantId;
            this.orderId = orderId;
            this.orderApprovalStatus = orderApprovalStatus;
        }

        public Builder id(OrderApprovalId val) {
            id = val;
            return this;
        }

        public OrderApproval build() {
            return new OrderApproval(this);
        }
    }
}

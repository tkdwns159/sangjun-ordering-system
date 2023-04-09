package com.sangjun.restaurant.domain.entity;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderApprovalStatus;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.restaurant.domain.valueobject.OrderApprovalId;

import java.util.List;
import java.util.UUID;

public class Restaurant extends AggregateRoot<RestaurantId> {
    private OrderApproval orderApproval;
    private boolean active;
    private final OrderDetail orderDetail;

    public void validateOrder(List<String> failureMessages) {
        validateOrderStatus(failureMessages);
        validateOrderTotalAmount(failureMessages);
    }

    private void validateOrderStatus(List<String> failureMessages) {
        if (orderDetail.getOrderStatus() != OrderStatus.PAID) {
            failureMessages.add("Payment is not completed for order: " + orderDetail.getId());
        }
    }

    private void validateOrderTotalAmount(List<String> failureMessages) {
        Money totalAmount = orderDetail.getProducts().stream()
                .map(product -> {
                    if (!product.isAvailable()) {
                        failureMessages.add("Product with id: " + product.getId().getValue() + " is not available");
                    }

                    return product.getPrice().multiply(product.getQuantity());
                })
                .reduce(Money.ZERO, Money::add);

        if (!totalAmount.equals(orderDetail.getTotalAmount())) {
            failureMessages.add("Price total is not correct for order: " + orderDetail.getId());
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setOrderApproval(OrderApprovalStatus orderApprovalStatus) {
        this.orderApproval = OrderApproval.builder(
                        this.getId(),
                        this.orderDetail.getId(),
                        orderApprovalStatus)
                .id(new OrderApprovalId(UUID.randomUUID()))
                .build();
    }

    public OrderApproval getOrderApproval() {
        return orderApproval;
    }

    public boolean isActive() {
        return active;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    private Restaurant(Builder builder) {
        setId(builder.id);
        orderApproval = builder.orderApproval;
        active = builder.active;
        orderDetail = builder.orderDetail;
    }

    public static Builder builder(OrderDetail orderDetail) {
        return new Builder(orderDetail);
    }


    public static final class Builder {
        private RestaurantId id;
        private OrderApproval orderApproval;
        private boolean active;
        private final OrderDetail orderDetail;

        private Builder(OrderDetail orderDetail) {
            this.orderDetail = orderDetail;
        }

        public Builder id(RestaurantId val) {
            id = val;
            return this;
        }

        public Builder orderApproval(OrderApproval val) {
            orderApproval = val;
            return this;
        }

        public Builder active(boolean val) {
            active = val;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}

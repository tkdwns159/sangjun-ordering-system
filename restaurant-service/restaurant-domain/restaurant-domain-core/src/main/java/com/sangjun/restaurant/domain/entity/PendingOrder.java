package com.sangjun.restaurant.domain.entity;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.restaurant.domain.event.OrderApprovedEvent;
import com.sangjun.restaurant.domain.valueobject.PendingOrderId;
import com.sangjun.restaurant.domain.valueobject.PendingOrderStatus;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;
import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "pending_order", schema = "restaurant")
@Access(AccessType.FIELD)
public class PendingOrder extends BaseEntity<PendingOrderId> {

    @Embedded
    private RestaurantId restaurantId;

    @Embedded
    private OrderId orderId;

    @Enumerated(EnumType.STRING)
    private PendingOrderStatus status;

    protected PendingOrder() {
    }

    private PendingOrder(Builder builder) {
        setId(new PendingOrderId(UUID.randomUUID()));
        restaurantId = requireNonNull(builder.restaurantId, "restaurantId");
        orderId = requireNonNull(builder.orderId, "orderId");
        status = requireNonNull(builder.status, "status");
    }

    public static Builder builder() {
        return new Builder();
    }

    public PendingOrderStatus getStatus() {
        return status;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public OrderApprovedEvent approve() {
        this.status = PendingOrderStatus.APPROVED;
        return new OrderApprovedEvent(this, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }

    public static final class Builder {
        private RestaurantId restaurantId;
        private OrderId orderId;
        private PendingOrderStatus status;

        private Builder() {
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder status(PendingOrderStatus val) {
            status = val;
            return this;
        }

        public PendingOrder build() {
            return new PendingOrder(this);
        }
    }
}

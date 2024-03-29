package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.*;
import com.sangjun.order.domain.FailureMessageAttributeConverter;
import com.sangjun.order.domain.event.OrderCancellingEvent;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.valueobject.OrderItem;
import com.sangjun.order.domain.valueobject.StreetAddress;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

@Entity
@Table(name = "p_orders", schema = "p_order")
@Access(AccessType.FIELD)
public class Order extends AggregateRoot<OrderId> {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(Order.class.getName());

    @Embedded
    private CustomerId customerId;
    @Embedded
    private RestaurantId restaurantId;
    @Embedded
    private StreetAddress deliveryAddress;
    @Embedded
    private Money price;
    @Embedded
    private TrackingId trackingId;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST,
                    CascadeType.MERGE,
                    CascadeType.REMOVE},
            orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;

    @Convert(converter = FailureMessageAttributeConverter.class)
    private List<String> failureMessages;

    private Order(
            CustomerId customerId,
            RestaurantId restaurantId,
            StreetAddress deliveryAddress,
            Money price,
            List<OrderItem> items,
            List<String> failureMessages) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress;
        this.price = price;
        this.orderStatus = OrderStatus.PENDING;
        this.items = items;
        this.failureMessages = failureMessages;
    }

    protected Order() {
    }

    private static Order of(Builder builder) {
        return new Order(
                requireNonNull(builder.customerId, "customerId"),
                requireNonNull(builder.restaurantId, "restaurantId"),
                requireNonNull(builder.deliveryAddress, "deliveryAddress"),
                requireNonNull(builder.price, "price"),
                requireNonNullElse(builder.items, new ArrayList<>()),
                requireNonNullElse(builder.failureMessages, new ArrayList<>())
        );
    }

    public void setFailureMessages(List<String> failureMessages) {
        this.failureMessages = failureMessages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public StreetAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public Money getPrice() {
        return price;
    }

    public OrderItem getItemOfIndex(int idx) {
        return this.items.get(idx);
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public OrderCreatedEvent initialize() {
        validate();
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItems();
        return new OrderCreatedEvent(this, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }

    private void validate() {
        validateOrderItems();
        checkIfPriceEqualsSumOfOrderItemsPrice();
    }

    private void validateOrderItems() {
        for (OrderItem item : this.items) {
            item.validate();
        }
    }

    private void checkIfPriceEqualsSumOfOrderItemsPrice() {
        Money itemsPriceSum = this.items.stream().map(OrderItem::getSubTotal)
                .reduce(Money.ZERO, Money::add);
        if (!this.price.equals(itemsPriceSum)) {
            throw new IllegalStateException(
                    String.format("order price(%s) is not equal to the sum of order items price(%s)",
                            this.price, itemsPriceSum));
        }
    }

    private void initializeOrderItems() {
        long itemId = 1L;
        for (OrderItem orderItem : items) {
            orderItem.initialize(super.getId(), itemId++);
        }
    }

    public OrderPaidEvent pay() {
        if (this.orderStatus != OrderStatus.PENDING) {
            log.error("Order must be in PENDING state for pay operation! Order status: {}",
                    this.orderStatus);
            throw new OrderDomainException("Order must be in PENDING state for pay operation!" +
                    " Order status: " + this.orderStatus);
        }

        this.orderStatus = OrderStatus.PAID;

        return new OrderPaidEvent(this, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }

    public void approve() {
        this.orderStatus = OrderStatus.APPROVED;
    }

    public OrderCancellingEvent initCancel() {
        this.orderStatus = OrderStatus.CANCELLING;
        return new OrderCancellingEvent(this, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }

    private void updateFailureMessages(List<String> failureMessages) {
        this.failureMessages = new ArrayList<>();

        this.failureMessages.addAll(failureMessages.stream()
                .filter(Predicate.not(String::isBlank))
                .toList());
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(this.items);
    }


    public static final class Builder {
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private List<String> failureMessages;

        private Builder() {
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder deliveryAddress(StreetAddress val) {
            deliveryAddress = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder items(List<OrderItem> val) {
            items = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return Order.of(this);
        }
    }
}

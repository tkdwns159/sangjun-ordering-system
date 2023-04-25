package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.*;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.valueobject.OrderItemId;
import com.sangjun.order.domain.valueobject.StreetAddress;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {

    private static final Logger log = LoggerFactory.getLogger(Order.class.getName());
    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;

    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;

    private Order(Builder builder) {
        setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
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

    public List<OrderItem> getItems() {
        return items;
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

    public void initializeOrder() {
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItems();
    }

    private void initializeOrderItems() {
        long itemId = 1L;
        for (OrderItem orderItem : items) {
            orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
        }
    }

    public void validateOrder() {
        checkIdIsEmpty();
        checkOrderStatusIsEmpty();
        checkTotalPriceIsPresent();

        checkItemSubTotalsArePresent();
        checkItemPricesArePresent();

        checkTotalPriceEqualsActualItemSubTotalSum();
        checkItemSubTotalEqualsActualItemPriceSum();
    }


    public void checkIdIsEmpty() {
        if (this.getId() != null) {
            log.error("Order id is not null! {}", this.getId().getValue());
            throw new OrderDomainException("Order id is not null! " + this.getId().getValue());
        }
    }

    private void checkOrderStatusIsEmpty() {
        if (this.orderStatus != null) {
            log.error("Order status is not null! {}", this.orderStatus);
            throw new OrderDomainException("Order status is not null! " + this.orderStatus);
        }
    }

    private void checkTotalPriceIsPresent() {
        if (this.price == null) {
            log.error("Total price is null!");
            throw new OrderDomainException("Total price is null!");
        }
    }

    private void checkTotalPriceEqualsActualItemSubTotalSum() {
        Money orderItemsTotal = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(Money.ZERO, Money::add);

        if (!this.price.equals(orderItemsTotal)) {
            log.error("Total price: {} is not equal to Order items total: {}",
                    price.getAmount(), orderItemsTotal.getAmount());
            throw new OrderDomainException("Total price: " + price.getAmount()
                    + " is not equal to Order items total: " + orderItemsTotal.getAmount());
        }
    }

    private void checkItemSubTotalsArePresent() {
        this.items.forEach(OrderItem::checkSubTotalIsPresent);
    }

    private void checkItemPricesArePresent() {
        this.items.forEach(OrderItem::checkPriceIsPresent);
    }

    private void checkItemSubTotalEqualsActualItemPriceSum() {
        this.items.forEach(OrderItem::checkSubTotalEqualsActualPriceSum);
    }

    public void pay() {
        if (this.orderStatus != OrderStatus.PENDING) {
            log.error("Order must be in PENDING state for pay operation! Order status: {}",
                    this.orderStatus);
            throw new OrderDomainException("Order must be in PENDING state for pay operation!" +
                    " Order status: " + this.orderStatus);
        }

        this.orderStatus = OrderStatus.PAID;
    }

    public void approve() {
        if (this.orderStatus != OrderStatus.PAID) {
            log.error("Order must be in PAID state for approve operation! Order status: {}",
                    this.orderStatus);
            throw new OrderDomainException("Order must be in PAID state for approve operation! Order status: "
                    + this.orderStatus);
        }

        this.orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel(List<String> failureMessages) {
        if (orderStatus != OrderStatus.PAID) {
            log.error("Order must be in PAID state for cancel operation! Order status: {}",
                    this.orderStatus);
            throw new OrderDomainException("Order must be in PAID state for cancel operation! " +
                    "Order status: " + this.orderStatus);
        }

        this.orderStatus = OrderStatus.CANCELLING;
        updateFailureMessages(failureMessages);
    }

    private void updateFailureMessages(List<String> failureMessages) {
        if (this.failureMessages != null && failureMessages != null) {
            this.failureMessages.addAll(failureMessages
                    .stream()
                    .filter(String::isBlank)
                    .toList());
            return;
        }

        if (this.failureMessages == null) {
            this.failureMessages = failureMessages;
        }
    }

    public void cancel(List<String> failureMessages) {
        if (orderStatus == OrderStatus.CANCELLING || orderStatus == OrderStatus.PENDING) {
            log.error("Order must be in PAID or CANCELLED state for cancel operation! Order Status: {}",
                    this.orderStatus);
            throw new OrderDomainException("Order must be in PAID or CANCELLED state " +
                    "for cancel operation! Order Status: " + this.orderStatus);
        }

        this.orderStatus = OrderStatus.CANCELED;
        updateFailureMessages(failureMessages);
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public static final class Builder {
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;

        private Builder() {
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
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

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}

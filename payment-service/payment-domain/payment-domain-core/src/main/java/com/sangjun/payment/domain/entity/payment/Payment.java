package com.sangjun.payment.domain.entity.payment;


import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.*;
import com.sangjun.payment.domain.valueobject.payment.PaymentId;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;
import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "payments", schema = "payment")
@Access(AccessType.FIELD)
public class Payment extends AggregateRoot<PaymentId> {
    @Embedded
    private OrderId orderId;
    @Embedded
    private RestaurantId restaurantId;
    @Embedded
    private CustomerId customerId;
    @Embedded
    private Money price;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private ZonedDateTime createdAt;

    private Payment(OrderId orderId, RestaurantId restaurantId, CustomerId customerId, Money price) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.customerId = customerId;
        this.price = price;
    }

    protected Payment() {
    }

    private static Payment of(Builder builder) {
        return new Payment(
                requireNonNull(builder.orderId, "orderId"),
                requireNonNull(builder.restaurantId, "restaurantId"),
                requireNonNull(builder.customerId, "customerId"),
                requireNonNull(builder.price, "price"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public void initialize() {
        validate();
        setId(new PaymentId(UUID.randomUUID()));
        this.paymentStatus = PaymentStatus.PENDING;
    }

    @PrePersist
    private void stampCreatedAt() {
        this.createdAt = ZonedDateTime.now(ZoneId.of(ZONE_ID));
    }

    private void validate() {
        checkIfPriceIsGreaterThanZero();
    }

    private void checkIfPriceIsGreaterThanZero() {
        if (!price.isGreaterThanZero()) {
            throw new IllegalStateException(
                    String.format("Price(%s) must be greater than zero",
                            this.price.getAmount().toString()));
        }
    }

    public void complete() {
        this.paymentStatus = PaymentStatus.COMPLETED;
    }

    public void cancel() {
        if (this.paymentStatus != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    String.format("Payment(id: %s) - PaymentStatus(%s) must be COMPLETED to cancel payment",
                            this.getId().getValue(), this.paymentStatus.toString()));
        }
        this.paymentStatus = PaymentStatus.CANCELLED;
    }

    public void markAsFailed() {
        if (this.paymentStatus == PaymentStatus.FAILED) {
            throw new IllegalStateException(
                    String.format("Payment(id: %s) is already marked as FAILED",
                            this.getId().getValue()));
        }
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getPrice() {
        return price;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public static final class Builder {
        private OrderId orderId;
        private RestaurantId restaurantId;
        private CustomerId customerId;
        private Money price;

        private Builder() {
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Payment build() {
            return Payment.of(this);
        }
    }
}

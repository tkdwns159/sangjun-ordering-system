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
@AttributeOverride(name = "value", column = @Column(name = "id"))
public class Payment extends AggregateRoot<PaymentId> {
    @Embedded
    private final OrderId orderId;
    @Embedded
    private final RestaurantId restaurantId;
    @Embedded
    private final CustomerId customerId;
    @Embedded
    private final Money price;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private ZonedDateTime createdAt;

    private Payment(Builder builder) {
        orderId = requireNonNull(builder.orderId, "orderId");
        restaurantId = requireNonNull(builder.restaurantId, "restaurantId");
        customerId = requireNonNull(builder.customerId, "customerId");
        price = requireNonNull(builder.price, "price");
    }

    public static Builder builder() {
        return new Builder();
    }

    public void initialize() {
        validate();
        setId(new PaymentId(UUID.randomUUID()));
        this.createdAt = ZonedDateTime.now(ZoneId.of(ZONE_ID));
        this.paymentStatus = PaymentStatus.READY;
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
            return new Payment(this);
        }
    }
}

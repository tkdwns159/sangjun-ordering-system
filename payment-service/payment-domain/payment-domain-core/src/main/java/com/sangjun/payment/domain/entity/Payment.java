package com.sangjun.payment.domain.entity;


import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.PaymentStatus;
import com.sangjun.payment.domain.valueobject.PaymentId;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class Payment extends AggregateRoot<PaymentId> {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money price;

    private PaymentStatus paymentStatus;
    private ZonedDateTime createdAt;

    private Payment(Builder builder) {
        setId(builder.id);
        orderId = builder.orderId;
        customerId = builder.customerId;
        price = builder.price;
        paymentStatus = builder.paymentStatus;
        createdAt = builder.createdAt;
    }

    public static Builder builder(OrderId orderId, CustomerId customerId, Money price) {
        return new Builder(orderId, customerId, price);
    }

    public void initializePayment() {
        setId(new PaymentId(UUID.randomUUID()));
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC+9"));
    }

    public void validatePayment(List<String> failureMessages) {
        if (this.price == null || !price.isGreaterThanZero()) {
            failureMessages.add("Total price must be greater than zero!");
        }
    }

    public void updateStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
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
        private PaymentId id;
        private final OrderId orderId;
        private final CustomerId customerId;
        private final Money price;
        private PaymentStatus paymentStatus;
        private ZonedDateTime createdAt;

        private Builder(OrderId orderId, CustomerId customerId, Money price) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.price = price;
        }

        public Builder id(PaymentId val) {
            id = val;
            return this;
        }

        public Builder paymentStatus(PaymentStatus val) {
            paymentStatus = val;
            return this;
        }

        public Builder createdAt(ZonedDateTime val) {
            createdAt = val;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}

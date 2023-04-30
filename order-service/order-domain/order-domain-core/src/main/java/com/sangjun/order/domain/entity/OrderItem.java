package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.valueobject.OrderItemId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderItem extends BaseEntity<OrderItemId> {
    private static final Logger log = LoggerFactory.getLogger(OrderItem.class.getName());
    private OrderId orderId;
    private final Product product;
    private final int quantity;
    private final Money price;
    private final Money subTotal;

    public OrderItem(OrderItemId orderItemId, OrderId orderId, Product product, int quantity, Money price, Money subTotal) {
        setId(orderItemId);
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.subTotal = subTotal;
    }

    private OrderItem(Builder builder) {
        setId(builder.id);
        orderId = builder.orderId;
        product = builder.product;
        quantity = builder.quantity;
        price = builder.price;
        subTotal = builder.subTotal;
    }

    public static Builder builder() {
        return new Builder();
    }


    public OrderId getOrderId() {
        return orderId;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getPrice() {
        return price;
    }

    public Money getSubTotal() {
        return subTotal;
    }

    void initializeOrderItem(OrderId orderId, OrderItemId orderItemId) {
        this.orderId = orderId;
        super.setId(orderItemId);
    }

    public void checkSubTotalIsPresent() {
        if (this.subTotal == null) {
            log.error("subtotal is empty");
            throw new OrderDomainException("subtotal is empty");
        }
    }

    public void checkPriceIsPresent() {
        if (this.price == null) {
            log.error("price is empty");
            throw new OrderDomainException("price is empty");
        }
    }

    public void checkSubTotalEqualsActualPriceSum() {
        Money actualPriceSum = this.price.multiply(this.quantity);

        if (!actualPriceSum.equals(this.subTotal)) {
            log.error("subtotal: {} is not equals actual price sum: {}",
                    this.subTotal.getAmount(), actualPriceSum.getAmount());
            throw new OrderDomainException("subtotal: " + this.subTotal.getAmount() + " " +
                    "is not equals actual price sum: " + actualPriceSum.getAmount());
        }
    }

    public static final class Builder {
        private OrderItemId id;
        private OrderId orderId;
        private Product product;
        private int quantity;
        private Money price;
        private Money subTotal;

        private Builder() {
        }

        public Builder orderItemId(OrderItemId val) {
            id = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder product(Product val) {
            product = val;
            return this;
        }

        public Builder quantity(int val) {
            quantity = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder subTotal(Money val) {
            subTotal = val;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }
}

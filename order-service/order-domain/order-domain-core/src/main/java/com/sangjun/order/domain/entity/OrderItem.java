package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.valueobject.OrderItemId;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(name = "order_items", schema = "p_order")
@Access(AccessType.FIELD)
public class OrderItem {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(OrderItem.class.getName());

    @GenericGenerator(
            name = "order_item_id_gen",
            strategy = "com.sangjun.order.domain.entity.OrderItemIdGenerator",
            parameters = {
                    @Parameter(name = OrderItemIdGenerator.TYPE, value = "SEQUENCE"),
                    @Parameter(name = OrderItemIdGenerator.SEQUENCE_NAME, value = "order_item_id_seq")
            })
    @GeneratedValue(generator = "order_item_id_gen")
    @EmbeddedId
    private OrderItemId id;
    @Embedded
    private OrderId orderId;
    @Embedded
    private ProductId productId;
    private int quantity;
    @Embedded
    private Money price;
    @Embedded
    private Money subTotal;

    public OrderItem(OrderItemId orderItemId,
                     OrderId orderId,
                     ProductId productId,
                     int quantity,
                     Money price,
                     Money subTotal) {
        this.id = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.subTotal = subTotal;
    }

    protected OrderItem() {
    }

    private OrderItem(Builder builder) {
        id = builder.id;
        orderId = builder.orderId;
        productId = builder.productId;
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

    public int getQuantity() {
        return quantity;
    }

    public Money getPrice() {
        return price;
    }

    public Money getSubTotal() {
        return subTotal;
    }

    public ProductId getProductId() {
        return productId;
    }

    void initializeOrderItem(OrderId orderId, OrderItemId orderItemId) {
        this.orderId = orderId;
        this.id = orderItemId;
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
        private ProductId productId;
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

        public Builder productId(ProductId val) {
            productId = val;
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

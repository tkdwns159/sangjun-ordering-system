package com.sangjun.order.domain.valueobject;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.ProductId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(name = "order_items", schema = "p_order")
@Access(AccessType.FIELD)
public class OrderItem {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(OrderItem.class.getName());

    @EmbeddedId
    private OrderItemId orderItemId;
    @Embedded
    private ProductId productId;
    private int quantity;
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price"))
    private Money price;
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "sub_total"))
    private Money subTotal;

    public OrderItem(OrderItemId orderItemId,
                     ProductId productId,
                     int quantity,
                     Money price,
                     Money subTotal) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.subTotal = subTotal;
    }

    protected OrderItem() {
    }

    private OrderItem(Builder builder) {
        orderItemId = builder.orderItemId;
        productId = builder.productId;
        quantity = builder.quantity;
        price = builder.price;
        subTotal = builder.subTotal;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void initialize(OrderId orderId, Long orderItemId) {
        this.orderItemId = new OrderItemId(orderId, orderItemId);
    }

    public Money getSubTotal() {
        return this.subTotal;
    }

    public Money getPrice() {
        return this.price;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public OrderItemId getOrderItemId() {
        return this.orderItemId;
    }


    public static final class Builder {
        private OrderItemId orderItemId;
        private ProductId productId;
        private int quantity;
        private Money price;
        private Money subTotal;

        private Builder() {
        }

        public Builder orderItemId(OrderItemId val) {
            orderItemId = val;
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

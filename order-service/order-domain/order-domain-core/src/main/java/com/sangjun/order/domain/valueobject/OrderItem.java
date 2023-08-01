package com.sangjun.order.domain.valueobject;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.ProductId;

import javax.persistence.*;

@Entity
@Table(name = "order_items", schema = "p_order")
@Access(AccessType.FIELD)
public class OrderItem {

    @EmbeddedId
    private OrderItemId id;
    @Embedded
    private ProductId productId;
    private int quantity;
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price"))
    private Money price;
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "sub_total"))
    private Money subTotal;

    public OrderItem(OrderItemId id,
                     ProductId productId,
                     int quantity,
                     Money price,
                     Money subTotal) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.subTotal = subTotal;
    }

    protected OrderItem() {
    }

    private OrderItem(Builder builder) {
        productId = builder.productId;
        quantity = builder.quantity;
        price = builder.price;
        subTotal = builder.subTotal;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void initialize(OrderId orderId, Long orderItemId) {
        this.id = new OrderItemId(orderId, orderItemId);
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

    public OrderItemId getId() {
        return this.id;
    }

    public void validate() {
        Money subTotal = this.getPrice().multiply(this.getQuantity());
        if (!subTotal.equals(this.subTotal)) {
            throw new IllegalStateException(
                    String.format("OrderItem of product(%s): subTotal(%s) is not equal to the sum(%s) of price * quantity",
                            this.productId.getValue(), this.subTotal, subTotal));
        }
    }

    public static final class Builder {
        private ProductId productId;
        private int quantity;
        private Money price;
        private Money subTotal;

        private Builder() {
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

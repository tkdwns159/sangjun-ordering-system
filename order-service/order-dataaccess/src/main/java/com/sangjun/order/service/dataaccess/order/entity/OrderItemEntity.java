package com.sangjun.order.service.dataaccess.order.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Table(name = "order_items", schema = "p_order")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IdClass(OrderItemEntityId.class)
public class OrderItemEntity {

    @Id
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ORDER_ID")
    private OrderEntity order;
    private UUID productId;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemEntity that = (OrderItemEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, order);
    }
}

package com.sangjun.order.domain.valueobject;

import com.sangjun.common.domain.valueobject.BaseId;

import javax.persistence.*;

@Embeddable
@AttributeOverride(name = "value", column = @Column(name = "order_item_id"))
@Access(AccessType.FIELD)
public class OrderItemId extends BaseId<Long> {
    public OrderItemId(Long value) {
        super(value);
    }

    protected OrderItemId() {

    }
}

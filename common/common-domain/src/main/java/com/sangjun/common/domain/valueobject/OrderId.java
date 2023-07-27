package com.sangjun.common.domain.valueobject;

import javax.persistence.*;

@Embeddable
@Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "order_id"))
public class OrderId extends BaseId<java.util.UUID> {
    public OrderId(java.util.UUID value) {
        super(value);
    }

    protected OrderId() {
    }
}

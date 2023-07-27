package com.sangjun.common.domain.valueobject;

import javax.persistence.*;
import java.util.UUID;

@Embeddable
@Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "order_id"))
public class OrderId extends BaseId<UUID> {
    public OrderId(UUID value) {
        super(value);
    }

    protected OrderId() {
    }
}

package com.sangjun.common.domain.valueobject;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@AttributeOverride(name = "value", column = @Column(name = "order_id"))
public class OrderId extends BaseId<UUID> {
    public OrderId(UUID value) {
        super(value);
    }

    protected OrderId() {
    }
}

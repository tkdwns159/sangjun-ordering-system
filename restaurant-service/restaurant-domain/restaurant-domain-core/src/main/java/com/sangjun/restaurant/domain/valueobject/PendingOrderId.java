package com.sangjun.restaurant.domain.valueobject;

import com.sangjun.common.domain.valueobject.BaseId;

import javax.persistence.*;
import java.util.UUID;

@Embeddable
@AttributeOverride(name = "value", column = @Column(name = "pending_order_id"))
@Access(AccessType.FIELD)
public class PendingOrderId extends BaseId<UUID> {
    public PendingOrderId(UUID value) {
        super(value);
    }

    protected PendingOrderId() {
    }
}

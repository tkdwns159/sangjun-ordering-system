package com.sangjun.order.domain.valueobject;

import com.sangjun.common.domain.valueobject.BaseId;

import javax.persistence.*;
import java.util.UUID;

@Embeddable
@Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "tracking_id"))
public class TrackingId extends BaseId<UUID> {
    public TrackingId(UUID value) {
        super(value);
    }

    protected TrackingId() {
    }
}

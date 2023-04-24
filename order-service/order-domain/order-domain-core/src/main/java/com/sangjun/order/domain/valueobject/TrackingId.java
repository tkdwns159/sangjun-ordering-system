package com.sangjun.order.domain.valueobject;

import com.sangjun.common.domain.valueobject.BaseId;

import java.util.UUID;

public class TrackingId extends BaseId<UUID> {
    public TrackingId(UUID id) {
        super(id);
    }
}

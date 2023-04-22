package com.sangjun.order.domain.valueobject;

import java.util.UUID;

public class TrackingId {
    private final UUID id;

    public UUID getId() {
        return id;
    }

    public TrackingId(UUID id) {
        this.id = id;
    }
}

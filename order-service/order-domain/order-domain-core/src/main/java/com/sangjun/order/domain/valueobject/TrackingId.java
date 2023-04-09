package com.sangjun.order.domain.valueobject;

import lombok.Getter;

import java.util.UUID;

@Getter
public class TrackingId {
    private final UUID id;

    public TrackingId(UUID id) {
        this.id = id;
    }
}

package com.sangjun.restaurant.domain.valueobject;

import com.sangjun.common.domain.valueobject.BaseId;

import java.util.UUID;

public class OrderApprovalId extends BaseId<UUID> {
    public OrderApprovalId(UUID value) {
        super(value);
    }
}

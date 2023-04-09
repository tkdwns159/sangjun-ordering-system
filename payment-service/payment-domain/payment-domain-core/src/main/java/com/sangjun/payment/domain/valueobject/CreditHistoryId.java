package com.sangjun.payment.domain.valueobject;

import com.sangjun.common.domain.valueobject.BaseId;

import java.util.UUID;

public class CreditHistoryId extends BaseId<UUID> {
    public CreditHistoryId(UUID value) {
        super(value);
    }
}

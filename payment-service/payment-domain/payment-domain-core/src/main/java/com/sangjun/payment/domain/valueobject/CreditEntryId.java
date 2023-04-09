package com.sangjun.payment.domain.valueobject;

import com.sangjun.common.domain.valueobject.BaseId;

import java.util.UUID;

public class CreditEntryId extends BaseId<UUID> {
    public CreditEntryId(UUID value) {
        super(value);
    }
}

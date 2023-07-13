package com.sangjun.payment.domain.valueobject.payment;

import com.sangjun.common.domain.valueobject.BaseId;

import java.util.UUID;

public class PaymentId extends BaseId<UUID> {
    public PaymentId(UUID value) {
        super(value);
    }
}

package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.common.domain.valueobject.BaseId;

import java.util.UUID;

public class BookId extends BaseId<UUID> {
    public BookId(UUID value) {
        super(value);
    }
}

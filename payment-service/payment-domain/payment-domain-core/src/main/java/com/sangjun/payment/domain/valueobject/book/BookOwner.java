package com.sangjun.payment.domain.valueobject.book;

import java.util.UUID;

public class BookOwner {

    private UUID ownerUuid;
    private Long ownerId;

    private BookOwner(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    private BookOwner(Long ownerId) {
        this.ownerId = ownerId;
    }

    public static BookOwner uuidOf(UUID id) {
        return new BookOwner(id);
    }

    public static BookOwner longOf(Long id) {
        return new BookOwner(id);
    }
}

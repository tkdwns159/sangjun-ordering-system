package com.sangjun.payment.domain.valueobject.book;

import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class BookOwner {

    private final UUID ownerUuid;
    private final Long ownerId;

    private BookOwner(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        this.ownerId = null;
    }

    private BookOwner(Long ownerId) {
        this.ownerId = ownerId;
        this.ownerUuid = null;
    }

    public static BookOwner uuidOf(UUID id) {
        return new BookOwner(id);
    }

    public static BookOwner longOf(Long id) {
        return new BookOwner(id);
    }
}

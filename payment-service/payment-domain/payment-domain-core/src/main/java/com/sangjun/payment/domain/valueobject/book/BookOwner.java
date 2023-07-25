package com.sangjun.payment.domain.valueobject.book;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class BookOwner {

    @Column(name = "owner_uuid")
    private UUID uuid;

    @Column(name = "owner_long_id")
    private Long longId;

    private BookOwner(UUID ownerUuid) {
        this.uuid = ownerUuid;
        this.longId = null;
    }

    private BookOwner(Long longId) {
        this.longId = longId;
        this.uuid = null;
    }

    protected BookOwner() {
    }

    public static BookOwner uuidOf(UUID id) {
        return new BookOwner(id);
    }

    public static BookOwner longOf(Long id) {
        return new BookOwner(id);
    }
}

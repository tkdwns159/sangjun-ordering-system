package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.payment.domain.exception.BookDomainException;

import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class EntryIdType {
    private final String value;

    public EntryIdType(String value) {
        this.value = value;
    }

    public BookOwner createBookOwner(String id) {
        if (isUUID()) {
            return BookOwner.uuidOf(UUID.fromString(id));
        }

        if (isLong()) {
            return BookOwner.longOf(Long.valueOf(id));
        }

        throw new BookDomainException("No supporting id type: " + this.value);
    }

    private boolean isUUID() {
        return value.equalsIgnoreCase("UUID");
    }

    private boolean isLong() {
        return value.equalsIgnoreCase("Long");
    }
}

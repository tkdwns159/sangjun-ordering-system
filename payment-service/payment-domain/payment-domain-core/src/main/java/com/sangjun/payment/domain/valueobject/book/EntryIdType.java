package com.sangjun.payment.domain.valueobject.book;

import javax.persistence.Embeddable;

@Embeddable
public enum EntryIdType {
    UUID, LONG;

    public BookOwner createBookOwner(String id) {
        if (isUUID()) {
            return BookOwner.uuidOf(java.util.UUID.fromString(id));
        }

        if (isLong()) {
            return BookOwner.longOf(Long.valueOf(id));
        }

        throw new IllegalArgumentException(
                String.format("No supporting id type for id(%s)", id));
    }

    private boolean isUUID() {
        return this.name().equalsIgnoreCase("UUID");
    }

    private boolean isLong() {
        return this.name().equalsIgnoreCase("Long");
    }
}

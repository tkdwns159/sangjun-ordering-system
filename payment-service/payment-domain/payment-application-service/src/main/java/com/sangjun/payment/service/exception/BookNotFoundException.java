package com.sangjun.payment.service.exception;

import java.util.UUID;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(UUID bookShelveId, UUID ownerId) {
        super(String.format("Book not found for bookShelveId: %s, ownerId: %s", bookShelveId, ownerId));
    }
}

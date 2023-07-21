package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.book.Book;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository {

    Book save(Book book);

    Optional<Book> findByBookShelveIdAndBookOwner_uuid(UUID bookShelveId, UUID ownerUUID);
}

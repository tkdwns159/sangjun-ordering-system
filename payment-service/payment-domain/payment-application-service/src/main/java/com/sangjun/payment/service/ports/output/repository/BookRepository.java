package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.valueobject.book.BookId;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository {

    Book save(Book book);

    Optional<Book> findByBookShelveIdAndBookOwner_uuid(BookShelveId bookShelveId, UUID ownerUUID);

    Optional<Book> findById(BookId bookId);
}

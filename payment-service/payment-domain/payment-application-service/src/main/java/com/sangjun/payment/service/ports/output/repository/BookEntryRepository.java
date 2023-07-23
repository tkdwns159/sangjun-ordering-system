package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.book.BookEntry;
import com.sangjun.payment.domain.valueobject.book.BookId;

import java.util.Optional;

public interface BookEntryRepository {
    Optional<BookEntry> findLastByBookId(BookId bookId);
}

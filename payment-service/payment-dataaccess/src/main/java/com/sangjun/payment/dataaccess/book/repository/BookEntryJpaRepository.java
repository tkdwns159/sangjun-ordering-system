package com.sangjun.payment.dataaccess.book.repository;

import com.sangjun.payment.domain.entity.book.BookEntry;
import com.sangjun.payment.domain.valueobject.book.BookEntryId;
import com.sangjun.payment.service.ports.output.repository.BookEntryRepository;
import org.springframework.data.repository.Repository;

public interface BookEntryJpaRepository extends BookEntryRepository, Repository<BookEntry, BookEntryId> {
}

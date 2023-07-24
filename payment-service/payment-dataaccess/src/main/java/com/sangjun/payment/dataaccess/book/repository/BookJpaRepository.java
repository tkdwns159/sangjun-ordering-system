package com.sangjun.payment.dataaccess.book.repository;

import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.service.ports.output.repository.BookRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface BookJpaRepository extends BookRepository, Repository<Book, UUID> {
    @Override
    Book save(Book book);

    @Override
    Optional<Book> findByBookShelveIdAndBookOwner_uuid(BookShelveId bookShelveId, UUID ownerUuid);
}

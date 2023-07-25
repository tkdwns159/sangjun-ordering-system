package com.sangjun.payment.dataaccess.book.repository;

import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.service.ports.output.repository.BookShelveRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface BookShelveJpaRepository extends BookShelveRepository, Repository<BookShelve, BookShelveId> {

    @Override
    Optional<BookShelve> findById(BookShelveId bookShelveId);

    @Override
    BookShelve save(BookShelve bookShelve);
}

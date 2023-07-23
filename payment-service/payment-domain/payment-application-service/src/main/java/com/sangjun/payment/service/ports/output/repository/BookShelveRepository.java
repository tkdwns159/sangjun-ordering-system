package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;

import java.util.Optional;

public interface BookShelveRepository {

    Optional<BookShelve> findById(BookShelveId bookShelveId);

    BookShelve save(BookShelve bookShelve);

}

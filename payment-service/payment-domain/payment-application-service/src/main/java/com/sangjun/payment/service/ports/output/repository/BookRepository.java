package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.book.Book;

public interface BookRepository {

    Book save(Book book);

    Book findById(Long bookId);
}

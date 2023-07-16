package com.sangjun.payment.service.ports.input.service;

import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.exception.BookDomainException;
import com.sangjun.payment.service.ports.output.repository.BookRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookServiceHelper {

    public Book findBookById(BookRepository bookRepository, UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new BookDomainException("Book id: " + id + " not found"));
    }
}

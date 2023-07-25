package com.sangjun.payment.service.ports.input.service;

import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.service.ports.input.service.dto.BookCreateRequest;
import com.sangjun.payment.service.ports.output.repository.BookRepository;
import com.sangjun.payment.service.ports.output.repository.BookShelveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBookService {
    private final BookShelveRepository bookShelveRepository;
    private final BookRepository bookRepository;

    @Transactional
    public Book createBook(BookCreateRequest request) {
        BookShelve bookShelve = bookShelveRepository.findById(new BookShelveId(request.getShelveId())).get();

        Book book = Book.of(bookShelve, request.getOwnerId());
        return bookRepository.save(book);
    }
}

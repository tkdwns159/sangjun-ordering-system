package com.sangjun.payment.service.ports.input.service;

import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookEntry;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;
import com.sangjun.payment.service.ports.input.service.dto.BookEntryCreateRequest;
import com.sangjun.payment.service.ports.output.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddBookEntryService {
    private final BookRepository bookRepository;
    private final BookServiceHelper bookServiceHelper;

    @Transactional
    public BookEntry addBookEntry(BookEntryCreateRequest request) {
        Book book = bookServiceHelper.findBookById(bookRepository, request.getBookId());
        TransactionValue transactionValue =
                TransactionValue.of(request.getTransactionValueType(), request.getTransactionValueAmount());
        return book.addBookEntry(transactionValue, request.getDescription());
    }
}

package com.sangjun.payment.container;

import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import com.sangjun.payment.service.ports.output.repository.BookOwnerType;
import com.sangjun.payment.service.ports.output.repository.BookRepository;
import com.sangjun.payment.service.ports.output.repository.BookShelveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TestHelper {

    private final BookShelveRepository bookShelveRepository;
    private final BookRepository bookRepository;

    @Transactional
    public Book saveBook(String bookOwnerId, BookOwnerType bookOwnerType, EntryIdType entryIdType) {
        String bookShelveName = switch (bookOwnerType) {
            case CUSTOMER -> "customer";
            case RESTAURANT -> "restaurant";
            default -> "firm";
        };

        UUID bookShelveId = bookShelveRepository.findIdByOwnerType(bookOwnerType);
        BookShelve bookShelve = BookShelve.of(new BookShelveId(bookShelveId), bookShelveName, entryIdType);
        Book restaurantBook = Book.of(bookShelve, bookOwnerId);
        bookShelveRepository.save(bookShelve);
        return bookRepository.save(restaurantBook);
    }
}

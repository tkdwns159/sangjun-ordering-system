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
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class TestHelper {

    private final BookShelveRepository bookShelveRepository;
    private final BookRepository bookRepository;

    @PersistenceContext
    private final EntityManager em;


    public Book 식당_장부_생성(UUID id) {
        return saveBook(id.toString(), BookOwnerType.RESTAURANT, EntryIdType.UUID);
    }

    public Book 고객_장부_생성(UUID id) {
        return saveBook(id.toString(), BookOwnerType.CUSTOMER, EntryIdType.UUID);
    }

    public Book 회사_장부_생성(UUID id) {
        return saveBook(id.toString(), BookOwnerType.FIRM, EntryIdType.UUID);
    }

    public void 사전조건_반영() {
        em.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

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

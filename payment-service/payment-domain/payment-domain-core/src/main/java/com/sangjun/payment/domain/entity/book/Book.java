package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.utils.CommonConstants;
import com.sangjun.payment.domain.valueobject.book.*;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books", schema = "payment")
@Access(AccessType.FIELD)
public class Book extends AggregateRoot<BookId> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_shelve_id")
    private final BookShelve bookShelve;
    @Embedded
    private final BookOwner bookOwner;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private List<BookEntry> bookEntries;
    @Embedded
    private TotalBalance totalBalance;

    public Book(BookOwner owner, BookShelve bookShelve) {
        this.bookOwner = owner;
        this.bookShelve = bookShelve;
        this.bookEntries = new ArrayList<>();
        this.totalBalance = new TotalBalance();
    }

    public static Book from(BookShelve bookShelve, String bookOwnerId) {
        return new Book(Book.createBookOwner(bookShelve.getEntryIdType(), bookOwnerId), bookShelve);
    }

    public TotalBalance getTotalBalance() {
        return totalBalance;
    }

    public BookEntry addBookEntry(TransactionValue transactionValue,
                                  String desc) {
        BookEntry bookEntry = createBookEntry(transactionValue, desc);
        this.bookEntries.add(bookEntry);
        updateTotalBalance(bookEntry);

        return bookEntry;
    }

    private BookEntry createBookEntry(TransactionValue transactionValue, String desc) {
        BookEntry newBookEntry = new BookEntry(transactionValue,
                ZonedDateTime.now(ZoneId.of(CommonConstants.ZONE_ID)),
                desc);
        newBookEntry.validate();
        return newBookEntry;
    }

    private void updateTotalBalance(BookEntry bookEntry) {
        this.totalBalance = calculateTotalBalance(bookEntry.getTransactionValue());
    }

    private TotalBalance calculateTotalBalance(TransactionValue transactionValue) {
        return switch (transactionValue.getType()) {
            case CREDIT -> this.totalBalance.addCredit(transactionValue.getAmount());
            case DEBIT -> this.totalBalance.addDebit(transactionValue.getAmount());
        };
    }

    public static BookOwner createBookOwner(EntryIdType entryIdType, String id) {
        return entryIdType.createBookOwner(id);
    }
}

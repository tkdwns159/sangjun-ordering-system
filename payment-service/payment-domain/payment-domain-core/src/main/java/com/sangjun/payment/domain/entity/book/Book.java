package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.payment.domain.valueobject.book.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

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

    private Book(BookOwner owner, BookShelve bookShelve) {
        this.bookOwner = owner;
        this.bookShelve = bookShelve;
        this.bookEntries = new ArrayList<>();
        this.totalBalance = new TotalBalance();
    }

    public static Book of(BookShelve bookShelve, String bookOwnerId) {
        validate(bookShelve, bookOwnerId);
        return new Book(Book.createBookOwner(bookShelve.getEntryIdType(), bookOwnerId), bookShelve);
    }

    private static void validate(BookShelve bookShelve, String bookOwnerId) {
        requireNonNull(bookShelve, "bookShelve");
        requireNonNull(bookOwnerId, "bookOwnerId");
    }

    public BookEntry addBookEntry(TransactionValue transactionValue,
                                  String desc) {
        BookEntry bookEntry = createBookEntry(transactionValue, desc);
        this.bookEntries.add(bookEntry);
        updateTotalBalance(bookEntry);

        return bookEntry;
    }

    private BookEntry createBookEntry(TransactionValue transactionValue, String desc) {
        return BookEntry.of(transactionValue, desc);
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

    public TotalBalance getTotalBalance() {
        return this.totalBalance;
    }
}

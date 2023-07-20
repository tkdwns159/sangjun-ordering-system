package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.valueobject.book.*;

import javax.persistence.*;
import java.util.ArrayList;

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

    @Embedded
    private BookEntryList bookEntryList;
    @Embedded
    private TotalBalance totalBalance;

    private Book(BookOwner owner, BookShelve bookShelve) {
        this.bookOwner = owner;
        this.bookShelve = bookShelve;
        this.bookEntryList = new BookEntryList(new ArrayList<>());
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

    public void transact(Book to, Money amount, String fromDesc, String toDesc) {
        this.addBookEntry(TransactionValue.of(TransactionValueType.CREDIT, amount), fromDesc);
        to.addBookEntry(TransactionValue.of(TransactionValueType.DEBIT, amount), toDesc);
    }

    private BookEntry addBookEntry(TransactionValue transactionValue,
                                   String desc) {
        BookEntry bookEntry = createBookEntry(transactionValue, desc);
        this.bookEntryList.add(bookEntry);
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

    private static BookOwner createBookOwner(EntryIdType entryIdType, String id) {
        return entryIdType.createBookOwner(id);
    }

    public TotalBalance getTotalBalance() {
        return this.totalBalance;
    }

    public BookEntryList getBookEntryList() {
        return bookEntryList;
    }

}

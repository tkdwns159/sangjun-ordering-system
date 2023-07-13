package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.utils.CommonConstants;
import com.sangjun.payment.domain.valueobject.book.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Book extends AggregateRoot<BookId> {
    private BookShelve bookShelve;
    private BookOwner bookOwner;
    private List<BookEntry> bookEntries;
    private TotalBalance totalBalance;

    public Book(BookOwner owner, BookShelve bookShelve) {
        this.bookOwner = owner;
        this.bookShelve = bookShelve;
        this.bookEntries = new ArrayList<>();
        this.totalBalance = new TotalBalance();
    }

    public void addBookEntry(TransactionValueType transactionValueType,
                             Money amount,
                             String desc) {
        BookEntry bookEntry = createBookEntry(transactionValueType, amount, desc);
        this.bookEntries.add(bookEntry);
        updateTotalBalance(bookEntry);
    }

    private BookEntry createBookEntry(TransactionValueType transactionValueType, Money amount, String desc) {
        BookEntry newBookEntry = new BookEntry(transactionValueType,
                amount,
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
}

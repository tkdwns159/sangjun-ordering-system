package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.valueobject.book.BookEntryId;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;
import com.sangjun.payment.domain.valueobject.book.TransactionValueType;

import java.time.ZonedDateTime;
import java.util.Objects;

public class BookEntry extends BaseEntity<BookEntryId> {
    private TransactionValue transactionValue;
    private ZonedDateTime createdTime;
    private String description;

    public BookEntry(TransactionValueType transactionValueType,
                     Money amount,
                     ZonedDateTime createdTime,
                     String description) {
        this.transactionValue = new TransactionValue(transactionValueType, amount);
        this.createdTime = Objects.requireNonNull(createdTime, "createdTime must be non-null");
        this.description = Objects.requireNonNull(description, "description must be non-null");
    }

    public TransactionValue getTransactionValue() {
        return transactionValue;
    }

    public void validate() {
        this.transactionValue.validate();
    }
}


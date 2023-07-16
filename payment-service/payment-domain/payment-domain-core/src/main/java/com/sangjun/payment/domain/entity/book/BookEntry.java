package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.payment.domain.valueobject.book.BookEntryId;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "book_entry", schema = "payment")
@Access(AccessType.FIELD)
public class BookEntry extends BaseEntity<BookEntryId> {
    @Embedded
    private final TransactionValue transactionValue;
    private final ZonedDateTime createdTime;
    private final String description;

    public BookEntry(TransactionValue transactionValue,
                     ZonedDateTime createdTime,
                     String description) {
        this.transactionValue = Objects.requireNonNull(transactionValue, "transactionValue must be non-null");
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


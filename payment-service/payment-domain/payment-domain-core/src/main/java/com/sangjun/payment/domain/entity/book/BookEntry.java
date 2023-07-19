package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.payment.domain.valueobject.book.BookEntryId;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;
import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "book_entry", schema = "payment")
@Access(AccessType.FIELD)
public class BookEntry extends BaseEntity<BookEntryId> {
    @Embedded
    private final TransactionValue transactionValue;
    private final ZonedDateTime createdTime;
    private final String description;

    private BookEntry(TransactionValue transactionValue,
                      ZonedDateTime createdTime,
                      String description) {
        this.transactionValue = transactionValue;
        this.createdTime = createdTime;
        this.description = description;
    }

    public static BookEntry of(TransactionValue transactionValue, String description) {
        validate(transactionValue, description);
        return new BookEntry(transactionValue, ZonedDateTime.now(ZoneId.of(ZONE_ID)), description);
    }

    private static void validate(TransactionValue transactionValue,
                                 String description) {
        requireNonNull(transactionValue, "transactionValue");
        requireNonNull(description, "description");
    }

    public TransactionValue getTransactionValue() {
        return transactionValue;
    }
}


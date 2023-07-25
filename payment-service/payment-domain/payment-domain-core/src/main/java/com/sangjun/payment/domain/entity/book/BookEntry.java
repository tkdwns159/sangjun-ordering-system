package com.sangjun.payment.domain.entity.book;

import com.sangjun.payment.domain.valueobject.book.BookEntryId;
import com.sangjun.payment.domain.valueobject.book.BookId;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;
import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "book_entry", schema = "payment")
@Access(AccessType.FIELD)
public class BookEntry {
    @EmbeddedId
    @GenericGenerator(name = "book_entry_id_gen",
            strategy = "com.sangjun.payment.domain.entity.book.BookEntryIdGenerator",
            parameters = {
                    @Parameter(name = BookEntryIdGenerator.TYPE, value = "SEQUENCE"),
                    @Parameter(name = BookEntryIdGenerator.SEQUENCE_NAME, value = "payment.book_entry_id_seq")
            }
    )
    @GeneratedValue(generator = "book_entry_id_gen")
    private BookEntryId id;

    @Embedded
    private BookId bookId;
    @Embedded
    private TransactionValue transactionValue;
    private ZonedDateTime createdTime;
    private String description;

    private BookEntry(TransactionValue transactionValue,
                      ZonedDateTime createdTime,
                      String description) {
        this.transactionValue = transactionValue;
        this.createdTime = createdTime;
        this.description = description;
    }

    protected BookEntry() {
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


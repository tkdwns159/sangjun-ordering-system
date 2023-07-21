package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "book_shelve", schema = "payment")
@Access(AccessType.FIELD)
public class BookShelve extends BaseEntity<BookShelveId> {
    private String name;

    @Enumerated(EnumType.STRING)
    private EntryIdType entryIdType;

    private BookShelve(String name, EntryIdType entryIdType) {
        setId(new BookShelveId(UUID.randomUUID()));
        this.name = name;
        this.entryIdType = entryIdType;
    }

    protected BookShelve() {
    }

    public static BookShelve of(String name, EntryIdType entryIdType) {
        validate(name, entryIdType);
        return new BookShelve(name, entryIdType);
    }

    private static void validate(String name, EntryIdType entryIdType) {
        requireNonNull(name, "name");
        requireNonNull(entryIdType, "entryIdType");
        checkIfNameHasText(name);
    }

    private static void checkIfNameHasText(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("name must have text");
        }
    }

    public EntryIdType getEntryIdType() {
        return entryIdType;
    }
}

package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;

import javax.persistence.*;

@Entity
@Table(name = "book_shelve", schema = "payment")
@Access(AccessType.FIELD)
public class BookShelve extends BaseEntity<BookShelveId> {
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "entry_id_type"))
    private EntryIdType entryIdType;

    public EntryIdType getEntryIdType() {
        return entryIdType;
    }
}

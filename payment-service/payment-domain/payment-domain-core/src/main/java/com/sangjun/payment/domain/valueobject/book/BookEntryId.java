package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.common.domain.valueobject.BaseId;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@AttributeOverride(name = "value", column = @Column(name = "book_entry_id"))
public class BookEntryId extends BaseId<Long> {
    public BookEntryId(Long value) {
        super(value);
    }
}

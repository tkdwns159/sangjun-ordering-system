package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.common.domain.valueobject.BaseId;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@AttributeOverride(name = "value", column = @Column(name = "book_shelve_id"))
public class BookShelveId extends BaseId<UUID> {
    public BookShelveId(UUID value) {
        super(value);
    }

    protected BookShelveId() {
    }
}

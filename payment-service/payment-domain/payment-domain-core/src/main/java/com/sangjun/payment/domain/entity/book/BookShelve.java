package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.payment.domain.valueobject.book.IdType;

public class BookShelve extends BaseEntity<BookShelve> {
    private String name;
    private IdType idType;

    public IdType getIdType() {
        return idType;
    }
}

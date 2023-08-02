package com.sangjun.payment.domain.entity.book;

import com.sangjun.common.domain.entity.EmbeddedIdGenerator;
import com.sangjun.payment.domain.valueobject.book.BookEntryId;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.io.Serializable;


public class BookEntryIdGenerator extends EmbeddedIdGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) throws HibernateException {
        return new BookEntryId((Long) super.generate(session, obj));
    }
}

package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.book.BookShelve;

public interface BookShelveRepository {

    BookShelve findById(Long shelveId);

    BookShelve save(BookShelve bookShelve);

}

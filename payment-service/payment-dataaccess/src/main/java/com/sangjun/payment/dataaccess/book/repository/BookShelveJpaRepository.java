package com.sangjun.payment.dataaccess.book.repository;

import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.service.ports.output.repository.BookShelveRepository;
import org.springframework.data.repository.Repository;

public interface BookShelveJpaRepository extends BookShelveRepository, Repository<BookShelve, Long> {
    @Override
    BookShelve findById(Long shelveId);
}

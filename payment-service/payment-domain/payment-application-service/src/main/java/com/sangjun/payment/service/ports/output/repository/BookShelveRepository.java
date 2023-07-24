package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;

import java.util.Optional;
import java.util.UUID;

public interface BookShelveRepository {

    Optional<BookShelve> findById(BookShelveId bookShelveId);

    BookShelve save(BookShelve bookShelve);

    default UUID findIdByOwnerType(BookOwnerType bookOwnerType) {
        return UUID.fromString(switch (bookOwnerType) {
            case FIRM -> "35a3217a-11a1-45be-b09b-a27a63fdc156";
            case CUSTOMER -> "b25af285-c366-4ab1-b3e5-ce9ed054556e";
            case RESTAURANT -> "2fa223a3-fe5d-4dff-b3ca-8c2efeaa53e4";
        });
    }
}

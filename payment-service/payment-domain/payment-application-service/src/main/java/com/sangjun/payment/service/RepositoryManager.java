package com.sangjun.payment.service;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.exception.PaymentNotFoundException;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.service.exception.BookNotFoundException;
import com.sangjun.payment.service.ports.output.repository.BookOwnerType;
import com.sangjun.payment.service.ports.output.repository.BookRepository;
import com.sangjun.payment.service.ports.output.repository.BookShelveRepository;
import com.sangjun.payment.service.ports.output.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepositoryManager {
    private final BookRepository bookRepository;
    private final BookShelveRepository bookShelveRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Book getBook(BookOwnerType bookOwnerType, UUID bookOwnerId) {
        BookShelveId bookShelveId = new BookShelveId(bookShelveRepository.findIdByOwnerType(bookOwnerType));
        return bookRepository
                .findByBookShelveIdAndBookOwner_uuid(bookShelveId, bookOwnerId)
                .orElseThrow(() -> new BookNotFoundException(bookShelveId.getValue(), bookOwnerId));
    }

    public Payment getPayment(OrderId orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId.getValue()));
    }
}

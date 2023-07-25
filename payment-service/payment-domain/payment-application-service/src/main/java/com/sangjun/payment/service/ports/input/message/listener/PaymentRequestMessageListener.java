package com.sangjun.payment.service.ports.input.message.listener;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.payment.domain.PaymentCancelDomainService;
import com.sangjun.payment.domain.PaymentInitDomainService;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.event.PaymentCancelledEvent;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.event.PaymentFailedEvent;
import com.sangjun.payment.domain.ex.IllegalPaymentStateException;
import com.sangjun.payment.domain.exception.PaymentNotFoundException;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.exception.BookNotFoundException;
import com.sangjun.payment.service.ports.output.repository.BookOwnerType;
import com.sangjun.payment.service.ports.output.repository.BookRepository;
import com.sangjun.payment.service.ports.output.repository.BookShelveRepository;
import com.sangjun.payment.service.ports.output.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestMessageListener {
    private final PaymentEventShooter paymentEventShooter;
    private final PaymentInitDomainService paymentInitDomainService;
    private final PaymentCancelDomainService paymentCancelDomainService;
    private final BookRepository bookRepository;
    private final BookShelveRepository bookShelveRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void completePayment(PaymentRequest paymentRequest) {
        final Payment payment = PaymentRequestMapper.MAPPER.toPayment(paymentRequest);
        final Book customerBook = getBook(BookOwnerType.CUSTOMER, payment.getCustomerId().getValue());
        final Book restaurantBook = getBook(BookOwnerType.RESTAURANT, payment.getRestaurantId().getValue());

        final PaymentEvent paymentEvent = executePayment(payment, customerBook, restaurantBook);
        paymentRepository.save(paymentEvent.getPayment());
        paymentEventShooter.fire(paymentEvent);
    }

    private PaymentEvent executePayment(Payment payment, Book customerBook, Book restaurantBook) {
        try {
            return paymentInitDomainService.initPayment(payment.clone(), customerBook, restaurantBook);
        } catch (IllegalPaymentStateException ex) {
            payment.markAsFailed();
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
        }
    }

    Book getBook(BookOwnerType bookOwnerType, UUID bookOwnerId) {
        BookShelveId bookShelveId = new BookShelveId(bookShelveRepository.findIdByOwnerType(bookOwnerType));
        return bookRepository
                .findByBookShelveIdAndBookOwner_uuid(bookShelveId, bookOwnerId)
                .orElseThrow(() -> new BookNotFoundException(bookShelveId.getValue(), bookOwnerId));
    }

    @Transactional
    public void cancelPayment(PaymentRequest paymentRequest) {
        final OrderId orderId = new OrderId(UUID.fromString(paymentRequest.getOrderId()));
        final Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId.getValue()));
        final Book customerBook = getBook(BookOwnerType.CUSTOMER, payment.getCustomerId().getValue());
        final Book restaurantBook = getBook(BookOwnerType.RESTAURANT, payment.getRestaurantId().getValue());

        final PaymentCancelledEvent paymentCancelledEvent =
                paymentCancelDomainService.cancelPayment(payment, restaurantBook, customerBook);
        paymentRepository.save(paymentCancelledEvent.getPayment());
        paymentEventShooter.fire(paymentCancelledEvent);
    }
}

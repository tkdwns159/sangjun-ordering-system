package com.sangjun.payment.container;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookEntry;
import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import com.sangjun.payment.domain.valueobject.book.TransactionValueType;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.ports.input.message.listener.PaymentRequestMessageListener;
import com.sangjun.payment.service.ports.output.repository.BookEntryRepository;
import com.sangjun.payment.service.ports.output.repository.BookRepository;
import com.sangjun.payment.service.ports.output.repository.BookShelveRepository;
import com.sangjun.payment.service.ports.output.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@SpringBootTest(classes = PaymentRequestListenerTestConfig.class)
public class PaymentRequestListenerTest {

    @Autowired
    private PaymentRequestMessageListener paymentRequestMessageListener;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookShelveRepository bookShelveRepository;

    @Autowired
    private BookEntryRepository bookEntryRepository;

    @Test
    void 결제_성공() {
        // given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Money price = Money.of("1234");
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        RestaurantId restaurantId = new RestaurantId(UUID.randomUUID());

        BookShelve customerBookShelve =
                bookShelveRepository.save(BookShelve.of("customer", EntryIdType.UUID));
        BookShelve restaurantBookShelve =
                bookShelveRepository.save(BookShelve.of("restaurant", EntryIdType.UUID));

        Book customerBook = bookRepository.save(Book.of(customerBookShelve, customerId.getValue().toString()));
        Book restaurantBook = bookRepository.save(Book.of(restaurantBookShelve, restaurantId.getValue().toString()));

        // when
        paymentRequestMessageListener.completePayment(PaymentRequest.builder()
                .orderId(orderId.getValue().toString())
                .customerId(customerId.getValue().toString())
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .price(price.getAmount())
                .build());

        // then
        Payment newPayment = paymentRepository
                .findByOrderId(orderId)
                .get();

        assertThat(newPayment.getOrderId())
                .isEqualTo(orderId);
        assertThat(newPayment.getPaymentStatus())
                .isEqualTo(PaymentStatus.COMPLETED);
        assertThat(newPayment.getPrice())
                .isEqualTo(price);
        assertThat(newPayment.getCustomerId())
                .isEqualTo(customerId);

        Money currentCustomerBalance = customerBook.getTotalBalance().getCurrentBalance();
        Money priorCustomerBalance = currentCustomerBalance.add(newPayment.getPrice());
        assertThat(priorCustomerBalance)
                .isEqualTo(currentCustomerBalance.add(newPayment.getPrice()));

        BookEntry lastCustomerBookEntry = bookEntryRepository.findLastByBookId(customerBook.getId())
                .get();
        assertThat(lastCustomerBookEntry.getTransactionValue().getAmount())
                .isEqualTo(newPayment.getPrice());
        assertThat(lastCustomerBookEntry.getTransactionValue().getType())
                .isEqualTo(TransactionValueType.CREDIT);

        BookEntry lastRestaurantBookEntry = bookEntryRepository.findLastByBookId(restaurantBook.getId())
                .get();
        assertThat(lastRestaurantBookEntry.getTransactionValue().getAmount())
                .isEqualTo(newPayment.getPrice());
        assertThat(lastRestaurantBookEntry.getTransactionValue().getType())
                .isEqualTo(TransactionValueType.DEBIT);
    }
}

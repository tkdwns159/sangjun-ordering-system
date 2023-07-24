package com.sangjun.payment.container;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookEntry;
import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;
import com.sangjun.payment.domain.valueobject.book.TransactionValueType;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.ports.input.message.listener.PaymentRequestMessageListener;
import com.sangjun.payment.service.ports.output.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager em;

    @Test
    void 결제_성공() {
        // given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Money price = Money.of("1234");
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        RestaurantId restaurantId = new RestaurantId(UUID.randomUUID());

        UUID customerBookShelveId = bookShelveRepository.findIdByOwnerType(BookOwnerType.CUSTOMER);
        UUID restaurantBookShelveId = bookShelveRepository.findIdByOwnerType(BookOwnerType.RESTAURANT);
        UUID firmBookShelveId = bookShelveRepository.findIdByOwnerType(BookOwnerType.FIRM);

        BookShelve customerBookShelve = getBookShelve(customerBookShelveId, "customer");
        BookShelve restaurantBookShelve = getBookShelve(restaurantBookShelveId, "restaurant");
        BookShelve firmBookShelve = getBookShelve(firmBookShelveId, "firm");

        Book customerBook = bookRepository.save(Book.of(customerBookShelve, customerId.getValue().toString()));
        Book restaurantBook = bookRepository.save(Book.of(restaurantBookShelve, restaurantId.getValue().toString()));
        Book firmBook = bookRepository.save(Book.of(firmBookShelve, UUID.randomUUID().toString()));

        firmBook.transact(customerBook, Money.of("1000000"), "", "");

        Money priorCustomerBookBalance = customerBook.getTotalBalance().getCurrentBalance();
        Money priorRestaurantBookBalance = restaurantBook.getTotalBalance().getCurrentBalance();

        // when
        paymentRequestMessageListener.completePayment(PaymentRequest.builder()
                .orderId(orderId.getValue().toString())
                .customerId(customerId.getValue().toString())
                .restaurantId(restaurantId.getValue().toString())
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .price(price.getAmount())
                .build());

        // then
        Payment newPayment = paymentRepository
                .findByOrderId(orderId)
                .get();

        주어진_결제정보와_저장된_결제정보_비교(newPayment, orderId, price, customerId, restaurantId);
        장부_총액_변화_확인(customerBook,
                priorCustomerBookBalance,
                TransactionValue.of(TransactionValueType.CREDIT, newPayment.getPrice()));
        장부_총액_변화_확인(restaurantBook,
                priorRestaurantBookBalance,
                TransactionValue.of(TransactionValueType.DEBIT, newPayment.getPrice()));
        마지막으로_추가된_장부_항목_확인(customerBook, newPayment, TransactionValueType.CREDIT);
        마지막으로_추가된_장부_항목_확인(restaurantBook, newPayment, TransactionValueType.DEBIT);
    }

    private BookShelve getBookShelve(UUID customerBookShelveId, String name) {
        return bookShelveRepository.save(BookShelve.of(
                new BookShelveId(customerBookShelveId),
                name,
                EntryIdType.UUID));
    }

    private void 주어진_결제정보와_저장된_결제정보_비교(Payment newPayment,
                                       OrderId orderId,
                                       Money price,
                                       CustomerId customerId,
                                       RestaurantId restaurantId) {
        assertThat(newPayment.getOrderId())
                .isEqualTo(orderId);
        assertThat(newPayment.getCustomerId())
                .isEqualTo(customerId);
        assertThat(newPayment.getRestaurantId())
                .isEqualTo(restaurantId);
        assertThat(newPayment.getPaymentStatus())
                .isEqualTo(PaymentStatus.COMPLETED);
        assertThat(newPayment.getPrice())
                .isEqualTo(price);
        assertThat(newPayment.getCreatedAt())
                .isNotNull();
    }

    private void 장부_총액_변화_확인(Book book, Money priorBalance, TransactionValue tv) {
        Money currentBalance = book.getTotalBalance().getCurrentBalance();
        Money expectedBalance = tv.getType() == TransactionValueType.DEBIT ?
                priorBalance.add(tv.getAmount()) : priorBalance.subtract(tv.getAmount());
        assertThat(currentBalance)
                .isEqualTo(expectedBalance);
    }

    private void 마지막으로_추가된_장부_항목_확인(Book restaurantBook, Payment newPayment, TransactionValueType tvType) {
        BookEntry lastRestaurantBookEntry = bookEntryRepository.findTopByBookIdOrderByCreatedTimeDesc(restaurantBook.getId())
                .get();
        assertThat(lastRestaurantBookEntry.getTransactionValue())
                .isEqualTo(TransactionValue.of(tvType, newPayment.getPrice()));
    }
}

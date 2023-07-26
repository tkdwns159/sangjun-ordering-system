package com.sangjun.payment.container;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookEntry;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.valueobject.book.BookId;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;
import com.sangjun.payment.domain.valueobject.book.TransactionValueType;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.ports.input.message.listener.PaymentRequestMessageListener;
import com.sangjun.payment.service.ports.output.repository.BookEntryRepository;
import com.sangjun.payment.service.ports.output.repository.BookRepository;
import com.sangjun.payment.service.ports.output.repository.BookShelveRepository;
import com.sangjun.payment.service.ports.output.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@SpringBootTest(classes = PaymentRequestListenerTestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PaymentRequestListenerTest {
    private static final OrderId orderId = new OrderId(UUID.randomUUID());
    private static final CustomerId customerId = new CustomerId(UUID.randomUUID());
    private static final RestaurantId restaurantId = new RestaurantId(UUID.randomUUID());

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

    @Autowired
    private TestHelper testHelper;

    @PersistenceContext
    private EntityManager em;

    @BeforeEach
    void cleanUp() {
        // transaction 자동시작
        TestTransaction.flagForCommit();
        truncateAllTables();
        TestTransaction.end();

        // 다음 수행할 함수를 위해 transaction 재시작
        TestTransaction.start();
    }

    private void truncateAllTables() {
        Query query = em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE");
        query.executeUpdate();

        query = em.createNativeQuery(
                "SELECT 'TRUNCATE TABLE ' || TABLE_SCHEMA || '.' || TABLE_NAME || ';' FROM INFORMATION_SCHEMA.TABLES WHERE " +
                        "TABLE_SCHEMA in ('restaurant', 'payment')");
        List<String> statements = query.getResultList();

        for (String statement : statements) {
            query = em.createNativeQuery(statement);
            query.executeUpdate();
        }

        query = em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE");
        query.executeUpdate();
    }

    @Test
    void 결제_성공() {
        // given
        Money price = Money.of("1234");
        Book customerBook = testHelper.고객_장부_생성(customerId.getValue());
        Book restaurantBook = testHelper.식당_장부_생성(restaurantId.getValue());
        Book firmBook = testHelper.회사_장부_생성(UUID.randomUUID());

        고객에게_충전금_부여(customerBook, firmBook);
        testHelper.사전조건_반영();

        // when
        paymentRequestMessageListener.completePayment(PaymentRequest.builder()
                .orderId(orderId.getValue().toString())
                .customerId(customerId.getValue().toString())
                .restaurantId(restaurantId.getValue().toString())
                .createdAt(Instant.now())
                .price(price.getAmount())
                .build());

        // then
        Payment foundPayment = paymentRepository
                .findByOrderId(orderId)
                .get();

        final Money priorCustomerBookBalance = customerBook.getTotalBalance().getCurrentBalance();
        final Money priorRestaurantBookBalance = restaurantBook.getTotalBalance().getCurrentBalance();
        final Money currentCustomerBookBalance = priorCustomerBookBalance.subtract(price);
        final Money currentRestaurantBookBalance = priorRestaurantBookBalance.add(price);

        final TransactionValue customerTransactionValue =
                TransactionValue.of(TransactionValueType.CREDIT, foundPayment.getPrice());
        final TransactionValue restaurantTransactionValue =
                TransactionValue.of(TransactionValueType.DEBIT, foundPayment.getPrice());

        결제정보_확인(foundPayment, price, PaymentStatus.COMPLETED);
        장부_총액_변화_확인(customerBook.getId(), currentCustomerBookBalance);
        장부_총액_변화_확인(restaurantBook.getId(), currentRestaurantBookBalance);
        마지막으로_추가된_장부_항목_확인(customerBook, customerTransactionValue);
        마지막으로_추가된_장부_항목_확인(restaurantBook, restaurantTransactionValue);
    }

    private void 고객에게_충전금_부여(Book customerBook, Book firmBook) {
        firmBook.transact(customerBook, Money.of("1000000"), "", "");
    }

    private void 결제정보_확인(Payment newPayment,
                         Money price,
                         PaymentStatus paymentStatus) {
        assertThat(newPayment.getOrderId())
                .isEqualTo(orderId);
        assertThat(newPayment.getCustomerId())
                .isEqualTo(customerId);
        assertThat(newPayment.getRestaurantId())
                .isEqualTo(restaurantId);
        assertThat(newPayment.getPaymentStatus())
                .isEqualTo(paymentStatus);
        assertThat(newPayment.getPrice())
                .isEqualTo(price);
        assertThat(newPayment.getCreatedAt())
                .isNotNull();

    }

    private void 장부_총액_변화_확인(BookId bookId, Money expectedBalance) {
        Book book = bookRepository.findById(bookId).get();
        Money currentBalance = book.getTotalBalance().getCurrentBalance();
        assertThat(currentBalance)
                .isEqualTo(expectedBalance);
    }

    private void 마지막으로_추가된_장부_항목_확인(Book restaurantBook, TransactionValue tv) {
        BookEntry lastRestaurantBookEntry = bookEntryRepository
                .findTopByBookIdOrderByCreatedTimeDesc(restaurantBook.getId())
                .get();

        assertThat(lastRestaurantBookEntry.getTransactionValue())
                .isEqualTo(tv);
    }

    @Test
    void 결제_실패() {
        //given
        Money price = Money.of("1234");
        Book customerBook = testHelper.고객_장부_생성(customerId.getValue());
        Book restaurantBook = testHelper.식당_장부_생성(restaurantId.getValue());
        testHelper.회사_장부_생성(UUID.randomUUID());
        testHelper.사전조건_반영();

        //when
        paymentRequestMessageListener.completePayment(PaymentRequest.builder()
                .orderId(orderId.getValue().toString())
                .customerId(customerId.getValue().toString())
                .restaurantId(restaurantId.getValue().toString())
                .createdAt(Instant.now())
                .price(price.getAmount())
                .build());

        //then
        Payment foundPayment = paymentRepository.findByOrderId(orderId).get();
        결제정보_확인(foundPayment, price, PaymentStatus.FAILED);
        장부에_기록되면_안됨(customerBook, restaurantBook);
    }

    private void 장부에_기록되면_안됨(Book customerBook, Book restaurantBook) {
        Optional<BookEntry> foundCustomerBookEntry =
                bookEntryRepository.findTopByBookIdOrderByCreatedTimeDesc(customerBook.getId());
        assertThat(foundCustomerBookEntry.isEmpty())
                .isTrue();
        Optional<BookEntry> foundRestaurantBookEntry =
                bookEntryRepository.findTopByBookIdOrderByCreatedTimeDesc(restaurantBook.getId());
        assertThat(foundRestaurantBookEntry.isEmpty())
                .isTrue();
    }

    @Test
    void 결제_취소() {
        //given
        Money price = Money.of("1234");
        Book restaurantBook = testHelper.식당_장부_생성(restaurantId.getValue());
        Book customerBook = testHelper.고객_장부_생성(customerId.getValue());
        saveCompletedPayment(price);
        testHelper.사전조건_반영();

        //when
        paymentRequestMessageListener.cancelPayment(PaymentRequest.builder()
                .orderId(orderId.getValue().toString())
                .customerId(customerId.getValue().toString())
                .restaurantId(restaurantId.getValue().toString())
                .createdAt(Instant.now())
                .price(price.getAmount())
                .build());

        //then
        Payment foundPayment = paymentRepository.findByOrderId(orderId).get();

        final Money priorCustomerBookBalance = customerBook.getTotalBalance().getCurrentBalance();
        final Money priorRestaurantBookBalance = restaurantBook.getTotalBalance().getCurrentBalance();
        final Money currentCustomerBookBalance = priorCustomerBookBalance.add(price);
        final Money currentRestaurantBookBalance = priorRestaurantBookBalance.subtract(price);

        결제정보_확인(foundPayment, price, PaymentStatus.CANCELLED);
        장부_총액_변화_확인(customerBook.getId(), currentCustomerBookBalance);
        장부_총액_변화_확인(restaurantBook.getId(), currentRestaurantBookBalance);
        마지막으로_추가된_장부_항목_확인(restaurantBook, TransactionValue.of(TransactionValueType.CREDIT, foundPayment.getPrice()));
        마지막으로_추가된_장부_항목_확인(customerBook, TransactionValue.of(TransactionValueType.DEBIT, foundPayment.getPrice()));
    }

    private void saveCompletedPayment(Money price) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .restaurantId(restaurantId)
                .customerId(customerId)
                .price(price)
                .build();
        payment.initialize();
        payment.complete();
        paymentRepository.save(payment);
    }
}

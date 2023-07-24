package com.sangjun.payment.container;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookEntry;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;
import com.sangjun.payment.domain.valueobject.book.TransactionValueType;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.ports.input.message.listener.PaymentRequestMessageListener;
import com.sangjun.payment.service.ports.output.repository.*;
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
@SpringBootTest(classes = PaymentRequestListenerTestConfig.class)
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

        Book customerBook = testHelper.saveBook(customerId.getValue().toString(), BookOwnerType.CUSTOMER, EntryIdType.UUID);
        Book restaurantBook = testHelper.saveBook(restaurantId.getValue().toString(), BookOwnerType.RESTAURANT, EntryIdType.UUID);
        Book firmBook = testHelper.saveBook(UUID.randomUUID().toString(), BookOwnerType.FIRM, EntryIdType.UUID);

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
        Payment foundPayment = paymentRepository
                .findByOrderId(orderId)
                .get();

        결제정보_확인(foundPayment, price, PaymentStatus.COMPLETED);
        장부_총액_변화_확인(customerBook,
                priorCustomerBookBalance,
                TransactionValue.of(TransactionValueType.CREDIT, foundPayment.getPrice()));
        장부_총액_변화_확인(restaurantBook,
                priorRestaurantBookBalance,
                TransactionValue.of(TransactionValueType.DEBIT, foundPayment.getPrice()));
        마지막으로_추가된_장부_항목_확인(customerBook, foundPayment, TransactionValueType.CREDIT);
        마지막으로_추가된_장부_항목_확인(restaurantBook, foundPayment, TransactionValueType.DEBIT);
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

    @Test
    void 결제_실패() {
        //given
        Book customerBook = testHelper.saveBook(customerId.getValue().toString(), BookOwnerType.CUSTOMER, EntryIdType.UUID);
        Book restaurantBook = testHelper.saveBook(restaurantId.getValue().toString(), BookOwnerType.RESTAURANT, EntryIdType.UUID);
        Book firmBook = testHelper.saveBook(UUID.randomUUID().toString(), BookOwnerType.FIRM, EntryIdType.UUID);
        Money price = Money.of("1234");

        //when
        paymentRequestMessageListener.completePayment(PaymentRequest.builder()
                .orderId(orderId.getValue().toString())
                .customerId(customerId.getValue().toString())
                .restaurantId(restaurantId.getValue().toString())
                .paymentStatus(PaymentStatus.PENDING)
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


}

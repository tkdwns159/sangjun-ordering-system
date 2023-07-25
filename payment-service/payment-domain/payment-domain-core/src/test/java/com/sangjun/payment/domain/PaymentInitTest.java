package com.sangjun.payment.domain;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.exception.IllegalPaymentStateException;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentInitTest {

    private static final BookShelve FIRM_BOOK_SHELVE = BookShelve.of("firm", EntryIdType.UUID);
    private static final UUID FIRM_BOOK_ID = UUID.randomUUID();
    private static final Book FIRM_BOOK = Book.of(FIRM_BOOK_SHELVE, FIRM_BOOK_ID.toString());

    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final Money PRICE = Money.of("10000");

    private static final BookShelve CUSTOMER_BOOK_SHELVE = BookShelve.of("customer", EntryIdType.UUID);
    private static final UUID CUSTOMER_BOOK_ID = UUID.randomUUID();

    private static final BookShelve RESTAURANT_BOOK_SHELVE = BookShelve.of("restaurant", EntryIdType.UUID);
    private static final UUID RESTAURANT_BOOK_ID = UUID.randomUUID();
    private Book restaurantBook;
    private Book customerBook;
    private Payment payment;

    private final PaymentInitDomainService paymentInitDomainService = new PaymentInitDomainService();

    @BeforeEach
    void init() {
        customerBook = Book.of(CUSTOMER_BOOK_SHELVE, CUSTOMER_BOOK_ID.toString());
        restaurantBook = Book.of(RESTAURANT_BOOK_SHELVE, RESTAURANT_BOOK_ID.toString());
        payment = Payment.builder()
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .orderId(new OrderId(ORDER_ID))
                .customerId(new CustomerId(CUSTOMER_ID))
                .price(PRICE)
                .build();
    }

    @Test
    void customerBookBalanceMustBeGreaterThanPaymentPrice() {
        // given
        FIRM_BOOK.transact(customerBook, Money.of("9999"), "", "");

        // when, then
        assertThatThrownBy(() ->
                paymentInitDomainService.initPayment(payment, customerBook, restaurantBook))
                .isInstanceOf(IllegalPaymentStateException.class);
    }

    @Test
    void initializePayment() {
        //given
        FIRM_BOOK.transact(customerBook, Money.of("100000"), "", "");

        //when
        PaymentEvent paymentEvent = paymentInitDomainService.initPayment(payment, customerBook, restaurantBook);

        //then
        assertThat(paymentEvent.getPayment().getId())
                .isNotNull();
    }

    @Test
    void subtractOnCustomerAccount() {
        //given
        FIRM_BOOK.transact(customerBook, Money.of("10001"), "", "");

        //when
        paymentInitDomainService.initPayment(payment, customerBook, restaurantBook);

        //then
        assertThat(customerBook.getTotalBalance().getCurrentBalance())
                .isEqualTo(Money.of("1"));
    }

    @Test
    void addOnRestaurantAccount() {
        //given
        FIRM_BOOK.transact(customerBook, Money.of("10001"), "", "");

        //when
        paymentInitDomainService.initPayment(payment, customerBook, restaurantBook);

        //then
        assertThat(restaurantBook.getTotalBalance().getCurrentBalance())
                .isEqualTo(Money.of("10000"));
    }

    @Test
    void markAsCompleted() {
        //given
        FIRM_BOOK.transact(customerBook, Money.of("10001"), "", "");

        //when
        paymentInitDomainService.initPayment(payment, customerBook, restaurantBook);

        //then
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.COMPLETED);
    }

}

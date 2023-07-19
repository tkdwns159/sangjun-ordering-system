package com.sangjun.payment.domain;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentInitTest {

    private final PaymentInitService paymentInitService = new PaymentInitService();
    private static final BookShelve FIRM_BOOK_SHELVE = BookShelve.of("firm", EntryIdType.UUID);
    private static final UUID FIRM_BOOK_ID = UUID.randomUUID();
    private static final Book FIRM_BOOK = Book.of(FIRM_BOOK_SHELVE, FIRM_BOOK_ID.toString());

    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final Money PRICE = Money.of("10000");
    private static final Payment PAYMENT = Payment.builder()
            .restaurantId(new RestaurantId(RESTAURANT_ID))
            .orderId(new OrderId(ORDER_ID))
            .customerId(new CustomerId(CUSTOMER_ID))
            .price(PRICE)
            .build();
    private static final BookShelve CUSTOMER_BOOK_SHELVE = BookShelve.of("customer", EntryIdType.UUID);
    private static final UUID CUSTOMER_BOOK_ID = UUID.randomUUID();

    private static final BookShelve RESTAURANT_BOOK_SHELVE = BookShelve.of("restaurant", EntryIdType.UUID);
    private static final UUID RESTAURANT_BOOK_ID = UUID.randomUUID();
    private Book restaurantBook;
    private Book customerBook;

    @BeforeEach
    void init() {
        Book dummy = Book.of(FIRM_BOOK_SHELVE, UUID.randomUUID().toString());
        FIRM_BOOK.transact(dummy, Money.of("100000000"), "", "");
        customerBook = Book.of(CUSTOMER_BOOK_SHELVE, CUSTOMER_BOOK_ID.toString());
        restaurantBook = Book.of(RESTAURANT_BOOK_SHELVE, RESTAURANT_BOOK_ID.toString());
    }

    @Test
    void customerBookBalanceMustBeGreaterThanPaymentPrice() {
        // given
        FIRM_BOOK.transact(customerBook, Money.of("9999"), "", "");

        // when, then
        assertThatThrownBy(() ->
                paymentInitService.initPayment(PAYMENT, customerBook, restaurantBook))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void initializePayment() {
        //given
        FIRM_BOOK.transact(customerBook, Money.of("100000"), "", "");


        //when
        PaymentEvent paymentEvent = paymentInitService.initPayment(PAYMENT, customerBook, restaurantBook);

        //then
        assertThat(paymentEvent.getPayment().getId())
                .isNotNull();
    }

    @Test
    void subtractOnCustomerAccount() {
        //given
        FIRM_BOOK.transact(customerBook, Money.of("10001"), "", "");

        //when
        paymentInitService.initPayment(PAYMENT, customerBook, restaurantBook);

        //then
        assertThat(customerBook.getTotalBalance().getCurrentBalance())
                .isEqualTo(Money.of("1"));
    }

    @Test
    void addOnRestaurantAccount() {
        //given
        FIRM_BOOK.transact(customerBook, Money.of("10001"), "", "");

        //when
        paymentInitService.initPayment(PAYMENT, customerBook, restaurantBook);

        //then
        assertThat(restaurantBook.getTotalBalance().getCurrentBalance())
                .isEqualTo(Money.of("10000"));
    }
}

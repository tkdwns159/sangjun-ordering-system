package com.sangjun.payment.domain;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentCancelTest {
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

    private final PaymentCancelDomainService paymentCancelDomainService = new PaymentCancelDomainService();

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
    void markAsCancelled() {
        // given
        FIRM_BOOK.transact(customerBook, Money.of("10000"), "", "");
        paymentInitDomainService.initPayment(payment, customerBook, restaurantBook);

        //when
        paymentCancelDomainService.cancelPayment(payment, restaurantBook, customerBook);

        //then
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void addRollbackEntryToBooks() {
        // given
        FIRM_BOOK.transact(customerBook, Money.of("10000"), "", "");
        paymentInitDomainService.initPayment(payment, customerBook, restaurantBook);
        assertThat(customerBook.getBookEntryList().getSize())
                .isEqualTo(2);

        // when
        paymentCancelDomainService.cancelPayment(payment, restaurantBook, customerBook);

        // then
        assertThat(customerBook.getBookEntryList().getSize())
                .isEqualTo(3);
        assertThat(restaurantBook.getBookEntryList().getSize())
                .isEqualTo(2);
    }

    @Test
    void changeBookTotalBalance() {
        // given
        FIRM_BOOK.transact(customerBook, Money.of("10000"), "", "");
        Money initialCustomerBalance = customerBook.getTotalBalance().getCurrentBalance();
        Money initialRestaurantBalance = restaurantBook.getTotalBalance().getCurrentBalance();

        paymentInitDomainService.initPayment(payment, customerBook, restaurantBook);
        Money expectedCustomerBalance = Money.of("10000").subtract(payment.getPrice());
        Money expectedRestaurantBalance = payment.getPrice();
        assertThat(customerBook.getTotalBalance().getCurrentBalance())
                .isEqualTo(expectedCustomerBalance);
        assertThat(restaurantBook.getTotalBalance().getCurrentBalance())
                .isEqualTo(expectedRestaurantBalance);

        // when
        paymentCancelDomainService.cancelPayment(payment, restaurantBook, customerBook);

        // then
        assertThat(customerBook.getTotalBalance().getCurrentBalance())
                .isEqualTo(initialCustomerBalance);
        assertThat(restaurantBook.getTotalBalance().getCurrentBalance())
                .isEqualTo(initialRestaurantBalance);
    }

}

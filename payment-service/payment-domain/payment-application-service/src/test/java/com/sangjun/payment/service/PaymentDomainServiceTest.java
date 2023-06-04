package com.sangjun.payment.service;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.PaymentStatus;
import com.sangjun.payment.domain.PaymentDomainService;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.Payment;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.valueobject.CreditEntryId;
import com.sangjun.payment.domain.valueobject.CreditHistoryId;
import com.sangjun.payment.domain.valueobject.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PaymentDomainServiceConfig.class)
public class PaymentDomainServiceTest {
    private final UUID ORDER_ID = UUID.randomUUID();
    private final UUID CUSTOMER_ID = UUID.randomUUID();

    @Autowired
    private PaymentDomainService paymentDomainService;

    @Test
    void failure_message_exists_after_init_validation_when_price_is_null() {
        //given
        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        null)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2000")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.initiatePayment(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);
        //then
        assertThat(failureMessages.contains("Total price must be greater than zero!"))
                .isTrue();
    }

    @Test
    void failure_message_exists_after_init_validation_when_price_is_zero() {
        //given
        Money price = Money.of(BigDecimal.ZERO);

        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2000")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.initiatePayment(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(failureMessages.contains("Total price must be greater than zero!"))
                .isTrue();
    }

    @Test
    void failure_message_exists_after_init_validation_when_current_credit_is_less_than_order_price() {
        //given
        Money price = Money.of(new BigDecimal("3000"));

        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2000")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.initiatePayment(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);
        //then
        assertThat(failureMessages.contains("Customer with id =" + payment.getCustomerId().getValue() +
                " doesn't have enough credit for payment!"))
                .isTrue();
    }

    @Test
    void subtract_credit_entry_after_init_validation() {
        //given
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.initiatePayment(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(creditEntry.getTotalCreditAmount())
                .isEqualTo(Money.of(new BigDecimal("500")));
    }

    @Test
    void failure_message_exists_after_init_validation_when_credit_history_sum_not_equals_current_credit() {
        //given
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("2300")),
                        TransactionType.DEBIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.initiatePayment(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(failureMessages.contains("Credit history total is not equal to current credit for customer id=" +
                creditEntry.getCustomerId().getValue()))
                .isTrue();
    }

    @Test
    void failure_message_exists_after_init_validation_when_credit_history_sum_is_not_greater_than_zero() {
        //given
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("1000")),
                        TransactionType.DEBIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.initiatePayment(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(failureMessages.contains("Customer with id=" + creditEntry.getCustomerId().getValue() +
                " doesn't have enough credit according to credit history"))
                .isTrue();
    }

    @Test
    void payment_event_contains_payment_when_successfully_initiated_payment() {
        //given
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.DEBIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        PaymentEvent paymentEvent = paymentDomainService.initiatePayment(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(paymentEvent.getPayment())
                .isEqualTo(payment);
    }

    @Test
    void failure_messages_is_empty_when_successfully_initiated_payment() {
        //given
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.DEBIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.initiatePayment(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(failureMessages.isEmpty())
                .isTrue();
    }

    @Test
    void failure_messages_exists_after_cancel_payment_when_price_is_null() {
        //given
        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        null)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.cancelPayment(payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(failureMessages.isEmpty())
                .isFalse();
    }

    @Test
    void failure_messages_exists_after_cancel_payment_when_price_is_Zero() {
        //given
        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        Money.ZERO)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.cancelPayment(payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(failureMessages.isEmpty())
                .isFalse();
    }

    @Test
    void add_credit_entry_amount_after_cancel_payment() {
        //given
        Money price = Money.of(new BigDecimal(1000));

        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.cancelPayment(payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(creditEntry.getTotalCreditAmount())
                .isEqualTo(Money.of(new BigDecimal(3500)));

    }

    @Test
    void add_credit_history_after_cancel_payment() {
        //given
        Money price = Money.of(new BigDecimal(1000));

        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.cancelPayment(payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(creditHistoryList.stream()
                .map(CreditHistory::getAmount)
                .reduce(Money.ZERO, Money::add))
                .isEqualTo(Money.of(new BigDecimal(3500)));
    }

    @Test
    void payment_status_becomes_FAILED_after_payment_cancel_fails() {
        //given
        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        Money.ZERO)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.cancelPayment(payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void payment_status_becomes_CANCELLED_after_payment_cancel_success() {
        //given
        Payment payment = Payment.builder(
                        new OrderId(ORDER_ID),
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal(700)))
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.cancelPayment(payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        //then
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.CANCELLED);
    }
}

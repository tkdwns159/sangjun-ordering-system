package com.sangjun.payment.service;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.payment.domain.PaymentDomainService;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.Payment;
import com.sangjun.payment.domain.event.PaymentCompletedEvent;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.event.PaymentFailedEvent;
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

    @Autowired
    private PaymentDomainService paymentDomainService;

    @Autowired
    private DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher;

    @Autowired
    private DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventDomainEventPublisher;

    @Test
    void contextLoads() {
    }

    @Test
    void add_failure_message_init_validation_when_price_is_null() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        null)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2000")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.validatePaymentInitiation(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);
        //then
        assertThat(failureMessages.contains("Total price must be greater than zero!"))
                .isTrue();
    }

    @Test
    void add_failure_message_init_validation_when_price_is_zero() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(BigDecimal.ZERO);

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2000")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.validatePaymentInitiation(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);
        //then
        assertThat(failureMessages.contains("Total price must be greater than zero!"))
                .isTrue();
    }

    @Test
    void add_failure_message_init_validation_when_current_credit_is_less_than_order_price() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("3000"));

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2000")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.validatePaymentInitiation(
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
    void subtract_credit_entry_init_validation() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.validatePaymentInitiation(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);
        //then
        assertThat(creditEntry.getTotalCreditAmount())
                .isEqualTo(Money.of(new BigDecimal("500")));
    }

    @Test
    void add_failure_message_init_validation_when_credit_history_sum_not_equals_current_credit() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(customerId),
                        Money.of(new BigDecimal("2300")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.validatePaymentInitiation(
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
    void add_failure_message_init_validation_when_credit_history_sum_is_not_greater_than_zero() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(customerId),
                        Money.of(new BigDecimal("1000")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.validatePaymentInitiation(
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
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(customerId),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.validatePaymentInitiation(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        PaymentEvent paymentEvent = paymentDomainService.initiatePayment(payment,
                failureMessages,
                paymentCompletedEventDomainEventPublisher,
                paymentFailedEventDomainEventPublisher);

        //then
        assertThat(paymentEvent.getPayment())
                .isEqualTo(payment);
    }

    @Test
    void failure_messages_is_empty_when_successfully_initiated_payment() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(customerId),
                        Money.of(new BigDecimal("2500")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.validatePaymentInitiation(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        PaymentEvent paymentEvent = paymentDomainService.initiatePayment(payment,
                failureMessages,
                paymentCompletedEventDomainEventPublisher,
                paymentFailedEventDomainEventPublisher);

        //then
        assertThat(paymentEvent.getFailureMessages().isEmpty())
                .isTrue();
    }

    @Test
    void fail_payment_initiation_when_not_tried_validation() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        List<String> failureMessages = new ArrayList<>();

        //when
        PaymentEvent paymentEvent = paymentDomainService.initiatePayment(payment,
                failureMessages,
                paymentCompletedEventDomainEventPublisher,
                paymentFailedEventDomainEventPublisher);

        //then
        assertThat(paymentEvent)
                .isInstanceOf(PaymentFailedEvent.class);
    }

    @Test
    void paymentEvent_contains_payment_when_not_tried_validation() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        List<String> failureMessages = new ArrayList<>();

        //when
        PaymentEvent paymentEvent = paymentDomainService.initiatePayment(payment,
                failureMessages,
                paymentCompletedEventDomainEventPublisher,
                paymentFailedEventDomainEventPublisher);

        //then
        assertThat(paymentEvent.getPayment())
                .isEqualTo(payment);
    }

    @Test
    void failure_message_is_not_empty_when_validation_failed() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("2000"));

        Payment payment = Payment.builder(
                        new OrderId(orderId),
                        new CustomerId(customerId),
                        price)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("2500")))
                .build();

        List<CreditHistory> creditHistoryList = new ArrayList<>();
        creditHistoryList.add(CreditHistory.builder(
                        new CustomerId(customerId),
                        Money.of(new BigDecimal("2300")),
                        TransactionType.CREDIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());

        List<String> failureMessages = new ArrayList<>();

        //when
        paymentDomainService.validatePaymentInitiation(
                payment,
                creditEntry,
                creditHistoryList,
                failureMessages);

        PaymentEvent paymentEvent = paymentDomainService.initiatePayment(payment,
                failureMessages,
                paymentCompletedEventDomainEventPublisher,
                paymentFailedEventDomainEventPublisher);

        //then
        assertThat(paymentEvent.getFailureMessages().isEmpty())
                .isFalse();
    }
}

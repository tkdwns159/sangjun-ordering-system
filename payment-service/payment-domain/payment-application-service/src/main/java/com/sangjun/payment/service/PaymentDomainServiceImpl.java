package com.sangjun.payment.service;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.PaymentStatus;
import com.sangjun.payment.domain.PaymentDomainService;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.Payment;
import com.sangjun.payment.domain.event.PaymentCancelledEvent;
import com.sangjun.payment.domain.event.PaymentCompletedEvent;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.event.PaymentFailedEvent;
import com.sangjun.payment.domain.valueobject.CreditHistoryId;
import com.sangjun.payment.domain.valueobject.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.sangjun.common.domain.CommonConstants.ZONE_ID;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {

    @Override
    public PaymentEvent validateAndInitiatePayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> histories,
            List<String> failureMessages,
            DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventDomainEventPublisher,
            DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher
    ) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, histories, TransactionType.DEBIT);
        validateCreditHistory(creditEntry, histories, failureMessages);

        if (!failureMessages.isEmpty()) {
            log.error("Payment initiation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(
                    payment,
                    ZonedDateTime.now(ZoneId.of(ZONE_ID)),
                    failureMessages,
                    paymentFailedEventDomainEventPublisher);
        }

        log.info("Payment is initiated with id: {}", payment.getId().getValue());
        payment.updateStatus(PaymentStatus.COMPLETED);
        return new PaymentCompletedEvent(payment,
                ZonedDateTime.now(ZoneId.of(ZONE_ID)),
                paymentCompletedEventDomainEventPublisher);
    }

    private void validateCreditEntry(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            log.error("Customer with id: {} doesn't have enough credit for payment!",
                    creditEntry.getCustomerId());
            failureMessages.add("Customer with id =" + payment.getCustomerId().getValue()
                    + " doesn't have enough credit for payment!");
        }

    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private void updateCreditHistory(Payment payment, List<CreditHistory> histories, TransactionType transactionType) {
        histories.add(CreditHistory.builder(
                        payment.getCustomerId(),
                        payment.getPrice(),
                        transactionType)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());
    }

    private void validateCreditHistory(CreditEntry creditEntry, List<CreditHistory> histories, List<String> failureMessages) {
        Money totalCreditAmount = getTotalAmountFromHistory(histories, TransactionType.CREDIT);
        Money totalDebitAmount = getTotalAmountFromHistory(histories, TransactionType.DEBIT);

        if (totalDebitAmount.isGreaterThan(totalCreditAmount)) {
            log.error("Customer with id: {} doesn't have enough credit according to credit history",
                    creditEntry.getCustomerId().getValue());
            failureMessages.add("Customer with id=" + creditEntry.getCustomerId().getValue() +
                    " doesn't have enough credit according to credit history");
        }

        if (!creditEntry.getTotalCreditAmount().equals(totalCreditAmount.subtract(totalDebitAmount))) {
            log.error("Credit history total is not equal to current credit for customer id: {}",
                    creditEntry.getCustomerId().getValue());
            failureMessages.add("Credit history total is not equal to current credit for customer id=" +
                    creditEntry.getCustomerId().getValue());
        }
    }

    private Money getTotalAmountFromHistory(List<CreditHistory> histories, TransactionType transactionType) {
        return histories.stream()
                .filter(history -> history.getTransactionType() == transactionType)
                .map(CreditHistory::getAmount)
                .reduce(Money.ZERO, Money::add);
    }

    @Override
    public PaymentEvent validateAndCancelPayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> histories,
            List<String> failureMessages,
            DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher,
            DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher) {
        payment.validatePayment(failureMessages);
        addCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, histories, TransactionType.CREDIT);

        if (!failureMessages.isEmpty()) {
            log.error("Payment cancellation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(
                    payment,
                    ZonedDateTime.now(ZoneId.of(ZONE_ID)),
                    failureMessages,
                    paymentFailedEventDomainEventPublisher);
        }

        log.info("Payment is cancelled for order id: {}", payment.getOrderId().getValue());
        payment.updateStatus(PaymentStatus.CANCELLED);
        return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(ZONE_ID)), paymentCancelledEventDomainEventPublisher);
    }

    private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }
}
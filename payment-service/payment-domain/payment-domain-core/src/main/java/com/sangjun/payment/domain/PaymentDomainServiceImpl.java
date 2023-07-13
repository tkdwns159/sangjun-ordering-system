package com.sangjun.payment.domain;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.PaymentStatus;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.Payment;
import com.sangjun.payment.domain.event.PaymentCancelledEvent;
import com.sangjun.payment.domain.event.PaymentCompletedEvent;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.event.PaymentFailedEvent;
import com.sangjun.payment.domain.exception.PaymentDomainException;
import com.sangjun.payment.domain.valueobject.CreditHistoryId;
import com.sangjun.payment.domain.valueobject.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;

public class PaymentDomainServiceImpl implements PaymentDomainService {

    private static final Logger log = LoggerFactory.getLogger(PaymentDomainServiceImpl.class);

    @Override
    public PaymentEvent initiatePayment(Payment payment,
                                        CreditEntry creditEntry,
                                        List<CreditHistory> creditHistoryList,
                                        List<String> failureMessages) {
        payment.initializePayment();
        validatePaymentInitiation(payment, creditEntry, creditHistoryList, failureMessages);

        if (!failureMessages.isEmpty()) {
            log.error("Payment initiation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(
                    payment,
                    ZonedDateTime.now(ZoneId.of(ZONE_ID)),
                    failureMessages);
        }

        log.info("Payment is initiated with id: {}", payment.getId().getValue());
        payment.updateStatus(PaymentStatus.COMPLETED);
        return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }


    private void validatePaymentInitiation(Payment payment,
                                           CreditEntry creditEntry,
                                           List<CreditHistory> histories,
                                           List<String> failureMessages) {
        try {
            payment.validatePayment(failureMessages);
            checkIfCustomerHasEnoughCredit(payment, creditEntry, failureMessages);

            creditEntry.subtractCreditAmount(payment.getPrice());
            addCreditHistory(payment, histories, TransactionType.CREDIT);
            Money creditSum = getCreditSumFromCreditHistories(histories);
            checkIfCreditHistorySumIsNotMinus(creditSum, creditEntry.getCustomerId(), failureMessages);
            checkIfCreditHistorySumEqualsCredit(creditSum, creditEntry, failureMessages);

        } catch (PaymentDomainException e) {
            log.error(e.getMessage());
        }
    }


    private void checkIfCustomerHasEnoughCredit(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            failureMessages.add("Customer with id =" + payment.getCustomerId().getValue()
                    + " doesn't have enough credit for payment!");

            throw new PaymentDomainException("Customer with id: " + creditEntry.getCustomerId() +
                    " doesn't have enough credit for payment!");
        }
    }

    private void checkIfCreditHistorySumIsNotMinus(Money creditSum,
                                                   CustomerId customerId,
                                                   List<String> failureMessages) {
        if (creditSum.isLessThan(Money.ZERO)) {
            failureMessages.add("Customer with id=" + customerId.getValue() +
                    " doesn't have enough credit according to credit history");
            throw new PaymentDomainException("Customer with id=" + customerId.getValue() +
                    " doesn't have enough credit according to credit history");
        }
    }

    private void checkIfCreditHistorySumEqualsCredit(Money creditSum,
                                                     CreditEntry creditEntry,
                                                     List<String> failureMessages) {
        if (!creditEntry.getTotalCreditAmount().equals(creditSum)) {
            failureMessages.add("Credit history total is not equal to current credit for customer id=" +
                    creditEntry.getCustomerId().getValue());
            throw new PaymentDomainException("Credit history total is not equal to current credit for customer id=" +
                    creditEntry.getCustomerId().getValue());
        }
    }

    private Money getCreditSumFromCreditHistories(List<CreditHistory> histories) {
        Money totalCreditAmount = getTotalAmountFromHistory(histories, TransactionType.CREDIT);
        Money totalDebitAmount = getTotalAmountFromHistory(histories, TransactionType.DEBIT);
        return totalDebitAmount.subtract(totalCreditAmount);
    }

    @Override
    public PaymentEvent cancelPayment(Payment payment,
                                      CreditEntry creditEntry,
                                      List<CreditHistory> histories,
                                      List<String> failureMessages) {
        validatePaymentCancel(payment, failureMessages);

        if (!failureMessages.isEmpty()) {
            log.error("Payment initiation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(
                    payment,
                    ZonedDateTime.now(ZoneId.of(ZONE_ID)),
                    failureMessages);
        }

        creditEntry.addCreditAmount(payment.getPrice());
        addCreditHistory(payment, histories, TransactionType.DEBIT);
        payment.updateStatus(PaymentStatus.CANCELLED);

        return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }

    private void validatePaymentCancel(Payment payment, List<String> failureMessages) {
        try {
            payment.validatePayment(failureMessages);
        } catch (PaymentDomainException e) {
            log.error(e.getMessage());
        }
    }

    private void addCreditHistory(Payment payment,
                                  List<CreditHistory> histories,
                                  TransactionType transactionType) {
        histories.add(CreditHistory.builder(payment.getCustomerId(),
                        payment.getPrice(),
                        transactionType)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build());
    }


    private Money getTotalAmountFromHistory(List<CreditHistory> histories, TransactionType transactionType) {
        return histories.stream()
                .filter(history -> history.getTransactionType() == transactionType)
                .map(CreditHistory::getAmount)
                .reduce(Money.ZERO, Money::add);
    }
}

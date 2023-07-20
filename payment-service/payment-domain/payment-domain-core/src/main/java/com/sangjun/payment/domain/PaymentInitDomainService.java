package com.sangjun.payment.domain;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.event.PaymentCompletedEvent;
import com.sangjun.payment.domain.event.PaymentEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;

public class PaymentInitDomainService {

    public PaymentEvent initPayment(Payment payment, Book from, Book to) {
        validate(payment, from);
        payment.initialize();
        from.transact(to, payment.getPrice(), "ITEM_PURCHASE", "ITEM_SELL");
        payment.complete();

        return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }

    private void validate(Payment payment, Book from) {
        checkIfBalanceIsEnoughForPaymentPrice(payment, from);
    }

    private void checkIfBalanceIsEnoughForPaymentPrice(Payment payment, Book book) {
        Money paymentPrice = payment.getPrice();
        Money currentBalance = book.getTotalBalance().getCurrentBalance();

        if (paymentPrice.isGreaterThan(currentBalance)) {
            throw new IllegalStateException(String.format("Payment price(%s) is over the current balance(%s)",
                    payment.getPrice(), currentBalance));
        }
    }
}

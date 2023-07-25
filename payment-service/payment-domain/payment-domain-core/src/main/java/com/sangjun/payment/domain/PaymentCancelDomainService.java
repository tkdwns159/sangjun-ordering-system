package com.sangjun.payment.domain;

import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.event.PaymentCancelledEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;

public class PaymentCancelDomainService {
    public PaymentCancelledEvent cancelPayment(Payment payment, Book from, Book to) {
        from.transact(to, payment.getPrice(), "ITEM_REFUND", "ITEM_REFUND");
        payment.cancel();
        
        return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }
}

package com.sangjun.payment.domain;

import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.payment.Payment;

public class PaymentCancelDomainService {
    public void cancelPayment(Payment payment, Book from, Book to) {
        from.transact(to, payment.getPrice(), "ITEM_REFUND", "ITEM_REFUND");
        payment.cancel();
    }
}

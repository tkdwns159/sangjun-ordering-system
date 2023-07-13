package com.sangjun.payment.domain;

import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.event.PaymentEvent;

import java.util.List;

public interface PaymentDomainService {


    PaymentEvent initiatePayment(Payment payment,
                                 CreditEntry creditEntry,
                                 List<CreditHistory> creditHistoryList,
                                 List<String> failureMessages);

    PaymentEvent cancelPayment(Payment payment,
                               CreditEntry creditEntry,
                               List<CreditHistory> histories,
                               List<String> failureMessages);
}

package com.sangjun.payment.domain;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.Payment;
import com.sangjun.payment.domain.event.PaymentCancelledEvent;
import com.sangjun.payment.domain.event.PaymentCompletedEvent;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.event.PaymentFailedEvent;

import java.util.List;

public interface PaymentDomainService {

    void validatePaymentInitiation(Payment payment,
                                   CreditEntry creditEntry,
                                   List<CreditHistory> histories,
                                   List<String> failureMessages);


    PaymentEvent initiatePayment(Payment payment,
                                 List<String> failureMessages,
                                 DomainEventPublisher<PaymentCompletedEvent> paymentFailedEventDomainEventPublisher,
                                 DomainEventPublisher<PaymentFailedEvent> paymentCancelledEventDomainEventPublisher);

    @Deprecated
    PaymentEvent validateAndInitiatePayment(Payment payment,
                                            CreditEntry creditEntry,
                                            List<CreditHistory> histories,
                                            List<String> failureMessages,
                                            DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventDomainEventPublisher,
                                            DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher);

    PaymentEvent validateAndCancelPayment(Payment payment,
                                          CreditEntry creditEntry,
                                          List<CreditHistory> histories,
                                          List<String> failureMessages,
                                          DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher,
                                          DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher);
}

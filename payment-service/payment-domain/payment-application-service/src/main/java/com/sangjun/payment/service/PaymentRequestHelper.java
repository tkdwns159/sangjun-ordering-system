package com.sangjun.payment.service;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.payment.domain.PaymentDomainService;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.Payment;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.exception.PaymentApplicationServiceException;
import com.sangjun.payment.service.mapper.PaymentDataMapper;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.sangjun.payment.service.ports.output.message.publisher.PaymentFailedMessagePublisher;
import com.sangjun.payment.service.ports.output.repository.CreditEntryRepository;
import com.sangjun.payment.service.ports.output.repository.CreditHistoryRepository;
import com.sangjun.payment.service.ports.output.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestHelper {
    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final PaymentCompletedMessagePublisher paymentCompletedEventDomainEventPublisher;
    private final PaymentFailedMessagePublisher paymentFailedEventDomainEventPublisher;
    private final PaymentCancelledMessagePublisher paymentCancelledMessagePublisher;

    @Transactional
    public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
        log.info("Received payment complete event for order id :{}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestToPayment(paymentRequest);
        PaymentDetails paymentDetails = getPaymentDetails(payment);

        PaymentEvent paymentEvent = paymentDomainService.initiatePayment(
                payment,
                paymentDetails.creditEntry,
                paymentDetails.creditHistories,
                paymentDetails.failureMessages);

        persistData(payment, paymentDetails);

        return paymentEvent;
    }

    @Transactional
    public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
        log.info("Received payment rollback event for order id: {}", paymentRequest.getOrderId());
        Optional<Payment> foundPayment = paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));
        Payment payment = foundPayment.orElseThrow(() -> {
            log.error("Payment with order id: {} could not be found", paymentRequest.getOrderId());
            return new PaymentApplicationServiceException("Payment with order id: " + paymentRequest.getOrderId()
                    + " could not be found");
        });
        PaymentDetails paymentDetails = getPaymentDetails(payment);

        PaymentEvent paymentEvent = paymentDomainService.cancelPayment(
                payment,
                paymentDetails.creditEntry,
                paymentDetails.creditHistories,
                paymentDetails.failureMessages);

        persistData(payment, paymentDetails);

        return paymentEvent;
    }

    private PaymentDetails getPaymentDetails(Payment payment) {
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistories(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();

        return PaymentDetails.builder()
                .creditEntry(creditEntry)
                .creditHistories(creditHistories)
                .failureMessages(failureMessages)
                .build();
    }

    private void persistData(Payment payment, PaymentDetails paymentDetails) {
        paymentRepository.save(payment);

        if (paymentDetails.failureMessages.isEmpty()) {
            List<CreditHistory> creditHistories = paymentDetails.creditHistories;

            creditEntryRepository.save(paymentDetails.creditEntry);
            creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
        }
    }


    private CreditEntry getCreditEntry(CustomerId customerId) {
        Optional<CreditEntry> creditEntry = creditEntryRepository.findByCustomerId(customerId.getValue());
        return creditEntry.orElseThrow(() -> {
            log.error("Could not find credit entry for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit entry for customer: " + customerId.getValue());
        });
    }

    private List<CreditHistory> getCreditHistories(CustomerId customerId) {
        Optional<List<CreditHistory>> creditHistories = creditHistoryRepository.findByCustomerId(customerId.getValue());
        return creditHistories.orElseThrow(() -> {
            log.error("Could not find credit histories for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit histories for customer: " + customerId.getValue());
        });
    }
}

package com.sangjun.payment.service;

import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.exception.PaymentApplicationServiceException;
import com.sangjun.payment.service.ports.input.message.listener.PaymentRequestMessageListener;
import com.sangjun.payment.service.ports.output.message.publisher.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestMessageListenerImpl implements PaymentRequestMessageListener {
    private final PaymentRequestHelper paymentRequestHelper;

    @Override
    public void completePayment(PaymentRequest paymentRequest) {
        PaymentEvent paymentEvent = paymentRequestHelper.persistPayment(paymentRequest);
        fireEvent(paymentEvent);

    }

    @Override
    public void cancelPayment(PaymentRequest paymentRequest) {
        PaymentEvent paymentEvent = paymentRequestHelper.persistCancelPayment(paymentRequest);
        fireEvent(paymentEvent);
    }

    private void fireEvent(PaymentEvent paymentEvent) {
        log.info("Publishing payment event with payment id: {} and order id: {}",
                paymentEvent.getPayment().getId().getValue(),
                paymentEvent.getPayment().getOrderId().getValue());
        paymentEvent.fire();
    }
}

package com.sangjun.order.domain.service;

import com.sangjun.common.domain.CommonConstants;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.service.dto.message.PaymentResponse;
import com.sangjun.order.domain.service.ports.input.message.listener.payment.PaymentResponseMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.sangjun.common.domain.CommonConstants.*;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

    private final OrderPaymentSaga orderPaymentSaga;

    @Override
    public void paymentCompleted(PaymentResponse paymentResponse) {
        OrderPaidEvent orderPaidEvent = orderPaymentSaga.process(paymentResponse);
        log.info("Publishing OrderPaidEvent with order id :{}", orderPaidEvent.getOrder().getId().getValue());
        orderPaidEvent.fire();
    }

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {
        orderPaymentSaga.rollback(paymentResponse);
        log.info("Order is rollbacked for order id :{} with failure messages: {}",
                paymentResponse.getOrderId(),
                String.join(FAILURE_MESSAGE_DELIMITER, paymentResponse.getFailureMessages()));
    }
}

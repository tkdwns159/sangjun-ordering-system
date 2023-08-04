package com.sangjun.order.domain.service;

import com.sangjun.common.domain.event.DomainEvent;
import com.sangjun.common.domain.event.EmptyEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.service.dto.message.PaymentResponse;
import com.sangjun.order.domain.service.ports.input.message.listener.payment.PaymentResponseMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.sangjun.common.utils.CommonConstants.FAILURE_MESSAGE_DELIMITER;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

    private final OrderPaymentSaga orderPaymentSaga;
    private final OrderEventShooter orderEventShooter;

    @Override
    public void paymentCompleted(PaymentResponse paymentResponse) {
        DomainEvent event = orderPaymentSaga.process(paymentResponse);
        if (event instanceof EmptyEvent) {
            return;
        }

        OrderPaidEvent orderPaidEvent = (OrderPaidEvent) event;
        log.info("Publishing OrderPaidEvent with order id :{}", orderPaidEvent.getOrder().getId().getValue());
        orderEventShooter.fire(orderPaidEvent);
    }

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {
        orderPaymentSaga.rollback(paymentResponse);
        log.info("Order is rollbacked for order id :{} with failure messages: {}",
                paymentResponse.getOrderId(),
                String.join(FAILURE_MESSAGE_DELIMITER, paymentResponse.getFailureMessages()));
    }
}

package com.sangjun.order.service.application.exception.handler;

import com.sangjun.common.application.exception.handler.ErrorDTO;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class OrderGlobalExceptionHandler {

    @ExceptionHandler(value = {OrderDomainException.class})
    public ResponseEntity<ErrorDTO> handleException(OrderDomainException orderDomainException) {
        log.error(orderDomainException.getMessage(), orderDomainException);

        return ResponseEntity
                .badRequest()
                .body(ErrorDTO.builder()
                        .message(orderDomainException.getMessage())
                        .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .build());
    }

    @ExceptionHandler(value = {OrderNotFoundException.class})
    public ResponseEntity<ErrorDTO> handleException(OrderNotFoundException orderNotFoundException) {
        log.error(orderNotFoundException.getMessage(), orderNotFoundException);
        return ResponseEntity
                .badRequest()
                .body(ErrorDTO.builder()
                        .message(orderNotFoundException.getMessage())
                        .code(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .build());
    }
}

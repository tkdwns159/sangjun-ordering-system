package com.sangjun.common.application.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDTO handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ErrorDTO.builder()
                .message("Unexpected Error!")
                .code(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .build();
    }


    @ResponseBody
    @ExceptionHandler(value = {ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleException(ValidationException exception) {
        return makeErrorDTO(exception);
    }

    private ErrorDTO makeErrorDTO(ValidationException exception) {
        if(exception instanceof ConstraintViolationException) {
            String violations = extractViolationsFromException((ConstraintViolationException) exception);
            log.error(violations, exception);
            return ErrorDTO.builder()
                    .message(violations)
                    .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .build();
        }

        log.error(exception.getMessage(), exception);
        return ErrorDTO.builder()
                .message(exception.getMessage())
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .build();
    }

    private String extractViolationsFromException(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("--"));
    }
}

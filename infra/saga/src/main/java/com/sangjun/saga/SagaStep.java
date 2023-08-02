package com.sangjun.saga;

import com.sangjun.common.domain.event.DomainEvent;

public interface SagaStep<T, S extends DomainEvent, U> {
    S process(T data);

    U rollback(T data);
}

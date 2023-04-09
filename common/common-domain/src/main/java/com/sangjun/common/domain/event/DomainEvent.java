package com.sangjun.common.domain.event;

public interface DomainEvent<T> {
    void fire();
}

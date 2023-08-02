package com.sangjun.common.domain.event.publisher;

public interface DomainEventPublisher<T> {

    void publish(T domainEvent);
}

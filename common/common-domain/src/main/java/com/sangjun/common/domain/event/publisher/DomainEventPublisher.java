package com.sangjun.common.domain.event.publisher;

import com.sangjun.common.domain.event.DomainEvent;

public interface DomainEventPublisher<T extends DomainEvent> {

    void publish(T domainEvent);
}

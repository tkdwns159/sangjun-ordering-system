package com.sangjun.payment.service.ports.input.service.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BookCreateRequest {
    private final Long shelveId;
    private final String ownerId;
}

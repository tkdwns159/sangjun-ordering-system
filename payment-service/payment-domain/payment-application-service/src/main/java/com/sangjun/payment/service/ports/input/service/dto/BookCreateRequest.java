package com.sangjun.payment.service.ports.input.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class BookCreateRequest {
    private final UUID shelveId;
    private final String ownerId;
}

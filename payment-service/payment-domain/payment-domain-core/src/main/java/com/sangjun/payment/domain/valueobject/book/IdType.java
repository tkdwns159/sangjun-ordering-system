package com.sangjun.payment.domain.valueobject.book;

public class IdType {
    private final String type;

    public IdType(String type) {
        this.type = type;
    }

    public boolean isUUID() {
        return type.equalsIgnoreCase("UUID");
    }

    public boolean isLong() {
        return type.equalsIgnoreCase("Long");
    }
}

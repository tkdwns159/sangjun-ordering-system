package com.sangjun.order.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.List;

@Converter(autoApply = false)
public class FailureMessageAttributeConverter implements AttributeConverter<List<String>, String> {
    private static final String DELIMETER = ", ";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return String.join(DELIMETER, attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return Arrays.stream(dbData.split(DELIMETER)).toList();
    }
}

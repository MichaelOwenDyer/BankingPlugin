package com.monst.bankingplugin.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class MultipliersConverter implements AttributeConverter<List<Integer>, String> {
    @Override
    public String convertToDatabaseColumn(List<Integer> multipliers) {
        if (multipliers == null)
            return null;
        return multipliers.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return Arrays.stream(dbData.split(", ")).map(Integer::parseInt).collect(Collectors.toList());
    }
}

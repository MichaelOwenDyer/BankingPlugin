package com.monst.bankingplugin.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class InterestPayoutTimesConverter implements AttributeConverter<Set<LocalTime>, String> {
    @Override
    public String convertToDatabaseColumn(Set<LocalTime> times) {
        if (times == null)
            return null;
        return times.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    @Override
    public Set<LocalTime> convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return Arrays.stream(dbData.split(", ")).map(LocalTime::parse).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

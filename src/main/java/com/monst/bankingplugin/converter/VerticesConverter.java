package com.monst.bankingplugin.converter;

import com.monst.bankingplugin.entity.geo.Vector2;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class VerticesConverter implements AttributeConverter<List<Vector2>, String> {
    @Override
    public String convertToDatabaseColumn(List<Vector2> vectors) {
        return vectors.toString();
    }

    @Override
    public List<Vector2> convertToEntityAttribute(String vectorString) {
        return Arrays.stream(vectorString.split(",")).map(this::parseVector2).collect(Collectors.toList());
    }

    private Vector2 parseVector2(String string) {
        String[] values = string.replace("\\[\\]", "").split(";");
        return new Vector2(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
    }
}

package org.opendatamesh.platform.up.executor.gitlabci.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Converter
public class ListMapConverter implements AttributeConverter<List<Map<String, String>>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public String convertToDatabaseColumn(List<Map<String, String>> maps) {
        if (maps == null || maps.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(maps);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert list of maps to JSON string.", e);
        }
    }

    @Override
    public List<Map<String, String>> convertToEntityAttribute(String s) {
        if(s == null || s.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(s, new TypeReference<List<Map<String, String>>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Could not convert JSON string to list of map.", e);
        }
    }
}

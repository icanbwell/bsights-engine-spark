package com.bwell.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class PropertyInspector {
    public static String getAllPropertiesAsJson(Object fooObject) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writer().withoutAttribute("idElement").writeValueAsString(fooObject);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static HashMap<String, Object> getAllProperties2(Object fooObject) {
        HashMap<String, Object> result = new HashMap<>();
        List<Field> fields = getAllFields(fooObject.getClass());
        for (Field field : fields) {
            Object value;
            try {
                value = field.get(fooObject);
                if (value instanceof ArrayList) {
                    for (Object item : (ArrayList)value) {
                        HashMap<String, Object> properties2 = getAllProperties2(item);
                        result.put(field.getName(), properties2);
                    }
                } else if (value instanceof org.hl7.fhir.r4.model.Base) {
                    result.put(field.getName(), getAllProperties2(value));
                } else {
                    result.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static List<Field> getAllFields(Class clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }

        List<Field> result = new ArrayList<>(getAllFields(clazz.getSuperclass()));
        List<Field> filteredFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .collect(Collectors.toList());
        result.addAll(filteredFields);
        return result;
    }
}

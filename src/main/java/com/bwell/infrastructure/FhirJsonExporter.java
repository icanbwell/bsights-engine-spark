package com.bwell.infrastructure;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class to export Fhir objects as Json
 */
public class FhirJsonExporter {
    /**
     * Export the resource to JSON
     * @param resource: resource
     * @return json
     */
    public static String getResourceAsJson(IBaseResource resource) {
        String fhirVersion = "R4";
        FhirVersionEnum fhirVersionEnum = FhirVersionEnum.valueOf(fhirVersion);
        FhirContext fhirContext = fhirVersionEnum.newContext();
        return fhirContext.newJsonParser().encodeResourceToString(resource);
    }

    /**
     * Export the Map to JSON
     * @param map: map
     * @return json
     */
    public static String getMapAsJson(Map<String, String> map) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return map.toString();
        }
    }

    /**
     * Export the MapSet to JSON
     * @param entrySet: MapSet
     * @return json
     */
    public static String getMapSetAsJson(Set<Map.Entry<String, Object>> entrySet) {
        Map<String, String> jsonMap = new HashMap<>();
        for (Map.Entry<String, Object> libraryEntry : entrySet) {
            String key = libraryEntry.getKey();
            Object value = libraryEntry.getValue();
            if (value instanceof IBaseResource) {
                jsonMap.put(key, getResourceAsJson((IBaseResource) value));
            } else {
                jsonMap.put(key, value != null ? value.toString(): null);
            }
        }
        return getMapAsJson(jsonMap);
    }
}

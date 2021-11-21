package com.bwell;

import org.apache.spark.sql.api.java.UDF3;

public class RunCql implements UDF3<String, String, String, java.util.Map<String, String>> {
    private static final long serialVersionUID = 1L;
    @Override
    public java.util.Map<String, String> call(String cqlLibraryUrl, String terminologyUrl, String fhirBundle) throws Exception {
        java.util.Map<String, String> newMap = new java.util.HashMap<>();
        newMap.put("key1", "value1");
        newMap.put("key2", "value2");

        return newMap;
    }
}


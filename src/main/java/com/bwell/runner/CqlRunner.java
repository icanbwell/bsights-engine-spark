package com.bwell.runner;

import java.util.*;

public class CqlRunner {

    public Map<String, String> runCql(String cqlLibraryUrl, String terminologyUrl, String fhirBundle) {
        java.util.Map<String, String> newMap = new java.util.HashMap<>();
        newMap.put("key1", fhirBundle + "_1");
        newMap.put("key2", fhirBundle + "_2");

        return newMap;
    }
}

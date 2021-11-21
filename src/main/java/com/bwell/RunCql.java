package com.bwell;

import org.apache.spark.sql.api.java.UDF3;

public class RunCql implements UDF3<String, String, String, String> {
    private static final long serialVersionUID = 1L;
    @Override
    public String call(String cqlLibraryUrl, String terminologyUrl, String fhirBundle) throws Exception {
        return "foo";
    }
}


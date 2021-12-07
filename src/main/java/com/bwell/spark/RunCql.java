package com.bwell.spark;

import org.apache.spark.sql.api.java.UDF3;
import com.bwell.runner.CqlRunner;

public class RunCql implements UDF3<String, String, String, java.util.Map<String, String>> {
    private static final long serialVersionUID = 1L;
    @Override
    public java.util.Map<String, String> call(String cqlLibraryUrl, String terminologyUrl, String fhirBundle) throws Exception {
        return new CqlRunner().runCql(cqlLibraryUrl, terminologyUrl, fhirBundle);
    }
}


package com.bwell;

import org.apache.spark.sql.api.java.UDF6;

public class RunCqlLibrary implements UDF6<String, String, String, String, String, String, java.util.Map<String, Object>> {
    private static final long serialVersionUID = 1L;

    @Override
    public java.util.Map<String, Object> call(String cqlLibraryUrl,
                                              String cqlLibraryName,
                                              String cqlLibraryVersion,
                                              String terminologyUrl,
                                              String cqlVariablesToReturn,
                                              String fhirBundle) throws Exception {
        return new CqlRunner().runCqlLibrary(
                cqlLibraryUrl,
                cqlLibraryName,
                cqlLibraryVersion,
                terminologyUrl,
                cqlVariablesToReturn,
                fhirBundle
        );
    }
}


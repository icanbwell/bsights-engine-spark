package com.bwell;

import org.apache.spark.sql.api.java.UDF6;

/**
 * This class implements a Spark UDF that takes in 6 strings and returns a map of key, value
 */
public class RunCqlLibrary implements UDF6<String, String, String, String, String, String, java.util.Map<String, String>> {
    private static final long serialVersionUID = 1L;

    /**
     * Implements the UDF function
     * @param cqlLibraryUrl: url to fhir server containing the cql library to run
     * @param cqlLibraryName: name of CQL library to run
     * @param cqlLibraryVersion: version of cql library to run
     * @param terminologyUrl: url to fhir server that has value sets
     * @param cqlVariablesToReturn: list of cql variables that we should return the values for
     * @param fhirBundle: FHIR resource bundle as a string
     * @return Map of cql variable name, value
     * @throws Exception: exception
     */
    @Override
    public java.util.Map<String, String> call(String cqlLibraryUrl,
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


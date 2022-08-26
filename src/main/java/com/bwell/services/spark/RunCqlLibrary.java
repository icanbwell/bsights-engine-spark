package com.bwell.services.spark;

import com.bwell.services.application.MeasureService;
import org.apache.spark.sql.api.java.UDF13;

import java.util.Map;

/**
 * This class implements a Spark UDF that takes in 10 string parameters and returns a map of key, value
 */
public class RunCqlLibrary implements UDF13<String, String, String, String, String, String, String, String, String, String, String, String, String, Map<String, String>> {
    private static final long serialVersionUID = 1L;

    /**
     * Implements the UDF function
     * @param libraryUrl:               url to fhir server containing the cql library to run
     * @param cqlLibraryName:           name of CQL library to run
     * @param cqlLibraryVersion:        version of cql library to run
     * @param terminologyUrl:           url to fhir server that has value sets
     * @param cqlVariablesToReturn:     list of cql variables that we should return the values for
     * @param fhirBundle:               FHIR resource bundle as a string
     * @param authUrl:                  URL for Auth Server
     * @param authId:                   Client ID for Auth Server
     * @param authSecret:               Client Secret for Auth Server
     * @param authScope:                Client Scope for Auth Server
     * @param clientTimeout:            Timeout for the FHIR Server
     * @param contextName:              Optional context name
     * @param contextValue:             Optional context value
     * @return Map of cql variable name, value
     * @throws Exception: exception
     */
    @Override
    public java.util.Map<String, String> call(String libraryUrl,
                                              String cqlLibraryName,
                                              String cqlLibraryVersion,
                                              String terminologyUrl,
                                              String cqlVariablesToReturn,
                                              String fhirBundle,
                                              String authUrl,
                                              String authId,
                                              String authSecret,
                                              String authScope,
                                              String clientTimeout,
                                              String contextName,
                                              String contextValue) throws Exception {
        return new MeasureService().runCqlLibrary(
                libraryUrl,
                cqlLibraryName,
                cqlLibraryVersion,
                terminologyUrl,
                cqlVariablesToReturn,
                fhirBundle,
                authUrl,
                authId,
                authSecret,
                authScope,
                clientTimeout,
                contextName,
                contextValue
        );
    }
}
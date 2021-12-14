package com.bwell.services.spark;

import com.bwell.services.application.MeasureService;
import org.apache.spark.sql.api.java.UDF10;

/**
 * This class implements a Spark UDF that takes in 10 string parameters and returns a map of key, value
 */
public class RunCqlLibrary implements UDF10<String, String, String, String, String, String, String, String, String, String, java.util.Map<String, String>> {
    private static final long serialVersionUID = 1L;

    /**
     * Implements the UDF function
     * @param libraryUrl:               url to fhir server containing the cql library to run
     * @param libraryUrlHeaders:        http headers for call to the fhir server - auth
     * @param cqlLibraryName:           name of CQL library to run
     * @param cqlLibraryVersion:        version of cql library to run
     * @param terminologyUrl:           url to fhir server that has value sets
     * @param terminologyUrlHeaders:    http headers for the call to the terminology server - auth
     * @param cqlVariablesToReturn:     list of cql variables that we should return the values for
     * @param fhirBundle:               FHIR resource bundle as a string
     * @param contextName:              Optional context name
     * @param contextValue:             Optional context value
     * @return Map of cql variable name, value
     * @throws Exception: exception
     */
    @Override
    public java.util.Map<String, String> call(String libraryUrl,
                                              String libraryUrlHeaders,
                                              String cqlLibraryName,
                                              String cqlLibraryVersion,
                                              String terminologyUrl,
                                              String terminologyUrlHeaders,
                                              String cqlVariablesToReturn,
                                              String fhirBundle,
                                              String contextName,
                                              String contextValue) throws Exception {
        return new MeasureService().runCqlLibrary(
                libraryUrl,
                libraryUrlHeaders,
                cqlLibraryName,
                cqlLibraryVersion,
                terminologyUrl,
                terminologyUrlHeaders,
                cqlVariablesToReturn,
                fhirBundle,
                contextName,
                contextValue
        );
    }
}
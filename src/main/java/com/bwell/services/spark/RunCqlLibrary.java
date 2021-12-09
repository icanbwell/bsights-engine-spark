package com.bwell.services.spark;

<<<<<<< Updated upstream:src/main/java/com/bwell/spark/RunCqlLibrary.java
import com.bwell.runner.MeasureRunner;
import org.apache.spark.sql.api.java.UDF8;
=======
import com.bwell.services.application.MeasureService;
import org.apache.spark.sql.api.java.UDF6;
>>>>>>> Stashed changes:src/main/java/com/bwell/services/spark/RunCqlLibrary.java

/**
 * This class implements a Spark UDF that takes in 6 strings and returns a map of key, value
 * allowing Spark to execute a CQL Measure Library.
 */
<<<<<<< Updated upstream:src/main/java/com/bwell/spark/RunCqlLibrary.java
public class RunCqlLibrary implements UDF8<String, String, String, String, String, String, String, String, java.util.Map<String, String>> {
=======
public class RunCqlLibrary implements UDF6<String, String, String, String, String, String, java.util.Map<String, String>> {

>>>>>>> Stashed changes:src/main/java/com/bwell/services/spark/RunCqlLibrary.java
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
<<<<<<< Updated upstream:src/main/java/com/bwell/spark/RunCqlLibrary.java
                                              String fhirBundle,
                                              String contextName,
                                              String contextValue) throws Exception {
        return new MeasureRunner().runCqlLibrary(
=======
                                              String fhirBundle) throws Exception {
        return new MeasureService().runCqlLibrary(
>>>>>>> Stashed changes:src/main/java/com/bwell/services/spark/RunCqlLibrary.java
                cqlLibraryUrl,
                cqlLibraryName,
                cqlLibraryVersion,
                terminologyUrl,
                cqlVariablesToReturn,
                fhirBundle,
                contextName,
                contextValue
        );
    }
}


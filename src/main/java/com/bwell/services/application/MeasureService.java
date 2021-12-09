package com.bwell.services.application;

import com.bwell.core.entities.ContextParameter;
import com.bwell.core.entities.LibraryParameter;
import com.bwell.core.entities.ModelParameter;
import com.bwell.services.domain.CqlService;
import org.opencds.cqf.cql.engine.exception.CqlException;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides CQL Measure Library processing
 */
public class MeasureService {

    /**
     * Runs the CQL Library
     *
     * @param cqlLibraryUrl:        link to fhir server that holds the CQL library
     * @param cqlLibraryName:       name of cql library
     * @param cqlLibraryVersion:    version of cql library
     * @param terminologyUrl:       link to fhir server that holds the value set
     * @param cqlVariablesToReturn: comma separated list of cql variables.  This functions returns a dictionary of values for these
     * @param fhirBundle:           FHIR bundle that contains the patient resource and any related resources like observations, conditions etc
     * @return map (dictionary) of variable name, value
     * @throws Exception exception
     */
    public Map<String, String> runCqlLibrary(
            String cqlLibraryUrl,
            String cqlLibraryName,
            String cqlLibraryVersion,
            String terminologyUrl,
            String cqlVariablesToReturn,
            String fhirBundle,
            String contextName,
            String contextValue
    ) throws Exception {
        String fhirVersion = "R4";
        List<LibraryParameter> libraries = new ArrayList<>();
        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = cqlLibraryName;

        libraryParameter.libraryUrl = cqlLibraryUrl;
        libraryParameter.libraryVersion = cqlLibraryVersion;
        libraryParameter.terminologyUrl = terminologyUrl;
        libraryParameter.model = new ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelBundle = fhirBundle;
        libraryParameter.context = new ContextParameter();
        libraryParameter.context.contextName = "Patient";
        libraryParameter.context.contextValue = "example";

        libraries.add(libraryParameter);

        List<String> cqlVariables = Arrays.stream(cqlVariablesToReturn.split(",")).map(String::trim).collect(Collectors.toList());

        Map<String, String> newMap = new HashMap<>();

        try {
            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);
            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
            for (Map.Entry<String, Object> libraryEntry : entrySet) {
                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();
                if (cqlVariables.contains(key)) {
                    newMap.put(key, value.toString());
                }
            }
        } catch (CqlException e) {
            if (Objects.equals(e.getMessage(), "Unexpected exception caught during execution: ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException: HTTP 404 Not Found")) {
                throw new Exception("NOTE: Did you run make loadfhir to load the fhir server?");
            } else {
                throw e;
            }

        }

        return newMap;
    }
}

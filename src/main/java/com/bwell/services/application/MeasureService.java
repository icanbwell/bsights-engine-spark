package com.bwell.services.application;

import com.bwell.core.entities.*;
import com.bwell.services.domain.CqlService;
import org.hl7.fhir.r4.model.Patient;
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
     * @param cqlLibraryHeaders:    comma-separated list of haeders to include in the call to get the CQL library
     * @param cqlLibraryName:       name of cql library
     * @param cqlLibraryVersion:    version of cql library
     * @param terminologyUrl:       link to fhir server that holds the value set
     * @param terminologyUrlHeaders:    comma-separated list of headers to include in the call to get the terminology library
     * @param cqlVariablesToReturn: comma separated list of cql variables.  This functions returns a dictionary of values for these
     * @param fhirBundle:           FHIR bundle that contains the patient resource and any related resources like observations, conditions etc
     * @param contextName:          Optional context name
     * @param contextValue:         Optional context value
     * @return map (dictionary) of variable name, value
     * @throws Exception exception
     */
    public Map<String, String> runCqlLibrary(
            String cqlLibraryUrl,
            String cqlLibraryHeaders,
            String cqlLibraryName,
            String cqlLibraryVersion,
            String terminologyUrl,
            String terminologyUrlHeaders,
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
        if (contextName != null) {
            libraryParameter.context = new ContextParameter();
            libraryParameter.context.contextName = contextName;
            if (contextValue != null) {
                libraryParameter.context.contextValue = contextValue;
            }
        }

        libraries.add(libraryParameter);

        List<String> cqlVariables = Arrays.stream(cqlVariablesToReturn.split(",")).map(String::trim).collect(Collectors.toList());
        if (cqlLibraryHeaders != null && !cqlLibraryHeaders.equals("")){
            libraryParameter.libraryUrlHeaders = Arrays.stream(cqlLibraryHeaders.split(",")).map(String::trim).collect(Collectors.toList());
        }
        if (terminologyUrlHeaders != null && !terminologyUrlHeaders.equals("")){
            libraryParameter.terminologyUrlHeaders = Arrays.stream(terminologyUrlHeaders.split(",")).map(String::trim).collect(Collectors.toList());
        }

        Map<String, String> newMap = new HashMap<>();

        try {
            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);
            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
            for (Map.Entry<String, Object> libraryEntry : entrySet) {
                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();

                if ("Patient".equals(key)) {
                    Patient patient = (Patient) value;
                    newMap.put("PatientId", patient.hasId() ? patient.getId() : null);

                }else{
                    if (cqlVariables.contains(key)) {
                        newMap.put(key, value != null ? value.toString() : null);
                    }

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

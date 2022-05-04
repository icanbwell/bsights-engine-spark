package com.bwell.services.application;

import com.bwell.core.entities.ContextParameter;
import com.bwell.core.entities.LibraryParameter;
import com.bwell.core.entities.ModelParameter;
import com.bwell.infrastructure.FhirJsonExporter;
import com.bwell.services.domain.CqlService;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides CQL Measure Library processing
 */
public class MeasureService {

    private static final org.slf4j.Logger myLogger = org.slf4j.LoggerFactory.getLogger(MeasureService.class);

    /**
     * Runs the CQL Library
     *
     * @param libraryUrl:            URL to FHIR server containing the cql library to run
     * @param libraryUrlHeaders:     HTTP headers for call to the fhir server - auth
     * @param libraryName:           name of the CQL library to run
     * @param libraryVersion:        version of the CQL library to run
     * @param terminologyUrl:        URL to FHIR server that has value sets
     * @param terminologyUrlHeaders: HTTP headers for the call to the terminology server - auth
     * @param cqlVariablesToReturn:  list of the CQL variables that we should return the values for
     * @param fhirBundle:            FHIR resource bundle as a string
     * @param contextName:           Optional context name
     * @param contextValue:          Optional context value
     * @return map (dictionary) of CQL variable name, value
     * @throws Exception exception
     */
    public Map<String, String> runCqlLibrary(
            String libraryUrl,
            String libraryUrlHeaders,
            String libraryName,
            String libraryVersion,
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
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryUrl = libraryUrl;
        libraryParameter.libraryVersion = libraryVersion;
        libraryParameter.terminologyUrl = terminologyUrl;
        libraryParameter.model = new ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelBundle = fhirBundle;
        libraryParameter.context = new ContextParameter();
        if (contextName != null) {
            libraryParameter.context.contextName = contextName;
        }
        if (contextValue != null) {
            libraryParameter.context.contextValue = contextValue;
        }

        myLogger.info("Running with libraryUrl={}, terminologyUrl={}, fhir={}", libraryUrl, terminologyUrl, fhirBundle);

        if (libraryUrlHeaders != null && !libraryUrlHeaders.equals("")) {
            libraryParameter.libraryUrlHeaders = Arrays.stream(libraryUrlHeaders.split(",")).map(String::trim).collect(Collectors.toList());
        }
        if (terminologyUrlHeaders != null && !terminologyUrlHeaders.equals("")) {
            libraryParameter.terminologyUrlHeaders = Arrays.stream(terminologyUrlHeaders.split(",")).map(String::trim).collect(Collectors.toList());
        }

        libraries.add(libraryParameter);

        List<String> cqlVariables = Arrays.stream(cqlVariablesToReturn.split(",")).map(String::trim).collect(Collectors.toList());

        Map<String, String> newMap = new HashMap<>();

        try {
            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();

            myLogger.info("Received result from CQL Engine={} for bundle={}",
                    FhirJsonExporter.getMapSetAsJson(fhirVersion, entrySet),
                    fhirBundle);

            for (Map.Entry<String, Object> libraryEntry : entrySet) {
                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();

                if ("Patient".equals(key)) {
                    Patient patient = (Patient) value;
                    myLogger.info("Received Patient in CQL result={}",
                            patient != null ? FhirJsonExporter.getResourceAsJson(fhirVersion, patient) : null);
                    newMap.put("PatientId", (patient != null) ? patient.getId() : null);
                } else {
                    if (cqlVariables.contains(key)) {
                        newMap.put(key, value != null ? value.toString() : null);
                    }
                }
            }
        } catch (Exception ex) {
            myLogger.error("Error={} for bundle={}", ex, fhirBundle);
            throw ex;
        }

        myLogger.info("Calculated CQL variables={} for bundle={}", FhirJsonExporter.getMapAsJson(newMap), fhirBundle);

        return newMap;
    }
}

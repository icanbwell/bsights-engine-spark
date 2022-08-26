package com.bwell.services.application;

import com.bwell.auth.model.Client;
import com.bwell.auth.service.AuthService;
import com.bwell.core.entities.ContextParameter;
import com.bwell.core.entities.LibraryParameter;
import com.bwell.core.entities.ModelParameter;
import com.bwell.infrastructure.FhirJsonExporter;
import com.bwell.services.domain.CqlService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.springframework.http.HttpHeaders;

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
     * @param libraryName:           name of the CQL library to run
     * @param libraryVersion:        version of the CQL library to run
     * @param terminologyUrl:        URL to FHIR server that has value sets
     * @param cqlVariablesToReturn:  list of the CQL variables that we should return the values for
     * @param fhirBundle:            FHIR resource bundle as a string
     * @param authUrl:               URL for Auth Server
     * @param authId:                Client ID for Auth Server
     * @param authSecret:            Client Secret for Auth Server
     * @param authScope:             Client Scope for Auth Server
     * @param clientTimeout:         Timeout for the FHIR Server
     * @param contextName:           Optional context name
     * @param contextValue:          Optional context value
     * @return map (dictionary) of CQL variable name, value
     * @throws Exception exception
     */
    public Map<String, String> runCqlLibrary(
            String libraryUrl,
            String libraryName,
            String libraryVersion,
            String terminologyUrl,
            String cqlVariablesToReturn,
            String fhirBundle,
            String authUrl,
            String authId,
            String authSecret,
            String authScope,
            String clientTimeout,
            String contextName,
            String contextValue
    ) throws Exception {
        String fhirVersion = "R4";

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryUrl = libraryUrl;
        libraryParameter.libraryVersion = libraryVersion;
        libraryParameter.terminologyUrl = terminologyUrl;
        libraryParameter.model = new ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelBundle = fhirBundle;
        libraryParameter.clientTimeout = Integer.parseInt(clientTimeout);
        libraryParameter.context = new ContextParameter();
        if (contextName != null) {
            libraryParameter.context.contextName = contextName;
        }
        if (contextValue != null) {
            libraryParameter.context.contextValue = contextValue;
        }

        myLogger.info("Running with libraryUrl={}, terminologyUrl={}, fhir={}", libraryUrl, terminologyUrl, fhirBundle);

        if(authUrl != null){
            List<String> headers = headers(authUrl, authId, authSecret, authScope);
            libraryParameter.libraryUrlHeaders = headers;
            libraryParameter.terminologyUrlHeaders = headers;
        }

        List<String> cqlVariables = Arrays.stream(cqlVariablesToReturn.split(",")).map(String::trim).collect(Collectors.toList());

        Map<String, String> newMap = new HashMap<>();

        try {
            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraryParameter);

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
                    newMap.put("PatientId", (patient != null) ? patient.getIdElement().getIdPart() : null);
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

    private List<String> headers(String authUrl,
                                 String authId,
                                 String authSecret,
                                 String authScope) throws JsonProcessingException {
        //Grab Token and turn into Header
        AuthService authService = new AuthService();

        Client client = new Client();
        client.setId(authId);
        client.setSecret(authSecret);
        client.setAuthScopes(authScope);
        client.setAuthServerUrl(authUrl);

        /*
            Build Header with Auth

            Building our own way of Header here, as Spring builds the Bearer header without a :
            but, the CQL Code requires the : - so, catch 22
         */
        return List.of(String.format("%s: Bearer %s", HttpHeaders.AUTHORIZATION, authService.getToken(client)));
    }
}

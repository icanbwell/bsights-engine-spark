package com.bwell;

import ca.uhn.fhir.context.FhirVersionEnum;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.exception.CqlException;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.evaluator.CqlEvaluator;
import org.opencds.cqf.cql.evaluator.builder.Constants;
import org.opencds.cqf.cql.evaluator.builder.CqlEvaluatorBuilder;
import org.opencds.cqf.cql.evaluator.builder.DataProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.EndpointInfo;
import org.opencds.cqf.cql.evaluator.cql2elm.content.LibraryContentProvider;
import org.opencds.cqf.cql.evaluator.dagger.CqlEvaluatorComponent;
import org.opencds.cqf.cql.evaluator.dagger.DaggerCqlEvaluatorComponent;

import java.util.*;

public class CqlRunner {

    static public class LibraryParameter {
        public String libraryUrl;
        public String libraryName;
        public String libraryVersion;
        public String terminologyUrl;
        public ModelParameter model;
        public ContextParameter context;

        public static class ContextParameter {
            public String contextName;

            public String contextValue;
        }

        public static class ModelParameter {
            public String modelName;

            public String modelUrl;
            public String modelBundle;
        }
    }

    private Map<String, LibraryContentProvider> libraryContentProviderIndex = new HashMap<>();
    private Map<String, TerminologyProvider> terminologyProviderIndex = new HashMap<>();

    public EvaluationResult runCql(String fhirVersion, List<LibraryParameter> libraries) {
        FhirVersionEnum fhirVersionEnum = FhirVersionEnum.valueOf(fhirVersion);

        // create an evaluator with the passed in fhirVersion
        CqlEvaluatorComponent cqlEvaluatorComponent = DaggerCqlEvaluatorComponent.builder()
                .fhirContext(fhirVersionEnum.newContext()).build();

        // load cql libraries
        for (LibraryParameter library : libraries) {

            // create a cql evaluator builder
            CqlEvaluatorBuilder cqlEvaluatorBuilder = cqlEvaluatorComponent.createBuilder();

            // check if it is in the cache
            LibraryContentProvider libraryContentProvider = libraryContentProviderIndex.get(library.libraryUrl);

            // if not in cache then load it
            if (libraryContentProvider == null) {
                libraryContentProvider = cqlEvaluatorComponent.createLibraryContentProviderFactory()
                        .create(new EndpointInfo().setAddress(library.libraryUrl));
                // put it in cache
                libraryContentProviderIndex.put(library.libraryUrl, libraryContentProvider);
            }

            // add libraries to cql evaluator builder
            cqlEvaluatorBuilder.withLibraryContentProvider(libraryContentProvider);

            // load terminology
            if (library.terminologyUrl != null) {
                // check if terminology is in cache
                TerminologyProvider terminologyProvider = terminologyProviderIndex.get(library.terminologyUrl);
                if (terminologyProvider == null) {
                    // if terminology is not in cache then load ut
                    terminologyProvider = cqlEvaluatorComponent.createTerminologyProviderFactory()
                            .create(new EndpointInfo().setAddress(library.terminologyUrl));
                    // add to cache
                    terminologyProviderIndex.put(library.terminologyUrl, terminologyProvider);
                }

                // add terminology to cql evaluator builder
                cqlEvaluatorBuilder.withTerminologyProvider(terminologyProvider);
            }

            // load the data to evaluate
            Triple<String, ModelResolver, RetrieveProvider> dataProvider = null;
            DataProviderFactory dataProviderFactory = cqlEvaluatorComponent.createDataProviderFactory();
            if (library.model != null) {
                // if model is provided as text then use it
                if (library.model.modelBundle != null) {
                    IBaseBundle bundle = ResourceLoader.loadResourceFromString(library.model.modelBundle);
                    dataProvider = dataProviderFactory.create(bundle);
                } else {
                    // load model from url
                    dataProvider = dataProviderFactory.create(new EndpointInfo().setAddress(library.model.modelUrl));
                }
            }
            // default to FHIR
            else {
                dataProvider = dataProviderFactory.create(new EndpointInfo().setType(Constants.HL7_FHIR_FILES_CODE));
            }

            // add data to cql evaluator builder
            cqlEvaluatorBuilder.withModelResolverAndRetrieveProvider(dataProvider.getLeft(), dataProvider.getMiddle(),
                    dataProvider.getRight());

            // build the evaluator
            CqlEvaluator evaluator = cqlEvaluatorBuilder.build();

            VersionedIdentifier identifier = new VersionedIdentifier().withId(library.libraryName);
            if (library.libraryVersion != null) {
                identifier = identifier.withVersion(library.libraryVersion);
            }

            // add any context parameters
            Pair<String, Object> contextParameter = null;

            if (library.context != null) {
                contextParameter = Pair.of(library.context.contextName, library.context.contextValue);
            }

            try {
                // run evaluator and return result
                return evaluator.evaluate(identifier, contextParameter);
            } catch (Exception e) {
                int foo = 1;
                throw e;
            }

        }
        return null;
    }

    Map<String, String> runCql2(String cqlLibraryUrl, String terminologyUrl, String fhirBundle) {
        java.util.Map<String, String> newMap = new java.util.HashMap<>();
        newMap.put("key1", fhirBundle + "_1");
        newMap.put("key2", fhirBundle + "_2");

        return newMap;
    }

    /**
     * Runs the CQL Library
     *
     * @param cqlLibraryUrl:     link to fhir server that holds the CQL library
     * @param cqlLibraryName:    name of cql library
     * @param cqlLibraryVersion: version of cql library
     * @param terminologyUrl:    link to fhir server that holds the value set
     * @param cqlVariablesToReturn: comma separated list of cql variables.  This functions returns a dictionary of values for these
     * @param fhirBundle:        FHIR bundle that contains the patient resource and any related resources like observations, conditions etc
     * @return
     * @throws Exception
     */
    Map<String, String> runCqlLibrary(
            String cqlLibraryUrl,
            String cqlLibraryName,
            String cqlLibraryVersion,
            String terminologyUrl,
            String cqlVariablesToReturn,
            String fhirBundle
    ) throws Exception {
        String fhirVersion = "R4";
        List<CqlRunner.LibraryParameter> libraries = new ArrayList<>();
        CqlRunner.LibraryParameter libraryParameter = new CqlRunner.LibraryParameter();
        libraryParameter.libraryName = cqlLibraryName;

        libraryParameter.libraryUrl = cqlLibraryUrl;
        libraryParameter.libraryVersion = cqlLibraryVersion;
        libraryParameter.terminologyUrl = terminologyUrl;
        libraryParameter.model = new CqlRunner.LibraryParameter.ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelBundle = fhirBundle;
        libraryParameter.context = new CqlRunner.LibraryParameter.ContextParameter();
        libraryParameter.context.contextName = "Patient";
        libraryParameter.context.contextValue = "example";

        libraries.add(libraryParameter);

        List<String> cqlVariables = Arrays.asList( cqlVariablesToReturn.split(","));

        try {
            EvaluationResult result = new CqlRunner().runCql(fhirVersion, libraries);
            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
            for (Map.Entry<String, Object> libraryEntry : entrySet) {
                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();
                if (cqlVariables.contains(key)) {
                    Patient patient = (Patient) value;
                    String identifier_value = patient.getIdentifier().get(0).getValue();
                }
            }
        } catch (CqlException e) {
            if (Objects.equals(e.getMessage(), "Unexpected exception caught during execution: ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException: HTTP 404 Not Found")) {
                throw new Exception("NOTE: Did you run make loadfhir to load the fhir server?");
            } else {
                throw e;
            }

        }

        java.util.Map<String, String> newMap = new java.util.HashMap<>();
        newMap.put("key1", fhirBundle + "_1");
        newMap.put("key2", fhirBundle + "_2");

        return newMap;
    }
}

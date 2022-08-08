package com.bwell.services.domain;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import com.bwell.core.entities.LibraryParameter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBaseBundle;
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

import java.io.IOException;
import java.util.*;

public class CqlService {

    private static final org.slf4j.Logger myLogger = org.slf4j.LoggerFactory.getLogger(CqlService.class);

    private static final Object lock = new Object();

    // private instance, so that it can be
    // accessed by only by getInstance() method
    private static volatile FhirContext fhirContextSharedInstance;

    public static FhirContext getFhirContext(String fhirVersion)
    {
        if (fhirContextSharedInstance == null)
        {
            //synchronized block to remove overhead
            synchronized (FhirContext.class)
            {
                if(fhirContextSharedInstance ==null)
                {
                    FhirVersionEnum fhirVersionEnum = FhirVersionEnum.valueOf(fhirVersion);
                    // if instance is null, initialize
                    fhirContextSharedInstance = fhirVersionEnum.newContext();
                }

            }
        }
        return fhirContextSharedInstance;
    }
    /**
     * Runs the CQL library
     *
     * @param fhirVersion version of FHIR
     * @param libraries   list of libraries
     * @return result of evaluation
     */
    public EvaluationResult runCqlLibrary(String fhirVersion, List<LibraryParameter> libraries) throws IOException {

        // first create a FhirContext for this version
        // This is expensive so make it static lazy init
        // https://hapifhir.io/hapi-fhir/apidocs/hapi-fhir-base/ca/uhn/fhir/context/FhirContext.html
        FhirContext fhirContext = getFhirContext(fhirVersion);
//        fhirContext.setRestfulClientFactory();

        // create an evaluator with the FhirContext
        CqlEvaluatorComponent cqlEvaluatorComponent = DaggerCqlEvaluatorComponent.builder()
                .fhirContext(fhirContext).build();

        // load cql libraries
        for (LibraryParameter library : libraries) {
            CqlEvaluator evaluator = null;

            //noinspection CaughtExceptionImmediatelyRethrown
            try {
                evaluator = buildCqlEvaluator(fhirVersion, cqlEvaluatorComponent, library);
            } catch (Exception ex) {
                myLogger.error("Error runCqlLibrary:buildCqlEvaluator(): {}", ex.toString());
                throw ex;
            }

            //noinspection CaughtExceptionImmediatelyRethrown
            try {
                VersionedIdentifier identifier = new VersionedIdentifier().withId(library.libraryName);
                if (library.libraryVersion != null) {
                    identifier = identifier.withVersion(library.libraryVersion);
                }

                // add any context parameters
                Pair<String, Object> contextParameter = null;
                if (library.context != null) {
                    contextParameter = Pair.of(library.context.contextName, library.context.contextValue);
                }

                // run evaluator and return result
                return evaluator.evaluate(identifier, contextParameter);
            } catch (Exception ex) {
                myLogger.error("Error runCqlLibrary:evaluator.evaluate(): {}", ex.toString());
                throw ex;
            }

        }

        return null;
    }

    /**
     * Builds an evaluator using the configuration values passed in.
     * Uses synchronized to avoid multiple threads updating the underlying caches at the same time
     *
     * @param cqlEvaluatorComponent evaluator component
     * @param library               library configuration
     * @return a CqlEvaluator built with the passed in configuration
     */
    private CqlEvaluator buildCqlEvaluator(String fhirVersion, CqlEvaluatorComponent cqlEvaluatorComponent, LibraryParameter library) throws IOException {
        synchronized (lock) {
            // create a cql evaluator builder
            CqlEvaluatorBuilder cqlEvaluatorBuilder = cqlEvaluatorComponent.createBuilder();

            // load CQL library
            if (library.libraryUrl != null) {
                EndpointInfo endpointInfo = new EndpointInfo().setAddress(library.libraryUrl);
                if (library.libraryUrlHeaders != null && library.libraryUrlHeaders.size() > 0) {
                    endpointInfo.setHeaders(library.libraryUrlHeaders);
                }

                LibraryContentProvider libraryContentProvider = cqlEvaluatorComponent.createLibraryContentProviderFactory().create(endpointInfo);

                // add libraries to cql evaluator builder
                cqlEvaluatorBuilder.withLibraryContentProvider(libraryContentProvider);
            }

            // load terminology
            if (library.terminologyUrl != null) {
                EndpointInfo endpointInfo = new EndpointInfo().setAddress(library.terminologyUrl);
                if (library.terminologyUrlHeaders != null && library.terminologyUrlHeaders.size() > 0) {
                    endpointInfo.setHeaders(library.terminologyUrlHeaders);
                }

                TerminologyProvider terminologyProvider = cqlEvaluatorComponent.createTerminologyProviderFactory().create(endpointInfo);

                // add terminology to cql evaluator builder
                cqlEvaluatorBuilder.withTerminologyProvider(terminologyProvider);
            }

            // load the data to evaluate
            Triple<String, ModelResolver, RetrieveProvider> dataProvider;
            DataProviderFactory dataProviderFactory = cqlEvaluatorComponent.createDataProviderFactory();
            if (library.model != null) {
                // if model is provided as text then use it
                if (library.model.modelBundle != null) {
                    IBaseBundle bundle = new ResourceLoader().loadResourceFromString(fhirVersion, library.model.modelBundle);
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
            return cqlEvaluatorBuilder.build();
        }
    }
}

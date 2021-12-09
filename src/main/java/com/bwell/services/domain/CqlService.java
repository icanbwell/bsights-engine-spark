package com.bwell.services.domain;

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

import java.util.*;

public class CqlService {

    private final Map<String, LibraryContentProvider> libraryContentProviderIndex = new HashMap<>();
    private final Map<String, TerminologyProvider> terminologyProviderIndex = new HashMap<>();

    public EvaluationResult runCqlLibrary(String fhirVersion, List<LibraryParameter> libraries) {
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
            Triple<String, ModelResolver, RetrieveProvider> dataProvider;
            DataProviderFactory dataProviderFactory = cqlEvaluatorComponent.createDataProviderFactory();
            if (library.model != null) {
                // if model is provided as text then use it
                if (library.model.modelBundle != null) {
                    IBaseBundle bundle = new ResourceLoader().loadResourceFromString(library.model.modelBundle);
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
                throw e;
            }

        }
        return null;
    }
}

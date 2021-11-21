package com.bwell;

import ca.uhn.fhir.context.FhirVersionEnum;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.formats.JsonParser;
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

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            public String modelText;
        }
    }
    private Map<String, LibraryContentProvider> libraryContentProviderIndex = new HashMap<>();
    private Map<String, TerminologyProvider> terminologyProviderIndex = new HashMap<>();

    public EvaluationResult runCql(String fhirVersion, List<LibraryParameter> libraries) {
        FhirVersionEnum fhirVersionEnum = FhirVersionEnum.valueOf(fhirVersion);

        CqlEvaluatorComponent cqlEvaluatorComponent = DaggerCqlEvaluatorComponent.builder()
                .fhirContext(fhirVersionEnum.newContext()).build();

        for (LibraryParameter library : libraries) {

            CqlEvaluatorBuilder cqlEvaluatorBuilder = cqlEvaluatorComponent.createBuilder();

            LibraryContentProvider libraryContentProvider = libraryContentProviderIndex.get(library.libraryUrl);

            if (libraryContentProvider == null) {
                libraryContentProvider = cqlEvaluatorComponent.createLibraryContentProviderFactory()
                        .create(new EndpointInfo().setAddress(library.libraryUrl));
                libraryContentProviderIndex.put(library.libraryUrl, libraryContentProvider);
            }

            cqlEvaluatorBuilder.withLibraryContentProvider(libraryContentProvider);

            if (library.terminologyUrl != null) {
                TerminologyProvider terminologyProvider = terminologyProviderIndex.get(library.terminologyUrl);
                if (terminologyProvider == null) {
                    terminologyProvider = cqlEvaluatorComponent.createTerminologyProviderFactory()
                            .create(new EndpointInfo().setAddress(library.terminologyUrl));
                    terminologyProviderIndex.put(library.terminologyUrl, terminologyProvider);
                }

                cqlEvaluatorBuilder.withTerminologyProvider(terminologyProvider);
            }

            Triple<String, ModelResolver, RetrieveProvider> dataProvider = null;
            DataProviderFactory dataProviderFactory = cqlEvaluatorComponent.createDataProviderFactory();
            // check if th model is provided as text then just use that

            if (library.model != null) {
                if (library.model.modelText != null){
                    File f = new File("/usr/local/bin/geeks");
                    String resource = FileUtils.readFileToString(f, Charset.forName("UTF-8"));
                    JsonParser parser = new JsonParser();
                    IBaseBundle bundle = parser.parse(resource);
                    dataProvider = dataProviderFactory.create(bundle);
                }
                else {
                    dataProvider = dataProviderFactory.create(new EndpointInfo().setAddress(library.model.modelUrl));

                }
            }
            // default to FHIR
            else {
                dataProvider = dataProviderFactory.create(new EndpointInfo().setType(Constants.HL7_FHIR_FILES_CODE));
            }

            cqlEvaluatorBuilder.withModelResolverAndRetrieveProvider(dataProvider.getLeft(), dataProvider.getMiddle(),
                    dataProvider.getRight());

            CqlEvaluator evaluator = cqlEvaluatorBuilder.build();

            VersionedIdentifier identifier = new VersionedIdentifier().withId(library.libraryName);

            Pair<String, Object> contextParameter = null;

            if (library.context != null) {
                contextParameter = Pair.of(library.context.contextName, library.context.contextValue);
            }

            return evaluator.evaluate(identifier, contextParameter);
        }
        return null;
    }
}

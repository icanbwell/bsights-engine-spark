package com.bwell.measure;

import com.bwell.core.entities.LibraryParameter;
import com.bwell.core.entities.ModelParameter;
import com.bwell.services.domain.CqlService;
import com.bwell.utilities.Utilities;

import org.json.JSONException;
import org.opencds.cqf.cql.engine.exception.CqlException;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.*;


public class BaseTest {
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private static final String fhirVersion = "R4";
    private static final String modelName = "FHIR";
    private static final String fhirServerUrl = "http://fhir:3000/4_0_0";
    private static final String testResourceRelativePath = "src/test/resources";

    @SuppressWarnings("FieldCanBeLocal")
    private String folder = null;
    private String libraryName = null;
    private String libraryVersion = null;
    private String terminologyPath = null;
    private String cqlPath = null;
    private String bundleJson = null;
    private String bundleContainedJson = null;

    // Maven testing fixture - BeforeMethod
    @BeforeMethod
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    // Maven testing fixture - AfterMethod
    @AfterMethod
    public void restoreStreams() {
        String sysOut = outContent.toString();
        String sysError = errContent.toString();

        System.setOut(originalOut);
        System.setErr(originalErr);

        System.out.println(sysOut);
        System.err.println(sysError);
    }

    // Call this method in a setup method with @BeforeClass annotation in the child class
    public void config(String folder, String libraryName, String libraryVersion) throws JSONException {
        this.folder = folder;
        this.libraryName = libraryName;
        this.libraryVersion = libraryVersion;

        File file = new File(testResourceRelativePath);
        String testResourcePath = file.getAbsolutePath();
        System.out.printf("Test resource directory: %s%n", testResourcePath);

        terminologyPath = testResourcePath + "/" + this.folder + "/terminology";
        cqlPath = testResourcePath + "/" + this.folder + "/cql";

        bundleJson = Utilities.getBundle(testResourcePath, this.folder);
        bundleContainedJson = Utilities.getContainedBundle(testResourcePath, this.folder);
    }

    // Override this method in the child class with assert logics in the method body
    public void assertExpressionResults(String key, Object value) {
    }

    // Call this method in test methods with @Test annotation in the child class
    public void testRunCqlLibrary(
            Boolean useMockTerminologyJson, Boolean useMockCqlJson,
            Boolean useBundleJson, Boolean useContainedBundleJson
    ) throws Exception {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = this.libraryName;
        libraryParameter.libraryVersion = this.libraryVersion;
        libraryParameter.libraryUrl = useMockCqlJson ? cqlPath : fhirServerUrl;
        libraryParameter.terminologyUrl = useMockTerminologyJson ? terminologyPath : fhirServerUrl;
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
        libraryParameter.model.modelBundle = useBundleJson ? bundleJson : ( useContainedBundleJson ? bundleContainedJson : null);

        libraries.add(libraryParameter);

        try {

            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();

            for (Map.Entry<String, Object> libraryEntry : entrySet) {

                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();

                assertExpressionResults(key, value);

                System.out.println(key + "=" + Utilities.tempConvert(value));
            }
        } catch (CqlException e) {
            if (Objects.equals(e.getMessage(), "Unexpected exception caught during execution: ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException: HTTP 404 Not Found")) {
                throw new Exception("NOTE: Did you run make loadfhir to load the fhir server?");
            }
            else {
                throw e;
            }

        }

        System.out.println();
    }
}

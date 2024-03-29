package com.bwell.services.application;

import com.bwell.core.entities.ContextParameter;
import com.bwell.core.entities.LibraryParameter;
import com.bwell.core.entities.ModelParameter;
import com.bwell.services.domain.CqlService;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class ResourceRunnerTest {
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private static final String testResourceRelativePath = "src/test/resources";
    private static String testResourcePath = null;

    @BeforeClass
    public void setup() {
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.printf("Test resource directory: %s%n", testResourcePath);
    }

    @BeforeMethod
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterMethod
    public void restoreStreams() {
        String sysOut = outContent.toString();
        String sysError = errContent.toString();

        System.setOut(originalOut);
        System.setErr(originalErr);

        System.out.println(sysOut);
        System.err.println(sysError);
    }

    @Test
    public void testR4() throws IOException {
        String fhirVersion = "R4";
        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = "TestFHIR";
        libraryParameter.libraryUrl = testResourcePath + "/r4";
        // libraryParameter.libraryVersion = libraryParameter.libraryVersion;
        libraryParameter.terminologyUrl = testResourcePath + "/r4/vocabulary/ValueSet";
        libraryParameter.model = new ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelUrl = testResourcePath + "/r4";
        libraryParameter.context = new ContextParameter();
        libraryParameter.context.contextName = "Patient";
        // [FINDINGs]
        // FHIR resource json data to be tested should have this contextValue as their "id" values,
        // because this test method would find and load those FHIR resource JSON data with the same "context" data,
        //  for instance, to test unitypoint FHIR data, Patient, Encounter, and ChargeItem data have the "example-unitypoint" as "id" value.
        //  when running this testR4 method, you will see only the FHIR data under this context, "example-unitypoint" will be loaded
        libraryParameter.context.contextValue = "example-unitypoint"; // previously, "example"

        EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraryParameter);

        Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
        for (Map.Entry<String, Object> libraryEntry : entrySet) {
            String key = libraryEntry.getKey();
            Object value = libraryEntry.getValue();
            if (key.equals("Patient")) {
                Patient patient = (Patient) value;
                String identifier_value = patient.getIdentifier().get(0).getValue();
                System.out.println(key + "Id = " + identifier_value);
                assertEquals(identifier_value, "E900019216");  // previously, "12345" for "example" Patient
            }
            System.out.println(key + "=" + tempConvert(value));
        }

        System.out.println();

    }

    @Test
    public void testR4WithHelpers() throws IOException {
        String fhirVersion = "R4";
        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = "TestFHIRWithHelpers";
        libraryParameter.libraryUrl = testResourcePath + "/r4";
//        libraryParameter.libraryVersion = libraryParameter.libraryVersion;
        libraryParameter.terminologyUrl = testResourcePath + "/r4/vocabulary/ValueSet";
        libraryParameter.model = new ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelUrl = testResourcePath + "/r4";
        libraryParameter.context = new ContextParameter();
        libraryParameter.context.contextName = "Patient";
        libraryParameter.context.contextValue = "example";

        EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraryParameter);

        Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
        for (Map.Entry<String, Object> libraryEntry : entrySet) {
            String key = libraryEntry.getKey();
            Object value = libraryEntry.getValue();
            if (key.equals("Patient")) {
                Patient patient = (Patient) value;
                String identifier_value = patient.getIdentifier().get(0).getValue();
                System.out.println(key + "Id = " + identifier_value);
                assertEquals(identifier_value, "12345");
            }
            System.out.println(key + "=" + tempConvert(value));
        }

        System.out.println();

    }

    private String tempConvert(Object value) {
        if (value == null) {
            return "null";
        }

        String result = "";
        if (value instanceof Iterable) {
            result += "[";
            Iterable<?> values = (Iterable<?>) value;
            for (Object o : values) {

                result += (tempConvert(o) + ", ");
            }

            if (result.length() > 1) {
                result = result.substring(0, result.length() - 2);
            }

            result += "]";
        } else if (value instanceof IBaseResource) {
            IBaseResource resource = (IBaseResource) value;
            result = resource.fhirType() + (resource.getIdElement() != null && resource.getIdElement().hasIdPart()
                    ? "(id=" + resource.getIdElement().getIdPart() + ")"
                    : "");
        } else if (value instanceof IBase) {
            result = ((IBase) value).fhirType();
        } else if (value instanceof IBaseDatatype) {
            result = ((IBaseDatatype) value).fhirType();
        } else {
            result = value.toString();
        }

        return result;
    }

}
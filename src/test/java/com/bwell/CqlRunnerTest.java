package com.bwell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CqlRunnerTest {
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
        System.out.println(String.format("Test resource directory: %s", testResourcePath));
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
    public void testR4() {
        String fhirVersion = "R4";
        List<CqlRunner.LibraryParameter> libraries = new ArrayList<>();
        CqlRunner.LibraryParameter libraryParameter = new CqlRunner.LibraryParameter();
        libraryParameter.libraryName = "TestFHIR";
        libraryParameter.libraryUrl = testResourcePath + "/r4";
//        libraryParameter.libraryVersion = libraryParameter.libraryVersion;
        libraryParameter.terminologyUrl = testResourcePath + "/r4/vocabulary/ValueSet";
        libraryParameter.model = new CqlRunner.LibraryParameter.ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelUrl = testResourcePath + "/r4";
        libraryParameter.context = new CqlRunner.LibraryParameter.ContextParameter();
        libraryParameter.context.contextName = "Patient";
        libraryParameter.context.contextValue = "example";

        libraries.add(libraryParameter);

        EvaluationResult result = new CqlRunner().runCql(fhirVersion, libraries);

        Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
        for (Map.Entry<String, Object> libraryEntry : entrySet) {
            String key = libraryEntry.getKey();
            Object value = libraryEntry.getValue();
            if (key.equals("Patient")) {
                Patient patient = (Patient) value;
                String identifier_value = patient.getIdentifier().get(0).getValue();
                System.out.println(key + " = " + identifier_value);
                assertEquals(identifier_value, "12345");
            }
            System.out.println(key + "=" + tempConvert(value));
        }

        System.out.println();

//        String output = outContent.toString();
//
//        assertTrue(output.contains("Patient=Patient(id=example)"));
//        assertTrue(output.contains("TestAdverseEvent=[AdverseEvent(id=example)]"));
//        assertTrue(output.contains("TestPatientGender=Patient(id=example)"));
//        assertTrue(output.contains("TestPatientActive=Patient(id=example)"));
//        assertTrue(output.contains("TestPatientBirthDate=Patient(id=example)"));
//        assertTrue(output.contains("TestPatientMaritalStatusMembership=Patient(id=example)"));
//        assertTrue(output.contains("TestPatientMartialStatusComparison=Patient(id=example)"));
//        assertTrue(output.contains("TestPatientDeceasedAsBoolean=Patient(id=example)"));
//        assertTrue(output.contains("TestPatientDeceasedAsDateTime=null"));
//        assertTrue(output.contains("TestSlices=[Observation(id=blood-pressure)]"));
//        assertTrue(output.contains("TestSimpleExtensions=Patient(id=example)"));
//        assertTrue(output.contains("TestComplexExtensions=Patient(id=example)"));
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

    @Test
    public void testLoadingBundle() {
        File f = new File(testResourcePath + "/bmi001" + "/example/expected1.json");
        String resource = null;
        try {
            resource = FileUtils.readFileToString(f, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonParser parser = new JsonParser();
        IBaseBundle bundle = null;
        try {
            bundle = (IBaseBundle) parser.parse(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(bundle);
        String patient_first_identifier = ((Identifier) ((java.util.ArrayList) ((Patient) ((Bundle.BundleEntryComponent) ((java.util.ArrayList) ((Bundle) bundle).getEntry()).get(0)).getResource()).getIdentifier()).get(0)).getValue();
        assertEquals("M888888", patient_first_identifier);
    }
}

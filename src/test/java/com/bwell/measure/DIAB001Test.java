package com.bwell.measure;

import com.bwell.core.entities.ContextParameter;
import com.bwell.core.entities.LibraryParameter;
import com.bwell.core.entities.ModelParameter;
import com.bwell.services.domain.CqlService;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.exception.CqlException;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.testng.Assert.assertEquals;

public class DIAB001Test {
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
    public void testDIAB001Bundle() {

        String fhirVersion = "R4";
        List<LibraryParameter> libraries = new ArrayList<>();
        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = "DIAB001";
        String folder = "diab001";

        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected.json");
        String bundleJson = null;
        try {
            bundleJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        libraryParameter.libraryUrl = testResourcePath + "/" + folder + "/cql";
        libraryParameter.model = new ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelBundle = bundleJson;
        libraries.add(libraryParameter);

        EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

        Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
        for (Map.Entry<String, Object> libraryEntry : entrySet) {
            String key = libraryEntry.getKey();
            Object value = libraryEntry.getValue();
            if (key.equals("Patient")) {
                Patient patient = (Patient) value;

                String mr_identifier_value = patient.getIdentifier().get(0).getValue(); // medical record number
                System.out.println(key + ": Medical Record ID = " + mr_identifier_value);
                assertEquals(mr_identifier_value, "12345");

                String patient_id = patient.getId();  // patient id
                System.out.println(key + ": Patient ID = " + patient_id);
                assertEquals(patient_id, "example");
            }
            System.out.println(key + "=" + tempConvert(value));
        }

        System.out.println();
    }

    @Test
    public void testDIAB001BundleCqlFromFhirServer() throws Exception {
        String fhirVersion = "R4";
        List<LibraryParameter> libraries = new ArrayList<>();
        LibraryParameter libraryParameter = new LibraryParameter();
        String folder = "diab001";

        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected.json");
        String bundleJson = null;
        try {
            bundleJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        libraryParameter.libraryUrl = "http://localhost:3000/4_0_0";
        libraryParameter.libraryName = "DIAB001";
        libraryParameter.libraryVersion = "1.0.0";
        libraryParameter.terminologyUrl = testResourcePath + "/" + folder + "/terminology";
        libraryParameter.model = new ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelBundle = bundleJson;
        libraries.add(libraryParameter);

        try {
            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);
            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
            for (Map.Entry<String, Object> libraryEntry : entrySet) {
                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();
                if (key.equals("Patient")) {
                    Patient patient = (Patient) value;
                    String mr_identifier_value = patient.getIdentifier().get(0).getValue(); // medical record number
                    System.out.println(key + ": Medical Record ID = " + mr_identifier_value);
                    assertEquals(mr_identifier_value, "12345");
                    String patient_id = patient.getId();  // patient id
                    System.out.println(key + ": Patient ID = " + patient_id);
                    assertEquals(patient_id, "example");
                }
                System.out.println(key + "=" + tempConvert(value));
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

    @Test
    public void testDIAB001BundleCqlAndTerminologyFromFhirServer() throws Exception {
        String fhirVersion = "R4";
        List<LibraryParameter> libraries = new ArrayList<>();
        LibraryParameter libraryParameter = new LibraryParameter();
        String folder = "diab001";

        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected.json");
        String bundleJson = null;
        try {
            bundleJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        libraryParameter.libraryUrl = "http://localhost:3000/4_0_0";
        libraryParameter.libraryName = "DIAB001";
        libraryParameter.libraryVersion = "1.0.0";
        libraryParameter.terminologyUrl = "http://localhost:3000/4_0_0";
        libraryParameter.model = new ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelBundle = bundleJson;
        libraries.add(libraryParameter);

        try {
            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);
            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
            for (Map.Entry<String, Object> libraryEntry : entrySet) {
                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();
                if (key.equals("Patient")) {
                    Patient patient = (Patient) value;
                    String mr_identifier_value = patient.getIdentifier().get(0).getValue(); // medical record number
                    System.out.println(key + ": Medical Record ID = " + mr_identifier_value);
                    assertEquals(mr_identifier_value, "12345");
                    String patient_id = patient.getId();  // patient id
                    System.out.println(key + ": Patient ID = " + patient_id);
                    assertEquals(patient_id, "example");
                }
                System.out.println(key + "=" + tempConvert(value));
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

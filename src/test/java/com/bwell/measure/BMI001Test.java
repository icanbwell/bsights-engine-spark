package com.bwell.measure;

import com.bwell.core.entities.LibraryParameter;
import com.bwell.core.entities.ModelParameter;
import com.bwell.services.domain.CqlService;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.testng.Assert.assertEquals;

public class BMI001Test {

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private static String fhirVersion = "R4";
    private static String modelName = "FHIR";
    private static String fhirServerUrl = "http://localhost:3000/4_0_0";
    private static final String testResourceRelativePath = "src/test/resources";
    private static String folder = "bmi001";
    private static String libraryName = "BMI001";
    private static String testResourcePath = null;
    private static String bundleJson = null;
    private static String bundleContainedJson = null;


    @BeforeClass
    public void setup() {
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.printf("Test resource directory: %s%n", testResourcePath);

        bundleJson = getBundle(file);
        bundleContainedJson = getContainedBundle(file);

/*        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected.json");
        try {
            bundleJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(bundleJson);
        JSONObject firstItem = (JSONObject) jsonArray.get(0);
        bundleJson = firstItem.getJSONObject("bundle").toString();*/
    }

    private String getBundle(File file) {
        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected.json");
        try {
            bundleJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(bundleJson);
        JSONObject firstItem = (JSONObject) jsonArray.get(0);
        bundleJson = firstItem.getJSONObject("bundle").toString();
        return bundleJson;
    }

    private String getContainedBundle(File file) {
        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected_contained.json");
        try {
            bundleContainedJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(bundleContainedJson);
        JSONObject firstItem = (JSONObject) jsonArray.get(0);
        bundleContainedJson = firstItem.getJSONObject("bundle").toString();
        return bundleContainedJson;
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
    public void testBMI001Bundle() {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryUrl = testResourcePath + "/" + folder + "/cql";
        libraryParameter.terminologyUrl = testResourcePath + "/" + folder + "/terminology";
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
        libraryParameter.model.modelBundle = bundleJson;

        libraries.add(libraryParameter);

        EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

        Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();

        for (Map.Entry<String, Object> libraryEntry : entrySet) {

            String key = libraryEntry.getKey();
            Object value = libraryEntry.getValue();

            if (key.equals("Patient")) {

                Patient patient = (Patient) value;

                // medical record number
                String medicalRecordId = patient.getIdentifier().get(0).getValue();
                System.out.println(key + ": Medical Record ID = " + medicalRecordId);
                assertEquals(medicalRecordId, "12345");

                // patient id
                String patientId = patient.getId();
                System.out.println(key + ": Patient ID = " + patientId);
                assertEquals(patientId, "example");

            }

            System.out.println(key + "=" + tempConvert(value));

        }

        System.out.println();
    }

    @Test
    public void testBMI001BundleTerminologyFromFhirServer() throws Exception {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryUrl = testResourcePath + "/" + folder + "/cql";
        libraryParameter.terminologyUrl = fhirServerUrl;
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
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

                    // medical record number
                    String medicalRecordId = patient.getIdentifier().get(0).getValue();
                    System.out.println(key + ": Medical Record ID = " + medicalRecordId);
                    assertEquals(medicalRecordId, "12345");

                    // patient id
                    String patientId = patient.getId();
                    System.out.println(key + ": Patient ID = " + patientId);
                    assertEquals(patientId, "example");

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
    public void testBMI001BundleCqlFromFhirServer() throws Exception {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryUrl = fhirServerUrl;
        libraryParameter.libraryVersion = "1.0.0";
        libraryParameter.terminologyUrl = testResourcePath + "/" + folder + "/terminology";
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
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

                    // medical record number
                    String medicalRecordId = patient.getIdentifier().get(0).getValue();
                    System.out.println(key + ": Medical Record ID = " + medicalRecordId);
                    assertEquals(medicalRecordId, "12345");

                    // patient id
                    String patientId = patient.getId();
                    System.out.println(key + ": Patient ID = " + patientId);
                    assertEquals(patientId, "example");

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
    public void testBMI001BundleCqlAndTerminologyFromFhirServer() throws Exception {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryUrl = fhirServerUrl;
        libraryParameter.libraryVersion = "1.0.0";
        libraryParameter.terminologyUrl = fhirServerUrl;
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
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
                    assertEquals(patient_id, "1");
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
    public void testBMI001BundleCqlAndTerminologyFromFhirServerWithContainedResources() throws Exception {
  /*       String fhirVersion = "R4";

       String bundleContainedJson = null;
        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected_contained.json");
        try {
            bundleContainedJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(bundleContainedJson);
        JSONObject firstItem = (JSONObject) jsonArray.get(0);
//        bundleJson = firstItem.toString();
        bundleContainedJson = firstItem.getJSONObject("bundle").toString();

        List<LibraryParameter> libraries = new ArrayList<>();
        LibraryParameter libraryParameter = new LibraryParameter();

        libraryParameter.libraryUrl = "http://localhost:3000/4_0_0";
        libraryParameter.libraryName = "BMI001";
        libraryParameter.libraryVersion = "1.0.0";
        libraryParameter.terminologyUrl = "http://localhost:3000/4_0_0";
        libraryParameter.model = new ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelBundle = bundleContainedJson;

        libraries.add(libraryParameter);

*/
        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryUrl = fhirServerUrl;
        libraryParameter.libraryVersion = "1.0.0";
        libraryParameter.terminologyUrl = fhirServerUrl;
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
        libraryParameter.model.modelBundle = bundleContainedJson;

        libraries.add(libraryParameter);

        try {

            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();

            for (Map.Entry<String, Object> libraryEntry : entrySet) {

                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();

                if (key.equals("Patient")) {

                    Patient patient = (Patient) value;

                    // medical record number
                    String medicalRecordId = patient.getIdentifier().get(0).getValue();
                    System.out.println(key + ": Medical Record ID = " + medicalRecordId);
                    assertEquals(medicalRecordId, "12345");

                    // patient id
                    String patientId = patient.getId();
                    System.out.println(key + ": Patient ID = " + patientId);
                    assertEquals(patientId, "example");

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

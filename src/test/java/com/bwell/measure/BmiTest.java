package com.bwell.measure;

import org.hl7.fhir.r4.model.Patient;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BmiTest extends BaseTest {

    private static final String folder = "bmi001";
    private static final String libraryName = "BMI001";
    private static final String libraryVersion = "1.0.0";

    @BeforeClass
    public void setUp() throws JSONException {
        super.config(folder, libraryName, libraryVersion);
    }

    // Override this method in the child class with assert logics in the method body
    public void assertExpressionResults(String key, Object value) {
        if (key.equals("Patient")) {
            Patient patient = (Patient) value;

            // medical record number
            String medicalRecordId = patient.getIdentifier().get(0).getValue();
            System.out.println(key + ": Medical Record ID = " + medicalRecordId);
            assertEquals(medicalRecordId, "12345");

            // patient id
            String patientId = patient.getId();
            System.out.println(key + ": Patient ID = " + patientId);
            assertEquals(patientId, "1");

            // patient active flag
            boolean isActive = patient.getActive();
            System.out.println(key + ": Patient Active = " + isActive);
            assertTrue(isActive);

        }
        if (key.equals("InAgeCohort")) {
            Boolean isInAgeCohort = (Boolean) value;
            System.out.println(key + ": " + isInAgeCohort);
            assertTrue(isInAgeCohort);
        }
        if (key.equals("InObservationCohort")) {
            Boolean isInObservationCohort = (Boolean) value;
            System.out.println(key + ": " + isInObservationCohort);
            assertTrue(isInObservationCohort);
        }
        if (key.equals("InDemographic")) {
            Boolean isInDemographic = (Boolean) value;
            System.out.println(key + ": " + isInDemographic);
            assertTrue(isInDemographic);
        }
    }

    @Test
    public void testBmi_Bundle_WithLocalMockupJson() throws Exception {
        Boolean useMockTerminologyJson = true;
        Boolean useMockCqlJson = true;
        Boolean useBundleJson = true;
        Boolean useContainedBundleJson = false;

        super.testRunCqlLibrary(
                useMockTerminologyJson,
                useMockCqlJson,
                useBundleJson,
                useContainedBundleJson
        );
    }

    @Test
    @Ignore
    public void testBmi_Bundle_WithTerminologyFromFhirServer() throws Exception {
        Boolean useMockTerminologyJson = false; // terminology will be fetched from the fhir server
        Boolean useMockCqlJson = true;
        Boolean useBundleJson = true;
        Boolean useContainedBundleJson = false;

        super.testRunCqlLibrary(
                useMockTerminologyJson,
                useMockCqlJson,
                useBundleJson,
                useContainedBundleJson
        );
    }

    @Test
    public void testBmi_Bundle_WithCqlFromFhirServer() throws Exception {
        Boolean useMockTerminologyJson = true;
        Boolean useMockCqlJson = false;         // cql will be fetched from the fhir server
        Boolean useBundleJson = true;
        Boolean useContainedBundleJson = false;

        super.testRunCqlLibrary(
                useMockTerminologyJson,
                useMockCqlJson,
                useBundleJson,
                useContainedBundleJson
        );
    }

    @Test
    @Ignore
    public void testBmi_Bundle_WithCqlAndTerminologyFromFhirServer() throws Exception {
        Boolean useMockTerminologyJson = false; // terminology will be fetched from the fhir server
        Boolean useMockCqlJson = false;         // cql will be fetched from the fhir server
        Boolean useBundleJson = true;
        Boolean useContainedBundleJson = false;

        super.testRunCqlLibrary(
                useMockTerminologyJson,
                useMockCqlJson,
                useBundleJson,
                useContainedBundleJson
        );
    }

    @Test
    @Ignore
    public void testBmi_ContainedBundle_WithCqlAndTerminologyFromFhirServer() throws Exception {
        Boolean useMockTerminologyJson = false; // terminology will be fetched from the fhir server
        Boolean useMockCqlJson = false;         // cql will be fetched from the fhir server
        Boolean useBundleJson = false;
        Boolean useContainedBundleJson = true;  // load "contained" bundle json for input data

        super.testRunCqlLibrary(
                useMockTerminologyJson,
                useMockCqlJson,
                useBundleJson,
                useContainedBundleJson
        );
    }
}

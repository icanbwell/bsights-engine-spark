package com.bwell.measure;

import org.hl7.fhir.r4.model.Patient;
import org.json.JSONException;
import org.opencds.cqf.cql.engine.runtime.Date;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Awv002Test extends BaseTest {
    private static final String folder = "awvcn";
    private static final String libraryName = "AWVCN002";
    private static final String libraryVersion = "1.1.0";

    @BeforeClass
    public void setUp() throws JSONException {
        super.config(folder, libraryName, libraryVersion);
    }

    // Override this method in the child class with assert logics in the method body
    public void assertExpressionResults(String key, Object value) {
        if (key.equals("Patient")) {

            Patient patient = (Patient) value;

            // EMPI
            String empiId = patient.getIdentifier().get(0).getValue();
            System.out.println(key + ": EMPI ID = " + empiId);
            assertEquals(empiId, "E900019216");

            // patient id
            String patientId = patient.getIdElement().getValue();
            System.out.println(key + ": Patient ID = " + patientId);
            assertEquals(patientId, "Patient/unitypoint-eFQWoGGaBo8dUJyl3DuS7lxGLvVFXjDVWEzu2h9X0DY43/_history/5");

            // patient active flag
            boolean isActive = patient.getActive();
            System.out.println(key + ": Patient Active = " + isActive);
            assertTrue(isActive);
        }

        // InAgeCohort - true
        if (key.equals("InAgeCohort")) {
            Boolean isInAgeCohort = (Boolean) value;
            System.out.println(key + ": " + isInAgeCohort);
            assertTrue(isInAgeCohort);
        }

        // AWEncounters - false
        if (key.equals("AWEncounters")) {
            Boolean awEncounters = (Boolean) value;
            System.out.println(key + ": " + awEncounters);
            assertFalse(awEncounters);
        }

        // AWDateEnc - null
        if (key.equals("AWDateEnc")) {
            Date awDateEnd = (Date) value;
            System.out.println(key + ": " + awDateEnd);
            assertNull(awDateEnd);
        }

        // AWCharges - true
        if (key.equals("AWCharges")) {
            Boolean awCharges = (Boolean) value;
            System.out.println(key + ": " + awCharges);
            assertTrue(awCharges);
        }

        // AWDateCharge - 2021-09-01
        if (key.equals("AWDateCharge")) {
            Date awDateCharge = (Date) value;
            System.out.println(key + ": " + awDateCharge);
            assertEquals(awDateCharge.toString(), "2021-09-01");
        }

        // AWVDates - 2021-09-01
        if (key.equals("AWVDates")) {
            Date awvDates = (Date) value;
            System.out.println(key + ": " + awvDates);
            assertEquals(awvDates.toString(), "2021-09-01");
        }

        // AWVReminder - 2022-06-01
        if (key.equals("AWVReminder")) {
            Date awvReminder = (Date) value;
            System.out.println(key + ": " + awvReminder);
            assertEquals(awvReminder.toString(), "2022-06-01");
        }

        // HadAWV1year - true
        if (key.equals("HadAWV1year")) {
            Boolean hadAWV1year = (Boolean) value;
            System.out.println(key + ": " + hadAWV1year);
            assertTrue(hadAWV1year);
        }

        // NeedAWV1year - false
        if (key.equals("NeedAWV1year")) {
            Boolean needAWV1year = (Boolean) value;
            System.out.println(key + ": " + needAWV1year);
            assertFalse(needAWV1year);
        }

        // Newly added def variables for version 1.1.0

        // EligiblePopulation
        if (key.equals("EligiblePopulation")) {
            Boolean isEligiblePopulation = (Boolean) value;
            System.out.println(key + ": " + isEligiblePopulation);
            assertTrue(isEligiblePopulation);
        }

        // Denominator
        if (key.equals("Denominator")) {
            Integer isDenominator = (Integer) value;
            System.out.println(key + ": " + isDenominator);
            assertEquals(isDenominator, 1);
        }

        // Numerator
        if (key.equals("Numerator")) {
            Integer isNumerator = (Integer) value;
            System.out.println(key + ": " + isNumerator);
            assertEquals(isNumerator, 1);
        }

        // RequiredExclusions
        if (key.equals("RequiredExclusions")) {
            Boolean requiredExclusions = (Boolean) value;
            System.out.println(key + ": " + requiredExclusions);
            assertFalse(requiredExclusions);
        }

    }

    @Test
    public void testAwv_Bundle_WithLocalMockupJson() throws Exception {
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
    public void testAwv_Bundle_WithTerminologyFromFhirServer() throws Exception {
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
    public void testAwv_Bundle_WithCqlFromFhirServer() throws Exception {
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
    public void testAwv_Bundle_WithCqlAndTerminologyFromFhirServer() throws Exception {
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
    public void testAwv_ContainedBundle_WithCqlAndTerminologyFromFhirServer() throws Exception {
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

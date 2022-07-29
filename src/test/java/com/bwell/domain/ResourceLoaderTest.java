package com.bwell.domain;

import com.bwell.services.domain.ResourceLoader;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ResourceLoaderTest {
    private static final String testResourceRelativePath = "src/test/resources";
    private static String testResourcePath = null;
    private static final String fhirVersion = "R4";
    private static final String folder = "bmi";

    @BeforeClass
    public void setup() {
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.printf("Test resource directory: %s%n", testResourcePath);
    }

    private String loadBundleJson(String fileName) throws JSONException, IOException  {
        String bundleJson = null;

        File f = new File(testResourcePath + "/" + folder + "/bundles/" + fileName);
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

    private String loadJson(String fileName)  {
        String json = null;

        File f = new File(testResourcePath + "/" + folder + "/bundles/" + fileName);
        try {
            json = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    @Test
    public void testLoadingBundle() throws JSONException, IOException {
        String bundleJson = loadBundleJson("expected.json");

        IBaseBundle bundle = new ResourceLoader().loadResourceFromString(fhirVersion, bundleJson);
        assertNotNull(bundle);
        ArrayList<Bundle.BundleEntryComponent> entry = (ArrayList<Bundle.BundleEntryComponent>) ((Bundle) bundle).getEntry();
        Bundle.BundleEntryComponent bundleEntryComponent = entry.get(0);
        Patient patient = (Patient) bundleEntryComponent.getResource();
        ArrayList<org.hl7.fhir.r4.model.Identifier> identifier = (ArrayList<org.hl7.fhir.r4.model.Identifier>) patient.getIdentifier();
        Identifier identifier1 = identifier.get(0);
        String patient_first_identifier = identifier1.getValue();
        assertEquals("12345", patient_first_identifier);
    }

    @Test
    public void testLoadingBundleContainedResources() throws JSONException, IOException {
        String bundleJson = loadBundleJson("expected_contained.json");

        IBaseBundle bundle = new ResourceLoader().loadResourceFromString(fhirVersion, bundleJson);
        assertNotNull(bundle);
        ArrayList<Bundle.BundleEntryComponent> entry = (ArrayList<Bundle.BundleEntryComponent>) ((Bundle) bundle).getEntry();
        Bundle.BundleEntryComponent bundleEntryComponent = entry.get(0);
        Patient patient = (Patient) bundleEntryComponent.getResource();
        ArrayList<org.hl7.fhir.r4.model.Identifier> identifier = (ArrayList<org.hl7.fhir.r4.model.Identifier>) patient.getIdentifier();
        Identifier identifier1 = identifier.get(0);
        String patient_first_identifier = identifier1.getValue();
        assertEquals("12345", patient_first_identifier);
    }

    @Test
    public void testPatientContainedUseIssue() throws IOException {
        String patientJson = loadJson("contained.json");

        IBaseBundle bundle = new ResourceLoader().loadResourceFromString(fhirVersion, patientJson);
        assertNotNull(bundle);
        ArrayList<Bundle.BundleEntryComponent> entry = (ArrayList<Bundle.BundleEntryComponent>) ((Bundle) bundle).getEntry();
        Bundle.BundleEntryComponent bundleEntryComponent = entry.get(0);
        Patient patient = (Patient) bundleEntryComponent.getResource();
        ArrayList<org.hl7.fhir.r4.model.Identifier> identifier = (ArrayList<org.hl7.fhir.r4.model.Identifier>) patient.getIdentifier();
        Identifier identifier1 = identifier.get(0);
        String patient_first_identifier = identifier1.getValue();
        assertEquals("1N39JR8VD44", patient_first_identifier);
    }
}

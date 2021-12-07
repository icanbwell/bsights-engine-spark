package com.bwell.common;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ResourceLoaderTest {
    private static final String testResourceRelativePath = "src/test/resources";
    private static String testResourcePath = null;

    @BeforeClass
    public void setup() {
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.println(String.format("Test resource directory: %s", testResourcePath));
    }

    @Test
    public void testLoadingBundle() {
        String folder = "bmi001";
        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected.json");
        String bundleJson = null;

        try {
            bundleJson = FileUtils.readFileToString(f, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(bundleJson);
        JSONObject firstItem = (JSONObject) jsonArray.get(0);
        bundleJson = firstItem.getJSONObject("bundle").toString();

        IBaseBundle bundle = ResourceLoader.loadResourceFromString(bundleJson);
        assertNotNull(bundle);
        String patient_first_identifier = ((Identifier) ((java.util.ArrayList) ((Patient) ((Bundle.BundleEntryComponent) ((java.util.ArrayList) ((Bundle) bundle).getEntry()).get(0)).getResource()).getIdentifier()).get(0)).getValue();
        assertEquals("12345", patient_first_identifier);
    }
}

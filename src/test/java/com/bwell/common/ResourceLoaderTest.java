package com.bwell.common;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

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
        IBaseBundle bundle = ResourceLoader.loadResourceFromFile(testResourcePath + "/bmi001" + "/bundles/expected.json");
        assertNotNull(bundle);
        String patient_first_identifier = ((Identifier) ((java.util.ArrayList) ((Patient) ((Bundle.BundleEntryComponent) ((java.util.ArrayList) ((Bundle) bundle).getEntry()).get(0)).getResource()).getIdentifier()).get(0)).getValue();
        assertEquals("12345", patient_first_identifier);
    }
}

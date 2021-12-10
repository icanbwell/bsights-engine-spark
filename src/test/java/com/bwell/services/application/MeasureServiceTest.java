package com.bwell.services.application;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class MeasureServiceTest {
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private static final String testResourceRelativePath = "src/test/resources";
    private static String testResourcePath = null;
    private static String folder = null;

    @BeforeClass
    public void setup() {
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.printf("Test resource directory: %s%n", testResourcePath);

        folder = "bmi001";
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
    public void testRunCqlLibraryFromFile() throws Exception {
        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = testResourcePath + "/" + folder + "/cql";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = testResourcePath + "/" + folder + "/terminology";
        String cqlVariablesToReturn = "InAgeCohort,InQualifyingObservationCohort,InDemographicComposed";

        String bundleJson = null;
        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected.json");
        try {
            bundleJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(bundleJson);
        JSONObject firstItem = (JSONObject) jsonArray.get(0);
        bundleJson = firstItem.getJSONObject("bundle").toString();

        try {
            Map<String, String> result = new MeasureService().runCqlLibrary(
                    cqllibraryUrl,
                    cqlLibraryName,
                    cqllibraryVersion,
                    terminologyUrl,
                    cqlVariablesToReturn,
                    bundleJson,
                    null,
                    null
            );
            assertEquals(result.get("InAgeCohort"), "true");
            assertEquals(result.get("PatientId"), "1");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();

    }

    @Test
    public void testRunCqlLibrary() throws Exception {
        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = "http://localhost:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://localhost:3000/4_0_0";
        String cqlVariablesToReturn = "InAgeCohort,InQualifyingObservationCohort,InDemographicComposed";

        String bundleJson = null;
        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected.json");
        try {
            bundleJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(bundleJson);
        JSONObject firstItem = (JSONObject) jsonArray.get(0);
        bundleJson = firstItem.getJSONObject("bundle").toString();

        try {
            Map<String, String> result = new MeasureService().runCqlLibrary(
                    cqllibraryUrl,
                    cqlLibraryName,
                    cqllibraryVersion,
                    terminologyUrl,
                    cqlVariablesToReturn,
                    bundleJson,
                    null,
                    null
            );
            assertEquals(result.get("InAgeCohort"), "true");
            assertEquals(result.get("PatientId"), "1");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();

    }

    @Test
    public void testRunCqlLibraryWithContainedResources() throws Exception {
        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = "http://localhost:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://localhost:3000/4_0_0";
        String cqlVariablesToReturn = "InAgeCohort,InQualifyingObservationCohort,InDemographicComposed";

        String bundleJson = null;
        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected_contained.json");
        try {
            bundleJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(bundleJson);
        JSONObject firstItem = (JSONObject) jsonArray.get(0);
//        bundleJson = firstItem.toString();
        bundleJson = firstItem.getJSONObject("bundle").toString();
        try {
            Map<String, String> result = new MeasureService().runCqlLibrary(
                    cqllibraryUrl,
                    cqlLibraryName,
                    cqllibraryVersion,
                    terminologyUrl,
                    cqlVariablesToReturn,
                    bundleJson,
                    null,
                    null
            );
            assertEquals(result.get("InAgeCohort"), "true");
            assertEquals(result.get("PatientId"), "1");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();

    }

}

package com.bwell.runner;

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class MeasureRunnerTest {
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
    public void testRunCqlLibrary() throws Exception {
        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = "http://localhost:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://localhost:3000/4_0_0";
        String cqlVariablesToReturn = "InAgeCohort,InDemographicExists";

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
            Map<String, String> result = new MeasureRunner().runCqlLibrary(
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
        String cqlVariablesToReturn = "InAgeCohort,InDemographicExists";

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
            Map<String, String> result = new MeasureRunner().runCqlLibrary(
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
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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

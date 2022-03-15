package com.bwell.services.application;

import com.bwell.utilities.Utilities;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Map;
import java.util.UUID;

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
        String cqlLibraryName = "AWVCN001";
        String cqllibraryUrl = testResourcePath + "/" + "awvcn001" + "/cql";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = testResourcePath + "/" + "awvcn001" + "/terminology";
        String cqlVariablesToReturn = "InAgeCohort,HadAWV1year,NeedAWV1year";

        String bundleJson = Utilities.getBundle(testResourcePath, "awvcn001");
        String rawJson = Utilities.getRawJson(testResourcePath, "awvcn001", "rawJson.txt");
        String bundleRawJson = bundleSeparateResourcesJson(rawJson);

        try {
            Map<String, String> result = new MeasureService().runCqlLibrary(
                    cqllibraryUrl,
                    null,
                    cqlLibraryName,
                    cqllibraryVersion,
                    terminologyUrl,
                    null,
                    cqlVariablesToReturn,
                    rawJson, //bundleJson, //
                    null,
                    null
            );
            assertEquals(result.get("PatientId"), "unitypoint-eFQWoGGaBo8dUJyl3DuS7lxGLvVFXjDVWEzu2h9X0DY43");
            assertEquals(result.get("InAgeCohort"), "true");
            assertEquals(result.get("HadAWV1year"), "true");
            assertEquals(result.get("NeedAWV1year"), "false");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();
    }

    private String bundleSeparateResourcesJson(String rawSeparatedJson) {
        // the JSON string from the FhirTextReader in the CQL pipeline has the separated resources,
        String separatedResourcesBundleJson = "";
        String[] lines = rawSeparatedJson.split(System.getProperty("line.separator"));
        for(int i=0; i<lines.length; i++) {
            // wrap in a resource
            separatedResourcesBundleJson += ((i!=0 ? "," : "") + "{\"resource\":" + lines[i] + "}");
        }

        UUID uuid = UUID.randomUUID();
        separatedResourcesBundleJson = "{\"resourceType\":\"Bundle\", \"id\":\"" + uuid.toString() + "\", \"entry\":[" + separatedResourcesBundleJson + "]}";
        return separatedResourcesBundleJson;
    }

    @Test
    public void testRunCqlLibraryFromFhirServer() throws Exception {
        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = "http://localhost:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://localhost:3000/4_0_0";
        String cqlVariablesToReturn = "InAgeCohort,InObservationCohort,InDemographic";

        String bundleJson = Utilities.getBundle(testResourcePath, folder);

        try {
            Map<String, String> result = new MeasureService().runCqlLibrary(
                    cqllibraryUrl,
                    null,
                    cqlLibraryName,
                    cqllibraryVersion,
                    terminologyUrl,
                    null,
                    cqlVariablesToReturn,
                    bundleJson,
                    null,
                    null
            );
            assertEquals(result.get("PatientId"), "1");
            assertEquals(result.get("InAgeCohort"), "true");
//            assertEquals(result.get("InObservationCohort"), "true");
//            assertEquals(result.get("InDemographic"), "true");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();
    }

    @Test
    public void testRunCqlLibraryFromFhirServerWithContainedResources() throws Exception {
        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = "http://localhost:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://localhost:3000/4_0_0";
        String cqlVariablesToReturn = "InAgeCohort,InObservationCohort,InDemographic";

        String bundleJson = Utilities.getContainedBundle(testResourcePath, folder);

        try {
            Map<String, String> result = new MeasureService().runCqlLibrary(
                    cqllibraryUrl,
                    null,
                    cqlLibraryName,
                    cqllibraryVersion,
                    terminologyUrl,
                    null,
                    cqlVariablesToReturn,
                    bundleJson,
                    null,
                    null
            );
            assertEquals(result.get("PatientId"), "1");
            assertEquals(result.get("InAgeCohort"), "true");
//            assertEquals(result.get("InObservationCohort"), "true");
//            assertEquals(result.get("InDemographic"), "true");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();
    }

}

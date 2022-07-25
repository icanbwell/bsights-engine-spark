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

        folder = "bmi";
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
        String cqllibraryUrl = testResourcePath + "/" + "awvcn" + "/cql";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = testResourcePath + "/" + "awvcn" + "/terminology";
        String cqlVariablesToReturn = "InAgeCohort,AWCharges,AWDateCharge,AWVDates,AWVReminder,HadAWV1year,NeedAWV1year";

        String bundleJson = Utilities.getBundle(testResourcePath, "awvcn");
        String rawContainedJson = Utilities.getRawJson(testResourcePath, "awvcn", "rawJson.txt");

        try {
            Map<String, String> result = new MeasureService().runCqlLibrary(
                    cqllibraryUrl,
                    null,
                    cqlLibraryName,
                    cqllibraryVersion,
                    terminologyUrl,
                    null,
                    cqlVariablesToReturn,
                    rawContainedJson, // bundleJson,
                    null,
                    null
            );
            assertEquals(result.get("PatientId"), "Patient/unitypoint-eegf5bWyPXkfiquWgAid7W.saxiV7j4TrzYoOWsvANmc3/_history/6");
            assertEquals(result.get("InAgeCohort"), "true");
            assertEquals(result.get("AWCharges"), "true");
            assertEquals(result.get("AWDateCharge"), "2022-01-25");
            assertEquals(result.get("AWVDates"), "2022-01-25");
            assertEquals(result.get("AWVReminder"), "2022-10-25");
            assertEquals(result.get("HadAWV1year"), "true");
            assertEquals(result.get("NeedAWV1year"), "false");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();
    }

    @Test
    public void testRunCqlLibraryFromFhirServer() throws Exception {
        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = "http://fhir:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://fhir:3000/4_0_0";
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
            assertEquals(result.get("PatientId"), "Patient/1");
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
        String cqllibraryUrl = "http://fhir:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://fhir:3000/4_0_0";
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
            assertEquals(result.get("PatientId"), "Patient/1");
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
    public void testRunAWVCqlLibraryFromFhirServerWithContainedResources() throws Exception {
        String cqlLibraryName = "AWVCN001";
        String cqllibraryUrl = "http://fhir:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://fhir:3000/4_0_0";
        String cqlVariablesToReturn = "InAgeCohort,AWVDates,AWVReminder,HadAWV1year,NeedAWV1year";

        String bundleJson = Utilities.getContainedBundle(testResourcePath, "awvcn");

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
            assertEquals(result.get("PatientId"), "Patient/unitypoint-eFQWoGGaBo8dUJyl3DuS7lxGLvVFXjDVWEzu2h9X0DY43/_history/5");
            assertEquals(result.get("HadAWV1year"), "true");
            assertEquals(result.get("NeedAWV1year"), "false");
            assertEquals(result.get("AWVDates"), "2021-09-01");
            assertEquals(result.get("AWVReminder"), "2022-06-01");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();
    }

}

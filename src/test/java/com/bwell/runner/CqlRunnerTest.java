package com.bwell.runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bwell.common.LibraryParameter;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CqlRunnerTest {
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private static final String testResourceRelativePath = "src/test/resources";
    private static String testResourcePath = null;

    @BeforeClass
    public void setup() {
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.println(String.format("Test resource directory: %s", testResourcePath));
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
    public void testR4() {
        String fhirVersion = "R4";
        List<LibraryParameter> libraries = new ArrayList<>();
        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.libraryName = "TestFHIR";
        libraryParameter.libraryUrl = testResourcePath + "/r4";
//        libraryParameter.libraryVersion = libraryParameter.libraryVersion;
        libraryParameter.terminologyUrl = testResourcePath + "/r4/vocabulary/ValueSet";
        libraryParameter.model = new LibraryParameter.ModelParameter();
        libraryParameter.model.modelName = "FHIR";
        libraryParameter.model.modelUrl = testResourcePath + "/r4";
        libraryParameter.context = new LibraryParameter.ContextParameter();
        libraryParameter.context.contextName = "Patient";
        libraryParameter.context.contextValue = "example";

        libraries.add(libraryParameter);

        EvaluationResult result = new MeasureRunner().runCql(fhirVersion, libraries);

        Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();
        for (Map.Entry<String, Object> libraryEntry : entrySet) {
            String key = libraryEntry.getKey();
            Object value = libraryEntry.getValue();
            if (key.equals("Patient")) {
                Patient patient = (Patient) value;
                String identifier_value = patient.getIdentifier().get(0).getValue();
                System.out.println(key + "Id = " + identifier_value);
                assertEquals(identifier_value, "12345");
            }
            System.out.println(key + "=" + tempConvert(value));
        }

        System.out.println();

    }

/*    @Test
    public void testRunCqlLibrary() throws Exception {
        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = "http://localhost:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://localhost:3000/4_0_0";
        String cqlVariablesToReturn = "InAgeCohort,InDemographicExists";

        String folder = "bmi001";
        File f = new File(testResourcePath + "/" + folder + "/bundles" + "/expected.json");
        String bundleJson = null;
        try {
            bundleJson = FileUtils.readFileToString(f, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Map<String, String> result = new MeasureRunner().runCqlLibrary(
                    cqllibraryUrl,
                    cqlLibraryName,
                    cqllibraryVersion,
                    terminologyUrl,
                    cqlVariablesToReturn,
                    bundleJson
            );
            assertEquals(result.get("InAgeCohort"), "true");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();

    }*/

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

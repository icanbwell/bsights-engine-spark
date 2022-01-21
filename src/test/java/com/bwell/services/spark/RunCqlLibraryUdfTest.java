package com.bwell.services.spark;

import com.holdenkarau.spark.testing.SharedJavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

public class RunCqlLibraryUdfTest extends SharedJavaSparkContext {

    private static final String testResourceRelativePath = "src/test/resources";
    private static String testResourcePath = null;


    @BeforeClass
    public void setup() {
        this.runBefore();
        jsc().setLogLevel("ERROR");
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.printf("Test resource directory: %s%n", testResourcePath);
    }

    @Test
    public void testRunCqlLibraryUdfOnBmi() {
        SQLContext sqlContext = new SQLContext(jsc());
        sqlContext.sparkSession().udf().register(
                "runCqlLibrary",
                new RunCqlLibrary(),
                DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType)
        );
        String folder = "bmi001";
        String pathName = testResourcePath + "/" + folder + "/bundles" + "/expected.json";

        Dataset<Row> df = sqlContext.read().option("multiLine", true).json(pathName);

        df.show();
        String patientBundleColumn = "bundle";
        df = df.withColumn(patientBundleColumn, functions.to_json(functions.col(patientBundleColumn)));
        df.show();
        df.select(patientBundleColumn).printSchema();
        df.createOrReplaceTempView("numbersdata");

        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = "http://localhost:3000/4_0_0";
        String cqlLibraryHeaders = "";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://localhost:3000/4_0_0";
        String terminologyHeaders = "";
        String cqlVariablesToReturn = "InAgeCohort,InObservationCohort,InDemographic";

        String command = String.format(
                "runCqlLibrary('%s', '%s', '%s','%s','%s', '%s', '%s', %s, %s, %s)",
                cqllibraryUrl, cqlLibraryHeaders, cqlLibraryName, cqllibraryVersion, terminologyUrl, terminologyHeaders, cqlVariablesToReturn, patientBundleColumn, null, null);

        Dataset<Row> result_df = sqlContext.sql("SELECT " + command + " As ruleResults from numbersdata");
        result_df.printSchema();
        result_df.show(10, false);

        result_df.selectExpr("ruleResults['PatientId'] as PatientId").show();
        List<Row> rows = result_df.selectExpr("ruleResults['PatientId'] as PatientId").collectAsList();
        Assert.assertEquals(rows.get(0).get(0), "1");
        Assert.assertEquals(rows.get(1).get(0), "2");

        result_df.selectExpr("ruleResults['InAgeCohort'] as InAgeCohort").show();
        rows = result_df.selectExpr("ruleResults['InAgeCohort'] as InAgeCohort").collectAsList();
        Assert.assertEquals(rows.get(0).get(0), "true");
        Assert.assertEquals(rows.get(1).get(0), "false");

        result_df.selectExpr("ruleResults['InObservationCohort'] as InObservationCohort").show();
        rows = result_df.selectExpr("ruleResults['InObservationCohort'] as InObservationCohort").collectAsList();
        // Assert.assertEquals(rows.get(0).get(0), "true");
        Assert.assertEquals(rows.get(1).get(0), "false");

        result_df.selectExpr("ruleResults['InDemographic'] as InDemographic").show();
        rows = result_df.selectExpr("ruleResults['InDemographic'] as InDemographic").collectAsList();
        // Assert.assertEquals(rows.get(0).get(0), "true");
        Assert.assertEquals(rows.get(1).get(0), "false");

    }

    @Test
    public void testRunCqlLibraryUdfOnDiab() {
        SQLContext sqlContext = new SQLContext(jsc());
        sqlContext.sparkSession().udf().register(
                "runCqlLibrary",
                new RunCqlLibrary(),
                DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType)
        );
        String folder = "diab001";
        String pathName = testResourcePath + "/" + folder + "/bundles" + "/expected.json";

        Dataset<Row> df = sqlContext.read().option("multiLine", true).json(pathName);

        df.show();
        String patientBundleColumn = "bundle";
        df = df.withColumn(patientBundleColumn, functions.to_json(functions.col(patientBundleColumn)));
        df.show();
        df.select(patientBundleColumn).printSchema();
        df.createOrReplaceTempView("numbersdata");

        String cqlLibraryName = "DIAB001";
        String cqllibraryUrl = "http://localhost:3000/4_0_0";
        String cqlLibraryHeaders = "";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://localhost:3000/4_0_0";
        String terminologyHeaders = "";
        String cqlVariablesToReturn = "InObservationCohort,InDemographic";

        String command = String.format(
                "runCqlLibrary('%s', '%s', '%s','%s','%s', '%s', '%s', %s, %s, %s)",
                cqllibraryUrl, cqlLibraryHeaders, cqlLibraryName, cqllibraryVersion, terminologyUrl, terminologyHeaders, cqlVariablesToReturn, patientBundleColumn, null, null);

        Dataset<Row> result_df = sqlContext.sql("SELECT " + command + " As ruleResults from numbersdata");
        result_df.printSchema();
        result_df.show(10, false);

        result_df.selectExpr("ruleResults['PatientId'] as PatientId").show();
        List<Row> rows = result_df.selectExpr("ruleResults['PatientId'] as PatientId").collectAsList();
        Assert.assertEquals(rows.get(0).get(0), "1");
        Assert.assertEquals(rows.get(1).get(0), "2");

        result_df.selectExpr("ruleResults['InObservationCohort'] as InObservationCohort").show();
        rows = result_df.selectExpr("ruleResults['InObservationCohort'] as InObservationCohort").collectAsList();
        Assert.assertEquals(rows.get(0).get(0), "true");
        Assert.assertEquals(rows.get(1).get(0), "false");

        result_df.selectExpr("ruleResults['InDemographic'] as InDemographic").show();
        rows = result_df.selectExpr("ruleResults['InDemographic'] as InDemographic").collectAsList();
        Assert.assertEquals(rows.get(0).get(0), "true");
        Assert.assertEquals(rows.get(1).get(0), "false");
    }
}

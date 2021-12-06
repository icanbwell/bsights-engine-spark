package com.bwell.spark;

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
    String folder = "bmi001";

    @BeforeClass
    public void setup() {
        this.runBefore();
        jsc().setLogLevel("ERROR");
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.println(String.format("Test resource directory: %s", testResourcePath));
    }

    @Test
    public void testRunCqlLibraryUdf() {
        SQLContext sqlContext = new SQLContext(jsc());
        sqlContext.sparkSession().udf().register(
                "runCqlLibrary",
                new RunCqlLibrary(),
                DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType)
        );

        String pathName = testResourcePath + "/" + folder + "/bundles" + "/expected.json";

        Dataset<Row> df = sqlContext.read().option("multiLine", true).json(pathName);
//        Dataset<Row> df = sqlContext.read().text(pathName);
        df.show();
        df = df.withColumn("patientBundle1", functions.struct(functions.col("resourceType"),functions.col("entry")));
        df = df.withColumn("patientBundle", functions.to_json(functions.col("patientBundle1")));
        df.show();
        df.select("patientBundle1").printSchema();
        df.select("patientBundle").printSchema();
        df.createOrReplaceTempView("numbersdata");

        String cqlLibraryName = "BMI001";
        String cqllibraryUrl = "http://localhost:3000/4_0_0";
        String cqllibraryVersion = "1.0.0";
        String terminologyUrl = "http://localhost:3000/4_0_0";
        String cqlVariablesToReturn = "InAgeCohort,InDemographicExists";

        String command = String.format(
                "runCqlLibrary('%s', '%s','%s','%s','%s', patientBundle)",
                cqllibraryUrl, cqlLibraryName, cqllibraryVersion, terminologyUrl, cqlVariablesToReturn);

        Dataset<Row> result_df = sqlContext.sql("SELECT " + command + " As ruleResults from numbersdata");
        result_df.printSchema();
        result_df.show(10, false);

        result_df.selectExpr("ruleResults['InAgeCohort'] as InAgeCohort").show();
        List<Row> rows = result_df.selectExpr("ruleResults['InAgeCohort'] as InAgeCohort").collectAsList();
        Assert.assertEquals(rows.get(0).get(0), "true");
//        result_df.selectExpr("ruleResults['key2'] as key2").show();
    }
}

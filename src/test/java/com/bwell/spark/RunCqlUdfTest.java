package com.bwell.spark;

import com.holdenkarau.spark.testing.SharedJavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.apache.spark.sql.functions;

import java.io.File;

public class RunCqlUdfTest extends SharedJavaSparkContext {

    private static final String testResourceRelativePath = "src/test/resources";
    private static String testResourcePath = null;
    String folder = "bmi001";

    @BeforeClass
    public void setup() {
        this.runBefore();
        jsc().setLogLevel("WARN");
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.println(String.format("Test resource directory: %s", testResourcePath));
    }

    @Test
    public void testRunCqlUdf() {
        SQLContext sqlContext = new SQLContext(jsc());
        sqlContext.sparkSession().udf().register("runCql", new RunCql(), DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType));

        String pathName = testResourcePath + "/" + folder + "/bundles" + "/expected.json";

        Dataset<Row> df = sqlContext.read().option("multiLine", true).json(pathName);
//        Dataset<Row> df = sqlContext.read().text(pathName);
        df.show();
        df = df.withColumn("patientBundle1", org.apache.spark.sql.functions.struct(functions.col("resourceType"),functions.col("entry")));
        df = df.withColumn("patientBundle", org.apache.spark.sql.functions.to_json(functions.col("patientBundle1")));
        df.show();
        df.select("patientBundle1").printSchema();
        df.select("patientBundle").printSchema();
        df.createOrReplaceTempView("numbersdata");
        Dataset<Row> result_df = sqlContext.sql("SELECT runCql('cqlLibraryUrl', 'terminologyUrl', patientBundle) As ruleResults from numbersdata");
        result_df.printSchema();
        result_df.show(10, false);

        result_df.selectExpr("ruleResults['key1'] as key1").show();
        result_df.selectExpr("ruleResults['key2'] as key2").show();
    }
}

package com.bwell;

import com.holdenkarau.spark.testing.SharedJavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.apache.spark.sql.functions;

import java.io.File;

public class RunCqlTest extends SharedJavaSparkContext {

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
        String patientBundleColumn = "bundle";
        df = df.withColumn(patientBundleColumn, functions.to_json(functions.col(patientBundleColumn)));
        df.show();
        df.select(patientBundleColumn).printSchema();

        df.createOrReplaceTempView("numbersdata");
        Dataset<Row> result_df = sqlContext.sql("SELECT runCql('cqlLibraryUrl', 'terminologyUrl', bundle) As ruleResults from numbersdata");
        result_df.printSchema();
        result_df.show(10, false);

        result_df.selectExpr("ruleResults['key1'] as key1").show();
        result_df.selectExpr("ruleResults['key2'] as key2").show();
    }
}

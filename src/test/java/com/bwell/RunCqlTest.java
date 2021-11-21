package com.bwell;

import com.holdenkarau.spark.testing.SharedJavaSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.MapType;
import org.apache.spark.sql.types.StringType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RunCqlTest extends SharedJavaSparkContext {

    @BeforeClass
    public void setup() {
        this.runBefore();
    }

    @Test
    public void testRunCqlUdf() {
        List<String> data = new ArrayList<String>();
        data.add("green");
        data.add("red");
        JavaRDD<String> jsonRDD = jsc().parallelize(data);
        SQLContext sqlContext = new SQLContext(jsc());
        sqlContext.sparkSession().udf().register("runCql", new RunCql(), DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType));

        Dataset<Row> df = sqlContext.read().json(jsonRDD);
        df.createOrReplaceTempView("numbersdata");
        Dataset<Row> result_df = sqlContext.sql("SELECT runCql('cqlLibraryUrl', 'terminologyUrl', 'patient') As num1 from numbersdata");
        result_df.printSchema();
        result_df.show(10, false);
    }
}

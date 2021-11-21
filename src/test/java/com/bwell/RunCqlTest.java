package com.bwell;

import com.holdenkarau.spark.testing.SharedJavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RunCqlTest extends SharedJavaSparkContext {

    @BeforeClass
    public void setup() {
        this.runBefore();
        jsc().setLogLevel("WARN");
    }

    @Test
    public void testRunCqlUdf() {
        SQLContext sqlContext = new SQLContext(jsc());
        sqlContext.sparkSession().udf().register("runCql", new RunCql(), DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType));

        List<Row> rows = Arrays.asList(RowFactory.create("green"), RowFactory.create("red"));
        StructType schema = DataTypes.createStructType(
                new StructField[]{DataTypes.createStructField("color", DataTypes.StringType, false)});

        Dataset<Row> df = sqlContext.createDataFrame(rows, schema);
        df.show();
        df.createOrReplaceTempView("numbersdata");
        Dataset<Row> result_df = sqlContext.sql("SELECT runCql('cqlLibraryUrl', 'terminologyUrl', 'patient') As num1 from numbersdata");
        result_df.printSchema();
        result_df.show(10, false);
    }
}

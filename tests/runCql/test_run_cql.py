from pyspark.sql.session import SparkSession
from pyspark.sql.types import StringType


def test_run_cql(spark_session: SparkSession) -> None:
    spark_session.udf.registerJavaFunction("runCql", "com.bwell.RunCql", StringType())
    import json
    j = {'num1': 2, 'num2': 3}
    a = [json.dumps(j)]
    jsonRDD = spark_session.sparkContext.parallelize(a)
    df = spark_session.read.json(jsonRDD)
    df.createOrReplaceTempView("numbersdata")
    df1 = spark_session.sql("SELECT runCql('cqlLibraryUrl', 'terminologyUrl', 'patient') As num1 from numbersdata")
    df1.show(10)

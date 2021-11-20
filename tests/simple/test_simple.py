import pyspark
from pyspark.sql import SparkSession
from pyspark.sql.types import LongType
from pyspark import SparkContext


def test_simple(spark_session: SparkSession) -> None:
    spark_session.udf.registerJavaFunction("numAdd", "com.bwell.AddNumber", LongType())
    spark_session.udf.registerJavaFunction("numMultiply", "com.bwell.MultiplyNumber", LongType())
    import json
    j = {'num1': 2, 'num2': 3}
    a = [json.dumps(j)]
    jsonRDD = spark_session.sparkContext.parallelize(a)
    df = spark.read.json(jsonRDD)
    df.registerTempTable("numbersdata")
    df1 = spark.sql("SELECT numMultiply(num1) As num1, numAdd(num2) AS num2 from numbersdata")
    df1.show(10)

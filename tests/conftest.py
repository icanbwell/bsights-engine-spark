import logging
import os
import shutil
import random
import string
from pathlib import Path
from typing import Any, Generator

import boto3
import pytest
from _pytest.fixtures import FixtureFunctionMarker
from botocore.client import BaseClient
from moto import mock_ssm, mock_s3  # type: ignore
from pyspark.sql import SparkSession

# make sure env variables are set correctly
if "SPARK_HOME" not in os.environ:
    os.environ["SPARK_HOME"] = "/usr/local/opt/spark"


def quiet_py4j() -> None:
    """turn down spark logging for the test context"""
    logger = logging.getLogger("py4j")
    logger.setLevel(logging.ERROR)


def clean_spark_dir() -> None:
    """

    :return:
    """
    try:
        os.remove("./derby.log")
        shutil.rmtree("./metastore_db")
        shutil.rmtree("./spark-warehouse")
    except OSError:
        pass


def clean_spark_session(session: SparkSession) -> None:
    """

    :param session:
    :return:
    """
    tables = session.catalog.listTables("default")

    for table in tables:
        # print(f"clear_tables() is dropping table/view: {table.name}")
        # noinspection SqlDialectInspection,SqlNoDataSourceInspection
        session.sql(f"DROP TABLE IF EXISTS default.{table.name}")
        # noinspection SqlDialectInspection,SqlNoDataSourceInspection
        session.sql(f"DROP VIEW IF EXISTS default.{table.name}")
        # noinspection SqlDialectInspection,SqlNoDataSourceInspection
        session.sql(f"DROP VIEW IF EXISTS {table.name}")

    session.catalog.clearCache()


def clean_close(session: SparkSession) -> None:
    """

    :param session:
    :return:
    """
    clean_spark_session(session)
    clean_spark_dir()
    session.stop()


def get_random_string(length: int) -> str:
    letters = string.ascii_lowercase
    result_str = "".join(random.choice(letters) for _ in range(length))
    return result_str


@pytest.fixture(scope="session")
def spark_session(request: Any) -> SparkSession:
    # make sure env variables are set correctly
    if "SPARK_HOME" not in os.environ:
        os.environ["SPARK_HOME"] = "/usr/local/opt/spark"

    lib_dir: Path = Path("/opt/bitnami/spark/jars/")
    udf_jar: Path = lib_dir / 'helix.cql_spark-1.0-SNAPSHOT.jar'

    clean_spark_dir()

    if os.environ.get("SPARK_IN_DOCKER"):
        master: str = "spark://spark:7077"
        print(f"++++++ Running on docker spark: {master} ++++")
    else:
        master = "local[2]"
        print(f"++++++ Running on local spark: {master} ++++")

    # use a random string to get a different app name every time and not reuse the same app name across tests
    session = (
        SparkSession.builder.appName(
            f"pytest-pyspark-local-testing-{get_random_string(4)}"
        )
        .master(master)
        .config("spark.ui.showConsoleProgress", "false")
        .config("spark.driver.memory", "2g")
        .config("spark.executor.memory", "2g")
        # .config("spark.executor.memoryOverhead", "4096")
        # .config('spark.dynamicAllocation.enabled', True)
        # .config('spark.shuffle.service.enabled', True)
        # .config("spark.sql.shuffle.partitions", "2")
        # .config("spark.default.parallelism", "4")
        .config("spark.sql.broadcastTimeout", "2400")
        .config("spark.executorEnv.AWS_ACCESS_KEY_ID", "testing")
        .config("spark.executorEnv.AWS_SECRET_ACCESS_KEY", "testing")
        .config("spark.executorEnv.AWS_SECURITY_TOKEN", "testing")
        .config("spark.executorEnv.AWS_SESSION_TOKEN", "testing")
        .config("spark.jars", udf_jar.as_uri())
        .enableHiveSupport()
        .getOrCreate()
    )

    request.addfinalizer(lambda: clean_close(session))
    quiet_py4j()
    return session


@pytest.fixture(scope="function")
def aws_credentials() -> None:
    """Mocked AWS Credentials for moto."""
    os.environ["AWS_ACCESS_KEY_ID"] = "testing"
    os.environ["AWS_SECRET_ACCESS_KEY"] = "testing"
    os.environ["AWS_SECURITY_TOKEN"] = "testing"
    os.environ["AWS_SESSION_TOKEN"] = "testing"
    os.environ["AWS_REGION"] = "us-east-1"


@pytest.fixture(scope="function")
def ssm_mock(
    aws_credentials: FixtureFunctionMarker,
) -> Generator[BaseClient, None, None]:
    with mock_ssm():
        yield boto3.client("ssm", region_name="us-east-1")


@pytest.fixture(scope="function")
def s3_mock(
    aws_credentials: FixtureFunctionMarker,
) -> Generator[BaseClient, None, None]:
    with mock_s3():
        yield boto3.client("s3", region_name="us-east-1")

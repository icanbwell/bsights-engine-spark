FROM imranq2/spark_python:0.1.28
# https://github.com/imranq2/docker.spark_python
USER root

ENV PYTHONPATH=/helix.pipelines
ENV PYTHONPATH "/opt/project:${PYTHONPATH}"
ENV CLASSPATH=/helix.pipelines/jars:/opt/bitnami/spark/jars/:$CLASSPATH

# first get just the pom.xml and download dependencies (so we don't do this again when the code changes)
COPY ./pom.xml /helix.pipelines/
WORKDIR /helix.pipelines

RUN mvn --batch-mode --update-snapshots verify clean

# now get the rest of the code and create the package
COPY ./src/ /helix.pipelines/src/

# skip running tests since it requires a fhir server
RUN mvn -Dmaven.test.skip package && \
    cp ./target/cql_spark_engine-1.0-SNAPSHOT.jar /opt/bitnami/spark/jars/

# USER 1001

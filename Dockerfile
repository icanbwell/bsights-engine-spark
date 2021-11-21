FROM imranq2/spark_python:0.1.28
# https://github.com/imranq2/docker.spark_python
USER root

ENV PYTHONPATH=/helix.pipelines
ENV PYTHONPATH "/opt/project:${PYTHONPATH}"
ENV CLASSPATH=/helix.pipelines/jars:/opt/bitnami/spark/jars/:$CLASSPATH

COPY Pipfile* /helix.pipelines/
WORKDIR /helix.pipelines

COPY ./target/helix.bSights.cql_spark_engine-1.0-SNAPSHOT.jar /opt/bitnami/spark/jars/

#COPY . /helix.pipelines

RUN mkdir -p /.local/share/virtualenvs && chmod 777 /.local/share/virtualenvs
# USER 1001

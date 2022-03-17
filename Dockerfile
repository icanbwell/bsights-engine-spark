FROM imranq2/spark-py:3.0.27
# https://github.com/imranq2/docker.spark_python
USER root

ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/root"
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN apt-get -y update && apt-get -y install curl && apt-get clean

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
 && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
 && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
 && rm -f /tmp/apache-maven.tar.gz \
 && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

#RUN apt-get -y update && apt-get -y install curl autoconf build-essential && apt-get clean

ENV PYTHONPATH=/helix.pipelines
ENV PYTHONPATH "/opt/project:${PYTHONPATH}"
ENV CLASSPATH=/helix.pipelines/jars:/opt/spark/jars/:$CLASSPATH

# first get just the pom.xml and download dependencies (so we don't do this again when the code changes)
COPY ./pom.xml /helix.pipelines/
WORKDIR /helix.pipelines

RUN mvn --batch-mode --update-snapshots verify clean

# now get the rest of the code and create the package
COPY ./src/ /helix.pipelines/src/

## skip running tests since it requires a fhir server
RUN mvn -Dmaven.test.skip package && \
    cp ./target/bsights-engine-spark-1.0.2.jar /opt/spark/jars/

# USER 1001

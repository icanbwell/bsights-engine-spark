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

ENV PYTHONPATH=/bsights-engine-spark
ENV PYTHONPATH "/opt/project:${PYTHONPATH}"
ENV CLASSPATH=/bsights-engine-spark/jars:/opt/spark/jars/:$CLASSPATH

# first get just the pom.xml and download dependencies (so we don't do this again when the code changes)
COPY ./pom.xml /bsights-engine-spark/
COPY ./settings.xml /bsights-engine-spark/
WORKDIR /bsights-engine-spark

RUN mvn --batch-mode verify -s ./settings.xml
#RUN mvn --batch-mode --update-snapshots verify clean

# now get the rest of the code and create the package
COPY ./src/ /bsights-engine-spark/src/

## skip running tests since it requires a fhir server
RUN mvn -Dmaven.test.skip package -s ./settings.xml && \
    cp ./target/bsights-engine-spark-1.0.8.jar /opt/spark/jars/

# USER 1001

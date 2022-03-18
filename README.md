# bsights-engine-spark

### Project Information
This project creates an Apache Spark UDF (User Defined Function) wrapper for the CQL (Clinical Quality Language) Engine (https://github.com/DBCG/cql-evaluator) so we can use it in Spark.

The Spark UDF is called RunCqlLibrary: [RunCqlLibrary](src/main/java/com.bwell/services/spark/RunCqlLibrary.java)


### CQL
Clinical Quality Language is a standard for defining rules on FHIR data.

Example CQL:
```cql
library BMI001 version '1.0.0'

using FHIR version '4.0.1'

include FHIRHelpers version '4.0.1'

parameter "MeasurementPeriod" Interval<DateTime> default Interval[@2021-01-01T00:00:00, @2021-12-31T00:00:00]

valueset "BMI Over 35": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1106.45'

context Patient

/* Test inline definition */
/*
    define InDemographicInline:
        [Patient] patient
        where AgeInYearsAt(start of MeasurementPeriod) >= 16
          and exists([Observation: "BMI Over 35"] observation
                        where observation.status in {'final', 'amended'}
                    )
*/

/* Test composed definition */
define InAgeCohort:
        AgeInYearsAt(start of MeasurementPeriod) >= 16

define InQualifyingObservationCohort:
        exists([Observation: "BMI Over 35"] observation
                    where observation.status in {'final', 'amended'}
              )

define InDemographicComposed:
         InAgeCohort
            and InQualifyingObservationCohort
```

[CQL Reference](https://cql.hl7.org/02-authorsguide.html)

[CQL Basics Video](https://youtu.be/XhOxCBhyK0Y)

[CQL 101 Video](https://youtu.be/BETFiQzLb8o)

[CQL Wiki](https://github.com/cqframework/CQL-Formatting-and-Usage-Wiki/wiki/Authoring-Measures-in-CQL)


### Contributing
This project requires Docker only except for debugging.

For debugging in IntelliJ IDEA:

On Mac (https://mkyong.com/java/how-to-install-java-on-mac-osx/):

One time setup of SDKMan: https://sdkman.io/install

1. ```sdk list java``` (to see java version available)
2. JDK 15 is now too old to be included in sdkman, so you have to download manually:

```shell
wget https://download.java.net/java/GA/jdk15.0.2/0d1cfde4252546c6931946de8db48ee2/7/GPL/openjdk-15.0.2_osx-x64_bin.tar.gz -P $TMPDIR
tar -xvf $TMPDIR/openjdk-15.0.2_osx-x64_bin.tar.gz -C ~/.sdkman/candidates/java/
mv ~/.sdkman/candidates/java/jdk-15.0.2.jdk ~/.sdkman/candidates/java/15.0.2-open
sdk default java 15.0.2-open
```

To run tests: `make tests`

To compile the jar: `make buildjar`

To deploy to local dev docker: `make loadfhir`
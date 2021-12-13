# bsights-measure-engine-spark

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
This project requires Docker, Java 1.8 and Maven.

To run tests: `make tests`

To compile the jar: `make buildjar`

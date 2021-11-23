library TestFHIR

/*
Every expression in this library should return non-null or a list with at least one element,
with the exception of TestPatientDeceasedDateTime, which should return null
 */

using FHIR version '4.0.1'

include FHIRHelpers version '4.0.1'

parameter "MeasurementPeriod" Interval<DateTime> default Interval[@2021-01-01T00:00:00, @2021-12-31T00:00:00]

/* Imran: CQL Engine does not seem to like value sets that refer to other value sets so using just the ICD10 value set here */
valueset "BMI Over 35 ICD 10": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1106.41'

context Patient

define InDemographic1:
        [Patient] patient
        where AgeInYearsAt(start of MeasurementPeriod) >= 16

define InDemographic:
        [Patient] patient
        where AgeInYearsAt(start of MeasurementPeriod) >= 16
          and exists([Observation: "BMI Over 35 ICD 10"] observation
                        where observation.status in {'final', 'amended'}
                    )

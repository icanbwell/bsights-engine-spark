library BMI001 version '1.0.0'

using FHIR version '4.0.1'

include FHIRHelpers version '4.0.1'

parameter "MeasurementPeriod" Interval<DateTime> default Interval[@2021-01-01T00:00:00, @2021-12-31T00:00:00]

valueset "BMI Over 35": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1106.45'

context Patient

define InAgeCohort:
        AgeInYearsAt(start of MeasurementPeriod) >= 16

define InObservationCohort:
        exists([Observation: "BMI Over 35"] observation
                    where observation.status in {'final', 'amended'}
              )

define InDemographic:
         InAgeCohort
            and InObservationCohort
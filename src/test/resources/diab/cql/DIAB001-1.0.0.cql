library DIAB001 version '1.0.0'

using FHIR version '4.0.1'

include "FHIRHelpers" version '4.0.1'

parameter "MeasurementPeriod" Interval<DateTime> default Interval[@2021-01-01T00:00:00, @2021-12-31T00:00:00]

codesystem "SNOMED": 'http://snomed.info/sct'
code "451061000124104": '451061000124104' from "SNOMED" display 'Hemoglobin A1c greater than 9% indicating poor diabetic control (finding)'

context Patient

define "InObservationCohort":
    exists([Observation: "451061000124104"] observation
            where (ToDate((observation.effective as FHIR.dateTime).value) in MeasurementPeriod)
                  and
                  observation.status in {'final', 'amended'}
          )

define InDemographic:
  InObservationCohort
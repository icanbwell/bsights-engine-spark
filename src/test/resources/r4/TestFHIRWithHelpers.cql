library TestFHIRWithHelpers

/*
Every expression in this library should return non-null or a list with at least one element,
with the exception of TestPatientDeceasedDateTime, which should return null
 */

using FHIR version '4.0.1'

include FHIRHelpers version '4.0.1'

codesystem "LOINC": 'http://loinc.org'
codesystem "Marital Status Codes": 'http://terminology.hl7.org/CodeSystem/v3-MaritalStatus'
codesystem "CDC Race and Ethnicity Codes": 'urn:oid:2.16.840.1.113883.6.238'

valueset "Marital Status": 'http://hl7.org/fhir/ValueSet/marital-status'

code "Marital Status - Married": 'M' from "Marital Status Codes"
code "American Indian or Alaska Native": '1002-5' from "CDC Race and Ethnicity Codes"
code "Alaska Native": '1735-0' from "CDC Race and Ethnicity Codes"
code "Blood pressure": '85354-9' from "LOINC" display 'blood pressure'
code "Diastolic blood pressure": '8462-4' from "LOINC" display 'Diastolic blood pressure'
code "Systolic blood pressure": '8480-6' from "LOINC" display 'Systolic blood pressure'

context Patient

define TestAdverseEvent: ["AdverseEvent"]
define TestAllergyIntolerance: ["AllergyIntolerance"]
define TestBodyStructure: ["BodyStructure"]
define TestCarePlan: ["CarePlan"]
define TestCareTeam: ["CareTeam"]
define TestChargeItem: ["ChargeItem"]
define TestClaim: ["Claim"]
define TestCommunication: ["Communication"]
define TestCommunicationRequest: ["CommunicationRequest"]
define TestCondition: ["Condition"]
define TestCoverage: ["Coverage"]
define TestDevice: ["Device"]
define TestDeviceRequest: ["DeviceRequest"]
define TestDeviceUseStatement: ["DeviceUseStatement"]
define TestDiagnosticReport: ["DiagnosticReport"]
define TestDocumentReference: ["DocumentReference"]
define TestEncounter: ["Encounter"]
define TestFamilyMemberHistory: ["FamilyMemberHistory"]
define TestFlag: ["Flag"]
define TestGoal: ["Goal"]
define TestImagingStudy: ["ImagingStudy"]
define TestImmunization: ["Immunization"]
define TestImmunizationEvaluation: ["ImmunizationEvaluation"]
define TestImmunizationRecommendation: ["ImmunizationRecommendation"]
define TestLocation: ["Location"]
define TestMedication: ["Medication"]
define TestMedicationAdministration: ["MedicationAdministration"]
define TestMedicationDispense: ["MedicationDispense"]
define TestMedicationRequest: ["MedicationRequest"]
define TestMedicationStatement: ["MedicationStatement"]
define TestObservation: ["Observation"]
define TestOrganization: ["Organization"]
define TestPatient: ["Patient"]
define TestPractitioner: ["Practitioner"]
define TestPractitionerRole: ["PractitionerRole"]
define TestProcedure: ["Procedure"]
define TestRelatedPerson: ["RelatedPerson"]
define TestServiceRequest: ["ServiceRequest"]
define TestSpecimen: ["Specimen"]
define TestSubstsance: ["Substance"]
define TestTask: ["Task"]

define TestPatientGender:
  Patient P
    where P.gender = 'male'

define TestPatientActive:
  Patient P
    where P.active is true

define TestPatientBirthDate:
  Patient P
    where P.birthDate before Today()

define TestPatientMaritalStatusMembership:
  Patient P
    where FHIRHelpers.ToConcept(P.maritalStatus) in "Marital Status"

define TestPatientMartialStatusComparison:
  Patient P
    where FHIRHelpers.ToConcept(P.maritalStatus) ~ "Marital Status - Married"

define TestPatientDeceasedAsBoolean:
  Patient P
    where P.deceased is false

define TestPatientDeceasedAsDateTime:
  Patient P
    where P.deceased before Today()

define TestSlices:
  [Observation: "Blood pressure"] BP
    let
      SystolicBP: singleton from (BP.component C where C.code ~ "Systolic blood pressure"),
      DiastolicBP: singleton from (BP.component C where C.code ~ "Diastolic blood pressure")
    where SystolicBP.value < 140 'mm[Hg]'
      and DiastolicBP.value < 90 'mm[Hg]'

define TestSimpleExtensions:
  Patient P
    let birthsex: singleton from (P.extension E where E.url = 'http://hl7.org/fhir/us/core/StructureDefinition/us-core-birthsex')
    where birthsex.value = 'M'

define TestComplexExtensions:
  Patient P
    let
      race: singleton from (P.extension E
        where E.url = 'http://hl7.org/fhir/us/core/StructureDefinition/us-core-race'),
      ombCategory: race.extension E where E.url = 'ombCategory',
      detailed: race.extension E where E.url = 'detailed'
    where exists (ombCategory O where O.value ~ "American Indian or Alaska Native")
      and exists (detailed O where O.value ~ "Alaska Native")


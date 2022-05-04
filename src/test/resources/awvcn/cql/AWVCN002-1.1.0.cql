/*
Name: Annual Wellness Visit Care Need
@Description: Checking if a patient had a Annual Wellness Visit in the Current Year as well as prior year
@Author Eugene Vestel
@Comment: Uses CMS AWV measure value sets and other definitions
provided by B WEll Clinical Team / Heather Crosby
Date: 02-11-22
change log
04.08.2022
-added measure report structure elements
*/

library AWVCN002 version '1.1.0'

using FHIR version '4.0.1'

include FHIRHelpers version '4.0.1'

/*
NOTE: Not used at this time
parameter "MeasurementPeriod" Interval<DateTime> default Interval[@2021-01-01T00:00:00, @2022-12-31T00:00:00]
*/

/*
VSAC OIDs: B Well custom maintained
*/

valueset "Annual Wellness Visit" : 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1235.31'
valueset "Hospice Exclusions" : 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1235.29'

context Patient

/*note*
If we need only Unity Point data? potentially for future?
define potentialy meta.security.coding.code = 'unitypoint'
*/
define currentperiod: Now()

define FirstOfCurrentYear: (@2022-01-01)

/*Start Period Year year back from current date */
define LookbackIntervalOne: Interval[@2021-01-01, Now()]

/* 2 years back from current date */
define LookbackIntervalTwo: Interval[Now()- 2 year, Now()]

define "InAgeCohort":
    AgeInYearsAt(currentperiod) >= 65

// Hospice Exclusions

//define measure: tbd

define HasHospice:
   exists ([ExplanationOfBenefit: "Hospice Exclusions"])
      or exists ([Procedure: "Hospice Exclusions"])
      or exists ([Observation:"Hospice Exclusions"])
      or exists ([Encounter: "Hospice Exclusions"])
      or exists ([ChargeItem: "Hospice Exclusions"])

define InitialPopulation: InAgeCohort and not HasHospice

//EOB resource

define AWEOB:
   exists ([ExplanationOfBenefit: "Annual Wellness Visit"])
      ValidEOB where (ToDate(ValidEOB.created as FHIR.dateTime)) during "LookbackIntervalOne"
  		    and not exists ([ExplanationOfBenefit:"Hospice Exclusions"])

//Charges
define AWCharges:
   exists ([ChargeItem: "Annual Wellness Visit"]) ValidCharge
or
   exists ([Encounter: "Annual Wellness Visit"])
      ValidEncounter where ValidEncounter.period during "LookbackIntervalOne"
  	   	 and not exists ([ChargeItem:"Hospice Exclusions"])


//Observations
define AWObservations:
   exists ([Observation: "Annual Wellness Visit"]) ValidObservation
    where (ToDate(ValidObservation.effective as FHIR.dateTime)) ///starts
      during "LookbackIntervalOne" and not exists ([Observation:"Hospice Exclusions"])

//Encounters
define AWEncounters:
   exists ([Encounter: "Annual Wellness Visit"])
      ValidEnc where ValidEnc.period during "LookbackIntervalOne"
  	   	 and not exists ([ChargeItem:"Hospice Exclusions"])

//Dates

define LastAWObservations:
   Last([Observation: "Annual Wellness Visit"] ValidObservation
       sort by effective desc)

define LastAWCharges:
  case when
  AWCharges is true then  Last([Encounter] ValidCharge
      sort by period.start desc) else null end

define LastAWEncounter:
   Last([Encounter: "Annual Wellness Visit"] ValidEnc
   sort by period.end desc)

define LastAWEOB:
   Last([ExplanationOfBenefit: "Annual Wellness Visit"] ValidEnc
       sort by created desc)

define AWDateObs:
   (ToDate(LastAWObservations.effective  as FHIR.dateTime))

define AWDateCharge:
   (ToDate(LastAWCharges.period.start  as FHIR.dateTime))

define AWDateEnc:
   (ToDate(LastAWEncounter.period.end  as FHIR.dateTime))

define AWDateEOB:
   (ToDate(LastAWEOB.created  as FHIR.dateTime))

define AWVDates:
   Coalesce(
      AWDateCharge
      ,AWDateObs,AWDateEnc,AWDateEOB)

define AWVReminder:
   case
      when AWVDates is not null then AWVDates + 9 months
      else FirstOfCurrentYear
   end

//Has had Annual Wellness within last 12 months

define "HadAWV1year":
         "AWCharges"or
         "AWObservations" or
         "AWEncounters" or
         "AWEOB"

///Needs Annual Wellness Visit
define   "NeedAWV1year": not (
          "AWCharges"  or
          "AWObservations" or
          "AWEncounters" or
          "AWEOB"
          )

define "EligiblePopulation":
   InitialPopulation

define "Denominator":
   case when Coalesce (HadAWV1year,NeedAWV1year)
         then 1 else 0 end

define "Numerator":
   case
      when HadAWV1year then 1 else 0
      end

define "RequiredExclusions":
   HasHospice

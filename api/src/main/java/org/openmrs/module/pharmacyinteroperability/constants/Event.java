/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.pharmacyinteroperability.constants;

/**
 *
 * @author Stanslaus Odhiambo
 */
public enum Event {

    HIV_DIAGNOSIS("HIV_DIAGNOSIS"),
    HIV_CARE_INITIATION("HIV_CARE_INITIATION"),
    ART_START("ART_START"),
    PEP_DATE("PEP_DATE"),
    TRANSFER_IN("TRANSFER_IN"),
    CD4_COUNT("CD4_COUNT"),
    CD4_PERCENT("CD4_PERCENT"),
    VIRAL_LOAD("VIRAL_LOAD"),
    WHO_STAGE("WHO_STAGE"),
    TB_DIAGNOSIS("TB_DIAGNOSIS"),
    TB_TREATMENT("TB_TREATMENT"),
    LOST_TO_FOLLOWUP("LOST_TO_FOLLOWUP"),
    DECEASED("DECEASED"),
    CHANGE_IN_REGIMEN("CHANGE_IN_REGIMEN"),
    FIRST_LINE_REGIMEN("FIRST_LINE_REGIMEN"),
    SECOND_LINE_REGIMEN("SECOND_LINE_REGIMEN"),
    PMTCT_INITIATION("PMTCT_INITIATION"),
    BIRTH("BIRTH"),
    TRANSFER_OUT("TRANSFER_OUT"),
    ART_ELIGIBILITY("ART_ELIGIBILITY"),
    ENROLLMENT_DATE("ENROLLMENT_DATE"),
    PATIENT_ADDRESS("ADDRESS"),
    PHONE_NUMBER("PHONE_NUMBER"),
    TYPE_OF_SERVICE("TYPE_OF_SERVICE"),
    CURRENT_STATUS("CURRENT_STATUS"),
    TB("TB "),
    CURRENT_REGIMEN("CURRENT_REGIMEN"),
    OTHER_DISEASE_CONDITIONS("OTHER_DISEASE_CONDITIONS"),
    ADR_SIDE_EFFECTS("ADR_SIDE_EFFECTS"),
    OTHER_DRUGS("OTHER_DRUGS "),
    PREGNANT("pregnant "),
    HEIGHT("height"),
    WEIGHT("weight"),
    PHONE("phone");
    
    
    

    private final String event;

    private Event(String event) {
        this.event = event;
    }

    public String getValue() {
        return event;
    }

}

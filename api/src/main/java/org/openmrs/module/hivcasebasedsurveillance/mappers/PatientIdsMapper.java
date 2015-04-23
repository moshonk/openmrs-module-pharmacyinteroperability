package org.openmrs.module.hivcasebasedsurveillance.mappers;

import java.util.HashSet;

import org.kemricdc.entities.IdentifierType;
import org.kemricdc.entities.PersonIdentifier;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;

public class PatientIdsMapper {
	private static final int HEI_ID = 7;
	private static final int MCH_ID = 4;
	private static final int PSC_ID = 6;
	private HashSet<PersonIdentifier> patientIds = new HashSet<PersonIdentifier>();
	private Patient patient;

	public PatientIdsMapper(Patient patient) {
		this.patient = patient;
	}

	public HashSet<PersonIdentifier> getPatientIds() {
		patientIds = getAssignedPatientIds();
		return patientIds;
	}

	public void setPatientIds(HashSet<PersonIdentifier> patientIds) {
		this.patientIds = patientIds;
	}

	private HashSet<PersonIdentifier> getAssignedPatientIds() {
		HashSet<PersonIdentifier> patientIds = new HashSet<PersonIdentifier>();

		PersonIdentifier p0 = new PersonIdentifier();
		p0.setIdentifierType(IdentifierType.PID);
		p0.setIdentifier(this.patient.getPatientId().toString());
		patientIds.add(p0);
		for (PatientIdentifierType pidType : Context.getPatientService().getPatientIdentifierTypes(null, null, null, null)) {
			PatientIdentifier patientIdentifier = this.patient.getPatientIdentifier(pidType);
			if (patientIdentifier != null) {
				String patientIdentifierValue = patientIdentifier.getIdentifier();
				switch (pidType.getId()) {
				case MCH_ID:// MCH ID
					if (patientIdentifierValue != null) {
						PersonIdentifier p = new PersonIdentifier();
						p.setIdentifierType(IdentifierType.MCH);
						p.setIdentifier(patientIdentifierValue);
						patientIds.add(p);
					}
					break;
				case HEI_ID: // HEI ID
					if (patientIdentifierValue != null) {
						PersonIdentifier p = new PersonIdentifier();
						p.setIdentifierType(IdentifierType.HEI);
						p.setIdentifier(patientIdentifierValue);
						patientIds.add(p);
					}
					break;
				case PSC_ID: // PSC NUmber
					if (patientIdentifierValue != null) {
						PersonIdentifier p = new PersonIdentifier();
						p.setIdentifierType(IdentifierType.CCC);
						p.setIdentifier(patientIdentifierValue);
						patientIds.add(p);
					}
					break;
				default:
					break;
				}
			}
		}

		return patientIds;
	}
}

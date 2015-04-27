package org.openmrs.module.pharmacyinteroperability.advice;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kemricdc.constants.Triggers;
import org.kemricdc.entities.AppProperties;
import org.kemricdc.entities.Person;
import org.kemricdc.entities.PersonIdentifier;
import org.kemricdc.hapi.EventsHl7Service;
import org.kemricdc.hapi.util.OruFiller;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacyinteroperability.mappers.OruFillerMapper;
import org.openmrs.module.pharmacyinteroperability.mappers.PatientIdsMapper;
import org.openmrs.module.pharmacyinteroperability.mappers.PersonMapper;
import org.openmrs.module.pharmacyinteroperability.utils.AppPropertiesLoader;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

public class PatientEncounterAroundAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {
	
	/**
	 * 
	 */
	private static final int CLINICIAN_FORM_ID = 81;
	
	private static final int CHILD_WHO_STAGE_2_ID = 1221;
	
	private static final int CHILD_WHO_STAGE_3_ID = 1222;
	
	private static final int CHILD_WHO_STAGE_4_ID = 1223;
	
	private static final int ADULT_WHO_STAGE_4_ID = 1207;
	
	private static final int ADULT_WHO_STAGE_3_ID = 1206;
	
	private static final int ADULT_WHO_STAGE_2_ID = 1205;
	
	private static final int CHILD_WHO_STAGE_1_ID = 1220;
	
	private static final int ADULT_WHO_STAGE_1_ID = 1204;
	
	private static final long serialVersionUID = -3842820316745098958L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		if (method.getName().equalsIgnoreCase("saveEncounter")) {
			return true;
		}
		return false;
	}
	
	@Override
	public Advice getAdvice() {
		return new PatientEncounterAroundAdvice();
	}
	
	private class PatientEncounterAroundAdvice implements MethodInterceptor {
		
		private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
		
		public Object invoke(MethodInvocation invocation) throws Throwable {
			Object args[] = invocation.getArguments();
			Encounter savedEncounter = (Encounter) args[0];
			Date startDate = new Date();
			Date lastDate = OpenmrsUtil.getLastMomentOfDay(startDate);
			
			Boolean newEncounter = savedEncounter.getId() == null ? true : false;
			
			Object o = null;
			o = invocation.proceed();
			
			if (savedEncounter.getForm().getId() == CLINICIAN_FORM_ID) {
				
				AppProperties appProperties = new AppPropertiesLoader(new AppProperties()).getAppProperties();
				Patient omrsPatient = null; //OpenMRS Patient object
				org.openmrs.Person omrsPerson = null; //OpenMRS Person object
				Person oecPerson = new Person(); //OEC Person object
				HashSet<PersonIdentifier> patientIds = null;
				PersonMapper personMapper = null;
				List<Encounter> myEncounters = null;
				List<Obs> myObs = null;
				List<OruFiller> fillers = null;
				omrsPatient = savedEncounter.getPatient();
				omrsPerson = Context.getPersonService().getPerson(omrsPatient.getPatientId());
				patientIds = new PatientIdsMapper(omrsPatient).getPatientIds();
				personMapper = new PersonMapper(omrsPatient, oecPerson);
				personMapper.mapPatient(patientIds);
				List<Encounter> patientEncounters = Context.getEncounterService().getEncountersByPatient(omrsPatient);
				int clinicianEncounters = 0;
				for (Encounter encounter : patientEncounters) {
					if (encounter.getForm().getId() == CLINICIAN_FORM_ID) {
						clinicianEncounters++;
					}
				}
				if (clinicianEncounters == 1) {
					myEncounters = Context.getEncounterService().getEncounters(omrsPatient, null, startDate, lastDate, null,
					    null, null, null, null, false);
					myObs = Context.getObsService().getObservations(Collections.singletonList(omrsPerson), myEncounters,
					    null, null, null, null, null, null, null, null, null, false);
					fillers = new ArrayList<OruFiller>();
					Date dateEnrolled = new Date();
					OruFiller dateEnrolledOruFiller = new OruFiller();
					dateEnrolledOruFiller.setCodingSystem((String) appProperties.getProperty("coding_system"));
					dateEnrolledOruFiller.setObservationIdentifier("date_enrolled");
					dateEnrolledOruFiller.setObservationValue(sdf.format(dateEnrolled));
					fillers.add(dateEnrolledOruFiller);
					try {
						processObs(appProperties, myObs, fillers);
					}
					catch (Exception e) {
						log.debug(e.getMessage());
					}
					try {
						EventsHl7Service eventsHl7Service = new EventsHl7Service(personMapper.getOecPerson(), fillers,
						        appProperties);
						
						if (newEncounter) {
							eventsHl7Service.doWork(Triggers.R01.getValue());
						} else {
							eventsHl7Service.doWork(Triggers.R01.getValue());
						}
					}
					catch (Exception ex) {
						log.debug(ex);
						System.out.println("Unable to send HL7 message: " + ex.getMessage());
					}
				}
			}
			return o;
		}
		
	}
	
	private void processObs(AppProperties appProperties, List<Obs> myObs, List<OruFiller> fillers) throws Exception {
		for (Obs obs : myObs) {
			OruFiller oruFiller = new OruFiller();
			OruFillerMapper oruFillerMapper = new OruFillerMapper(obs, oruFiller);
			oruFiller.setCodingSystem((String) appProperties.getProperty("coding_system"));
			
			switch (obs.getConcept().getConceptId()) {
				case 6882:// Other illnesses
					oruFillerMapper.setObservationIdentifier("other_illnesses");
					oruFillerMapper.mapValue(null);
					fillers.add(oruFillerMapper.getOruFiller());
					break;
				case 6218: // Drug allergies
					oruFillerMapper.setObservationIdentifier("adr");
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapValue(null);
					break;
				case 6114: // Smoke
					int substanceAbuseResponseConceptId = obs.getValueCoded().getConceptId();
					if (substanceAbuseResponseConceptId == 6112) {
						oruFillerMapper.setObservationIdentifier("smoke");
						fillers.add(oruFillerMapper.getOruFiller());
						oruFillerMapper.mapValue(1);
					}
					if (substanceAbuseResponseConceptId == 6111) {
						oruFillerMapper.setObservationIdentifier("alcohol");
						fillers.add(oruFillerMapper.getOruFiller());
						oruFillerMapper.mapValue(1);
					}
					break;
				case 6147:// Referral Source
					oruFillerMapper.setObservationIdentifier("source");
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapValue(null);
					break;
				case 6608:// Service
					oruFillerMapper.setObservationIdentifier("service");
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapValue(null);
					break;
				case 6561:// Service
					oruFillerMapper.setObservationIdentifier("start_regimen");
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapValue(null);
					break;
				case 6154:// Service
					oruFillerMapper.setObservationIdentifier("start_regimen_date");
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapValue(null);
					break;
				case 6583: //FP
					oruFillerMapper.setObservationIdentifier("fplan");
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapValue(null);
					break;
				case 5090: //Height
					oruFillerMapper.setObservationIdentifier("start_height");
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapValue(null);
					break;
				case 5089: //FP
					oruFillerMapper.setObservationIdentifier("start_weight");
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapValue(null);
					break;
				case 1048: //HIV Status Disclosure
					oruFillerMapper.setObservationIdentifier("disclosure");
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapValue(null);
					break;
				case 5356:// WHO Stage
					oruFillerMapper.setObservationIdentifier("who_stage");
					fillers.add(oruFillerMapper.getOruFiller());
					int whoStageConceptId = obs.getValueCoded().getConceptId();
					Integer whoStage = -1;
					if (whoStageConceptId == ADULT_WHO_STAGE_1_ID || whoStageConceptId == CHILD_WHO_STAGE_1_ID) {
						whoStage = 1;
					} else if (whoStageConceptId == ADULT_WHO_STAGE_2_ID || whoStageConceptId == CHILD_WHO_STAGE_2_ID) {
						whoStage = 2;
					} else if (whoStageConceptId == ADULT_WHO_STAGE_3_ID || whoStageConceptId == CHILD_WHO_STAGE_3_ID) {
						whoStage = 3;
					} else if (whoStageConceptId == ADULT_WHO_STAGE_4_ID || whoStageConceptId == CHILD_WHO_STAGE_4_ID) {
						whoStage = 4;
					}
					oruFillerMapper.mapValue(whoStage.toString());
					break;
			}
		}
	}
}

package org.openmrs.module.hits.advice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.hits.HITSConstants;
import org.openmrs.module.hits.httpclient.HttpClient;
import org.openmrs.module.hits.mapping.PatientDetailsMapping;
import org.springframework.aop.AfterReturningAdvice;

public class SaveEncounterAdvice implements AfterReturningAdvice {

	@Override
	public void afterReturning(Object returnValue, Method method,
			Object[] arguments, Object target) throws Throwable {
		if (method.getName() == "saveEncounter") {
			Encounter encounter = (Encounter) returnValue;

			Map<String, String> values = new HashMap<String, String>();

			PatientDetailsMapping patientDetailsMapping = new PatientDetailsMapping(
					encounter.getPatient());

			patientDetailsMapping.mapPatientDetails(values);

			values.put("method", "APIupdateRecord");

			if (HITSConstants.HEI_FORM_IDS.contains(encounter.getForm().getFormId())) {
				for (Obs obs : encounter.getObs()) {
					switch (obs.getConcept().getConceptId()) {
					case 7046:
						values.put("mother_receive_art_prophylaxis", obs
								.getValueCoded().getName().getName());
						break;

					case 7047:
						values.put("mother_intrapartum_regimen", obs
								.getValueCoded().getName().getName());
						System.out.println(obs
								.getValueCoded().getName().getName());
						break;

					case 7044:
						values.put("infant_postpartum_regimen", obs
								.getValueCoded().getName().getName());
						break;

					case 7062:
						values.put("date_specimen_obtained",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					case 7061:
						values.put("specimen_results", obs.getValueCoded()
								.getName().getName());
						break;

					case 7064:
						values.put("date_specimen_results",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					case 7072:
						values.put("date_retest_specimen_obtained",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					case 7073:
						values.put("retest_specimen_results", obs
								.getValueCoded().getName().getName());
						break;

					case 7075:
						values.put("date_retest_specimen_results",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					case 7070:
						values.put("antibody_retest_9", obs.getValueCoded()
								.getName().getName());
						break;

					case 7071:
						values.put("date_antibody_retest_9",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					case 7081:
						values.put("antibody_retest_18", obs.getValueCoded()
								.getName().getName());
						break;

					case 7080:
						values.put("date_antibody_retest_18",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					case 6147:
						values.put("entry_point", obs.getValueCoded().getName()
								.getName());
						break;

					case 6157:
						values.put("delivery_location", obs.getValueCoded()
								.getName().getName());
						break;

					case 7066:
						values.put("date_c_specimen_obtained",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					case 7065:
						values.put("c_specimen_results", obs.getValueCoded()
								.getName().getName());
						break;

					case 7068:
						values.put("date_c_specimen_results",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					case 7076:
						values.put("date_c_retest_specimen_obtained",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					case 7077:
						values.put("c_retest_specimen_results", obs
								.getValueCoded().getName().getName());
						break;

					case 7079:
						values.put("date_c_retest_specimen_results",
								new DateTime(obs.getValueDate())
										.toString("yyyy/MM/dd"));
						break;

					}

				}
			}

			HttpClient httpClient = new HttpClient();
			httpClient.setParameters(values);
			Thread thread = new Thread(httpClient);
			thread.run();
		}

	}

}

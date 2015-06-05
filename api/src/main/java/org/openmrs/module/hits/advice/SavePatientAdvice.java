package org.openmrs.module.hits.advice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.hits.HITSConstants;
import org.openmrs.module.hits.httpclient.HttpClient;
import org.openmrs.module.hits.mapping.PatientDetailsMapping;
import org.springframework.aop.AfterReturningAdvice;

public class SavePatientAdvice implements AfterReturningAdvice {

	private Log log = LogFactory.getLog(this.getClass());

	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		Map<String, String> parameters = new HashMap<String, String>();
		if (method.getName().equals("savePatient")) {
			log.info("Method: " + method.getName());
			Patient savedPatient = (Patient) returnValue;
			String clinicId = Context.getAdministrationService().getGlobalProperty("HITsClinicId");
			if (clinicId == null) {
				clinicId = String.valueOf(HITSConstants.DEFAULT_CLINIC_ID);
			}

			if (savedPatient.getPatientIdentifier(Context.getPatientService().getPatientIdentifierType(HITSConstants.HITS_IDENTIFIER_TYPE_ID)) == null) {
				if (savedPatient.getPatientIdentifier(Context.getPatientService().getPatientIdentifierType(HITSConstants.HEI_IDENTIFIER_TYPE_ID)) != null) {

					PatientDetailsMapping patientDetailsMapping = new PatientDetailsMapping(savedPatient);
					patientDetailsMapping.mapPatientDetails(parameters);
					
					parameters.put("clinic_id", clinicId);
					parameters.put("method", "APIcreateRecord");
					HttpClient httpClient = new HttpClient();
					httpClient.setParameters(parameters);
					httpClient.setPatientUuId(savedPatient.getUuid());
					//httpClient.sendPatientToHITS();
					Thread thread = new Thread(httpClient);
					thread.run();
				}
			}
		}
	}
}

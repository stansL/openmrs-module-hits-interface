package org.openmrs.module.hits;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.springframework.transaction.annotation.Transactional;

public class HITSUtil {
	
	public static Boolean checkMessageStatus(HITSResponse response) {
		if (response.getResult().equalsIgnoreCase("SUCCESS")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Transactional	
	public static void saveHITSId(HITSResponse response) {
		Patient patient = Context.getPatientService().getPatientByUuid(response.getPatientUuId());
		PatientIdentifierType hitsIdentifierType = Context.getPatientService().getPatientIdentifierType(9);
		PatientIdentifier hitsIdentifier = new PatientIdentifier();
		hitsIdentifier.setIdentifierType(hitsIdentifierType);
		hitsIdentifier.setIdentifier(response.getHitsId());
		patient.addIdentifier(hitsIdentifier);
		System.out.println("Patient Identifier Count" + patient.getActiveIdentifiers().size());
		try {
			Context.getPatientService().savePatient(patient);
		}
		catch (APIException e) {
			e.printStackTrace();
		}
	}

}

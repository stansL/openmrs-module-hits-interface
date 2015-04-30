package org.openmrs.module.hits;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.springframework.transaction.annotation.Transactional;

public class HITSPatientService {
	
	@Transactional	
	public static void saveHITSId(HITSResponse response) {
		if (response.getResult().equalsIgnoreCase("SUCCESS")) {
			Patient patient = Context.getPatientService().getPatientByUuid(
					response.getPatientUuId());
			PatientIdentifierType hitsIdentifierType = Context
					.getPatientService().getPatientIdentifierType(9);
			PatientIdentifier hitsIdentifier = new PatientIdentifier();
			hitsIdentifier.setIdentifierType(hitsIdentifierType);
			hitsIdentifier.setIdentifier(response.getHitsId());
			patient.addIdentifier(hitsIdentifier);
			System.out.println("Patient Identifier Count"
					+ patient.getActiveIdentifiers().size());
			try {
				Context.getPatientService().savePatient(patient);
			} catch (APIException e) {
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("ERROR: " + response.getMessage());			
		}
	}

}

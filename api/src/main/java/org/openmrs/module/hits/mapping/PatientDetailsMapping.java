package org.openmrs.module.hits.mapping;

import java.util.Map;

import org.joda.time.DateTime;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;

public class PatientDetailsMapping {
	
	private Patient patient;
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public PatientDetailsMapping(Patient patient)
	{
		this.patient = patient;		
	}
	
	public void mapPatientDetails(Map<String,String> values)
	{
		PatientIdentifier hitsIdentifier = patient
				.getPatientIdentifier(
						Context.getPatientService()
								.getPatientIdentifierType(9));
		PatientIdentifier heiIdentifier = patient
				.getPatientIdentifier(
						Context.getPatientService()
								.getPatientIdentifierType(8));
		
		String formattedDob = new DateTime(patient
				.getBirthdate()).toString("yyyy/MM/dd");
			
		Patient mother = null;
		for (Relationship relationship : Context.getPersonService()
				.getRelationshipsByPerson(patient)) {
			if (relationship.getRelationshipType().getbIsToA()
					.equals("Child")) {
				if (relationship.getPersonA().getGender().equals("F")) {
					mother = Context.getPatientService().getPatient(
							relationship.getPersonA().getId());
				}
			}
	
		}
	
		
		if (hitsIdentifier != null) {
			values.put("hitsid", hitsIdentifier.getIdentifier());
		}
		if (heiIdentifier != null) {
			values.put("infant_ID", heiIdentifier.getIdentifier());
		}
		values.put("infant_dob", formattedDob);
		values.put("infant_gender", patient.getGender());
	
		if (mother != null) {
			values.put("mother_dob", new DateTime(mother.getBirthdate())
					.toString("yyyy/MM/dd"));
			values.put("mother_age", mother.getAge().toString());
			values.put("mother_id", mother.getPatientIdentifier()
					.getIdentifier());
		}
	}
}

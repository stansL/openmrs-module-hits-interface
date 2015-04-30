package org.openmrs.module.hits;

public class HITSResponse {
	private String message;
	private String hitsId;
	private String result;
	private String patientUuId;
	
	public String getPatientUuId() {
		return patientUuId;
	}
	public void setPatientUuId(String patientUuId) {
		this.patientUuId = patientUuId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getHitsId() {
		return hitsId;
	}
	public void setHitsId(String hitsId) {
		this.hitsId = hitsId;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
}

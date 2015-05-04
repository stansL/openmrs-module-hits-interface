package org.openmrs.module.hits.httpclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kemricdc.constants.Triggers;
import org.kemricdc.entities.AppProperties;
import org.kemricdc.entities.IdentifierType;
import org.kemricdc.entities.MaritalStatus;
import org.kemricdc.entities.Person;
import org.kemricdc.entities.PersonIdentifier;
import org.kemricdc.entities.Sex;
import org.kemricdc.hapi.EventsHl7Service;
import org.kemricdc.hapi.IHL7Service;
import org.kemricdc.hapi.util.OruFiller;
import org.openmrs.module.hits.HITSConstants;
import org.openmrs.module.hits.HITSUtil;
import org.openmrs.module.hits.HITSResponse;
import org.openmrs.module.hits.HITSResponseDeserializer;
import org.openmrs.module.hits.utils.AppPropertiesLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpClient implements Runnable {
	
	protected Log log = LogFactory.getLog(getClass());
	
	private final String token = "K3mr1CdcT35t";
	
	private Map<String,String> parameters;
	
	private String patientUuId;

	public String getPatientUuId() {
		return patientUuId;
	}

	public void setPatientUuId(String patientUuId) {
		this.patientUuId = patientUuId;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public HttpClient() {
	}

	public void sendPatientToHITS() throws MalformedURLException, IOException {

		String url = "http://www.hitsystem.net/API/components/apiRecord.cfc";		
		String urlParameters = "";
		DataOutputStream output;
		HttpURLConnection connection;
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(HITSResponse.class,
				new HITSResponseDeserializer());		
		Gson gson = gsonBuilder.create();
		
		for (Map.Entry<String, String> parameter : parameters.entrySet()) {
			urlParameters += parameter.getKey() + "=" + parameter.getValue() + "&";
		}
		
		urlParameters += "token=" + token;

		connection = (HttpURLConnection)new URL(url).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));		
		connection.setDoOutput(true);
		output = new DataOutputStream(connection.getOutputStream());
		try {			
			output.writeBytes(urlParameters);
			output.flush();
		} 
		catch (IOException ex) {
			log.error(ex.getMessage());
		}
		finally {
			output.close();
		}
		
		int responseCode = connection.getResponseCode();
		List<Integer> errorResponseCode = Arrays.asList(404,408);
		
		if (errorResponseCode.contains(responseCode)) {
			saveParametersToFileSystem();
		}

		InputStream response = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				response));
		String line = null;
		StringBuffer responseBuffer = new StringBuffer();
		while ((line = br.readLine()) != null) {
			log.info(line);
			System.out.println(line);
			responseBuffer.append(line);				
		}
		HITSResponse hitsResponse = gson.fromJson(responseBuffer.toString(),
			HITSResponse.class);
		Boolean messageSuccess = HITSUtil.checkMessageStatus(hitsResponse);
		if (!messageSuccess) {
			saveParametersToFileSystem();
		}
		else if (parameters.get("method").equals("APIcreateRecord")) {			
			hitsResponse.setPatientUuId(patientUuId);
			HITSUtil.saveHITSId(hitsResponse);
			log.info("HITS ID: " + hitsResponse.getHitsId());
			System.out.println("HITS ID: " + hitsResponse.getHitsId());
		}
		br.close();
		output.close();
		connection.disconnect();
		
	}

	private void saveParametersToFileSystem() {
	    // TODO Auto-generated method stub
		List<OruFiller> fillers = new ArrayList<OruFiller>();
		AppProperties appProperties = new AppPropertiesLoader(new AppProperties()).getAppProperties();
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
		Person person = new Person();
		Set<PersonIdentifier> identifiers = new HashSet<PersonIdentifier>();
		person.setMaritalStatus(MaritalStatus.MISSING);
	    for (Map.Entry<String, String> parameter : this.parameters.entrySet()) {
			if (parameter.getKey() == HITSConstants.HEI_ID) {
				PersonIdentifier pIdentifier = new PersonIdentifier();
				pIdentifier.setIdentifierType(IdentifierType.HEI);
				pIdentifier.setIdentifier(parameter.getValue());
				identifiers.add(pIdentifier);
				continue;
			} 
			if (parameter.getKey() == HITSConstants.HITS_ID) {
				PersonIdentifier pIdentifier = new PersonIdentifier();
				pIdentifier.setIdentifierType(IdentifierType.HITS);
				pIdentifier.setIdentifier(parameter.getValue());
				identifiers.add(pIdentifier);
				continue;
			} 			
			if (parameter.getKey() == HITSConstants.OPENMRS_UUID) {
				PersonIdentifier pIdentifier = new PersonIdentifier();
				pIdentifier.setIdentifierType(IdentifierType.OPENMRSUUID);
				pIdentifier.setIdentifier(parameter.getValue());
				identifiers.add(pIdentifier);				
				continue;
			}			
			if (parameter.getKey().equalsIgnoreCase("infant_dob")) {
				try {
					person.setDob(format.parse(parameter.getValue()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				continue;
			}
			if (parameter.getKey().equalsIgnoreCase("infant_gender")) {
				if(parameter.getValue().equalsIgnoreCase("F")){
					person.setSex(Sex.FEMALE);
				}
				else if(parameter.getValue().equalsIgnoreCase("M")){
					person.setSex(Sex.MALE);
				}				
				continue;
			}
			if (parameter.getKey() == HITSConstants.MOTHER_ID) {
				person.setMotherId(parameter.getValue());
				continue;
			}			
			
			OruFiller parameterOruFiller = new OruFiller();
			parameterOruFiller.setCodingSystem((String) appProperties.getProperty("coding_system"));
			parameterOruFiller.setObservationIdentifier(parameter.getKey());
			parameterOruFiller.setObservationValue(parameter.getValue());
			fillers.add(parameterOruFiller);
        }
	    
	    OruFiller parameterOruFiller = new OruFiller();
		parameterOruFiller.setCodingSystem((String) appProperties.getProperty("coding_system"));
		parameterOruFiller.setObservationIdentifier("token");
		parameterOruFiller.setObservationValue(token);
		fillers.add(parameterOruFiller);
		
	    person.setPersonIdentifiers(identifiers);
	    
	    IHL7Service hl7Service = new EventsHl7Service(person, fillers, appProperties);
	    hl7Service.doWork(Triggers.R01.getValue());
    }

	@Override
	public void run() {
		try {
			sendPatientToHITS();
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

}

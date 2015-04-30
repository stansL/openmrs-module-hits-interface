package org.openmrs.module.hits.httpclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.hits.HITSPatientService;
import org.openmrs.module.hits.HITSResponse;
import org.openmrs.module.hits.HITSResponseDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpClient implements Runnable {
	
	protected Log log = LogFactory.getLog(getClass());
	
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
		String token = "K3mr1CdcT35t";
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

		InputStream response = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				response));
		String line = null;

		if (parameters.get("method").equals("APIcreateRecord")) {
			while ((line = br.readLine()) != null) {
				log.info(line);
				System.out.println(line);
				HITSResponse hitsResponse = gson.fromJson(line,
						HITSResponse.class);
				hitsResponse.setPatientUuId(patientUuId);
				HITSPatientService.saveHITSId(hitsResponse);
				log.info("HITS ID: " + hitsResponse.getHitsId());
				System.out.println("HITS ID: " + hitsResponse.getHitsId());
			}
		}
		else {
			while ((line = br.readLine()) != null) {
				log.info(line);
				System.out.println(line);
			}
		}
		br.close();
		output.close();
		connection.disconnect();
		
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

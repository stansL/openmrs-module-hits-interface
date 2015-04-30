package org.openmrs.module.hits;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class HITSResponseDeserializer implements JsonDeserializer<HITSResponse> {

	@Override
	public HITSResponse deserialize(JsonElement json, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		// TODO Auto-generated method stub
		final JsonObject jsonObject = json.getAsJsonObject();

	    final JsonElement jsonMessage = jsonObject.get("MESSAGE");
	    final String message = jsonMessage.getAsString();

	    final String hitsId = jsonObject.get("HITSID").getAsString();
	    final String result = jsonObject.get("RESULT").getAsString();
	    
	    final HITSResponse hitsResponse = new HITSResponse();
	    hitsResponse.setHitsId(hitsId);
	    hitsResponse.setMessage(message);
	    hitsResponse.setResult(result);
	    
	    return hitsResponse;

	}

}

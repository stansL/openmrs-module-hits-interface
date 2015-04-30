package org.openmrs.module.hits.utils;

import java.io.File;
import java.util.Properties;

import org.kemricdc.entities.AppProperties;
import org.openmrs.api.context.Context;
import org.openmrs.module.hits.HITSConstants;


public class AppPropertiesLoader {
	private AppProperties appProperties;
	
	public AppPropertiesLoader(AppProperties appProperties){
		this.appProperties = appProperties;
	}
	
	public AppProperties getAppProperties() {
		String propertiesFileLocation = Context.getAdministrationService().getGlobalProperty(
		    HITSConstants.PROPERTIES_FILE_LOCATION_GLOBAL_PROPERTY);
		File propertiesFile = new File(propertiesFileLocation);
		if (propertiesFile.isFile()) {
			this.setAppProperties(new AppProperties(new Properties(), propertiesFile));
		} else {
			this.setAppProperties(null);
		}
		return appProperties;
	}
	public void setAppProperties(AppProperties appProperties) {
		this.appProperties = appProperties;
	}	
}

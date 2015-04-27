package org.openmrs.module.pharmacyinteroperability.utils;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kemricdc.entities.AppProperties;
import org.openmrs.api.context.Context;

public class AppPropertiesLoader {
	private Log log = LogFactory.getLog(this.getClass());
	private static final String PROPERTIES_FILE_LOCATION_GLOBAL_PROPERTY = "hcbsurveillance.propertiesFileLocation";
	private AppProperties appProperties;
	
	public AppPropertiesLoader(AppProperties appProperties){		
		String propertiesFileLocation = Context.getAdministrationService().getGlobalProperty(PROPERTIES_FILE_LOCATION_GLOBAL_PROPERTY);
		File propertiesFile = new File(propertiesFileLocation);
		if (propertiesFile.isFile()) {
			this.appProperties = appProperties;
			this.appProperties.setPropertiesFile(propertiesFile);			
		}
		else {
			log.error("app.properties file does not exist");
		}
		
	}
	
	public AppProperties getAppProperties() {
		return appProperties;		
	}	
}

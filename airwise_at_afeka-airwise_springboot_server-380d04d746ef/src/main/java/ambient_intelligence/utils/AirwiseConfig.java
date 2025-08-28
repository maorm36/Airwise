package ambient_intelligence.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AirwiseConfig {

	private static String systemID;
	private static String idSeparator;
	
	public static String getSystemID() {
		return systemID;
	}

	public static String getIdSeparator() {
		return idSeparator;
	}

	@Value("${spring.application.name:dummy}")
	public void setSystemID(String systemID) {
		AirwiseConfig.systemID = systemID;
	}

	@Value("${spring.application.idSeparator:#::#}")
	public void setIdSeparator(String idSeparator) {
		AirwiseConfig.idSeparator = idSeparator;
	}

}

package edu.upenn.cis.cis455.http;

import java.util.HashMap;
import java.util.Map;

public class HTTP {

	private static final String protocol = "HTTP";
	private static final String version1 = "1";
	private static final String version11 = "1.1";
	private static Map<String, String> responseCodes;
	
	static
	{
		responseCodes = new HashMap<String, String>();
		responseCodes.put("200","OK");
		responseCodes.put("404","NOT FOUND");
		responseCodes.put("500", "SERVER ERROR");
		responseCodes.put("400", "BAD REQUEST");
	}

	public static String getProtocol() {
		return protocol;
	}

	public static String getVersion1() {
		return version1;
	}

	public static String getVersion11() {
		return version11;
	}

	public static Map<String, String> getResponseCodes() {
		return responseCodes;
	}
	
}

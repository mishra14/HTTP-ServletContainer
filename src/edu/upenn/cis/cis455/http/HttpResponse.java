package edu.upenn.cis.cis455.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse 
{
	private String protocol;
	private String version;
	private String responseCode;
	private String responseCodeString;
	private Map<String, ArrayList<String>> headers;
	private String data;
	
	public HttpResponse(String protocol, String version, String responseCode,
			String responseCodeString, Map<String, ArrayList<String>> headers, String data) {
		super();
		this.protocol = protocol;
		this.version = version;
		this.responseCode = responseCode;
		this.responseCodeString = responseCodeString;
		this.headers = headers;
		this.data = data;
	}
	
	public HttpResponse(String protocol, String version, String responseCode,
			String responseCodeString, Map<String, ArrayList<String>> headers) {
		super();
		this.protocol = protocol;
		this.version = version;
		this.responseCode = responseCode;
		this.responseCodeString = responseCodeString;
		this.headers = headers;
	}
	
	public HttpResponse() {
		this.protocol = "";
		this.version = "";
		this.responseCode = "";
		this.responseCodeString = "";
		this.headers = new HashMap<String, ArrayList<String>>();
		this.data = "";
	}

	public String getResponseString()
	{
		StringBuilder response = new StringBuilder();
		response.append(protocol+"/"+version+" "+responseCode+" "+responseCodeString+"\r\n");
		if(headers!=null)
		{
			for(Map.Entry<String, ArrayList<String>> header : headers.entrySet())
			{
				response.append(header.getKey()+":"+header.getValue()+"\r\n");
			}
		}
		response.append("\r\n");
		if(data!=null)
		{
			response.append(data);
		}
		return response.toString();
	}
	
	public String getResponseStringHeadersOnly()
	{
		StringBuilder response = new StringBuilder();
		response.append(protocol+"/"+version+" "+responseCode+" "+responseCodeString+"\r\n");
		if(headers!=null)
		{
			for(Map.Entry<String, ArrayList<String>> header : headers.entrySet())
			{
				StringBuilder headers = new StringBuilder();
				for(int i=0;i<header.getValue().size();i++)
				{
					headers.append(header.getValue().get(i));
					if(i<header.getValue().size()-1)
					{
						headers.append(",");
					}
				}
				response.append(header.getKey()+":"+headers.toString()+"\r\n");
			}
		}
		response.append("\r\n");
		return response.toString();
	}
	
	@Override
	public String toString() {
		return "HttpResponse [protocol=" + protocol + ", version=" + version
				+ ", responseCode=" + responseCode + ", responseCodeString="
				+ responseCodeString + ", headers=" + headers + "]";
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseCodeString() {
		return responseCodeString;
	}

	public void setResponseCodeString(String responseCodeString) {
		this.responseCodeString = responseCodeString;
	}

	public Map<String, ArrayList<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, ArrayList<String>> headers) {
		this.headers = headers;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void reset() {
		this.responseCode = "";
		this.responseCodeString = "";
		this.headers.clear();
		this.data = "";
	}
	
}
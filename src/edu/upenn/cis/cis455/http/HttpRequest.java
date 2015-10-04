package edu.upenn.cis.cis455.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;

/*
 GET /temp HTTP/1.1 
 Host: localhost:8080
 User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:28.0) Gecko/20100101 Firefox/28.0
 Accept: text/html,application/xhtml+xml,application/xml;q=0.9;q=0.8
 Accept-Language: en-US,en;q=0.5
 Accept-Encoding: gzip, deflate
 Connection: keep-alive 
 */

public class HttpRequest 
{
	private static final Logger logger = Logger.getLogger(HttpRequest.class);
	private String operation;
	private String protocol;
	private String version;
	private String resource;
	private Map<String,ArrayList<String>> headers;
	private boolean validRequest;
	private String servletUrl;
	private String contextPath="";
	private String servletPath;
	private String pathInfo;
	private String queryString;
	private String requestBody;
	
	public HttpRequest(String httpRequest)
	{
		headers=new HashMap<String, ArrayList<String>>();
		validRequest=true;
		String[] requestSplit=httpRequest.split("\n");
		String[] firstLineSplit=requestSplit[0].trim().split(" ");
		if(firstLineSplit.length<3)
		{
			logger.warn("invalid request - "+httpRequest);
			validRequest=false;
			return;
		}
		else
		{
			logger.info(requestSplit[0].trim());
			if(firstLineSplit.length<3 || firstLineSplit[2].split("/").length<2 || !firstLineSplit[2].split("/")[0].trim().equalsIgnoreCase("HTTP") || (!firstLineSplit[2].split("/")[1].trim().equalsIgnoreCase("1.0") && !firstLineSplit[2].split("/")[1].trim().equalsIgnoreCase("1.1")))
			{
				logger.warn("invalid first line in request - "+httpRequest);
				validRequest=false;
				return;
			}
			
			operation=firstLineSplit[0].trim();
			resource=firstLineSplit[1].trim();
			protocol=firstLineSplit[2].split("/")[0].trim();
			version=firstLineSplit[2].split("/")[1].trim();
		}
		for(int i=1;i<requestSplit.length;i++)
		{
			if(requestSplit[i].split(":").length<2)
			{
				logger.warn("invalid header in request - "+httpRequest);
			}
			else
			{
				logger.info(requestSplit[i]);
				String key = requestSplit[i].split(":")[0].trim().toLowerCase();
				String value = requestSplit[i].trim().split(":")[1].trim();
				if(requestSplit[i].split(":").length>2)
				{
					value=requestSplit[i].substring(requestSplit[i].indexOf(":")+1).trim();
				}
				if(headers.containsKey(key))
				{
					headers.get(key).add(value);
				}
				else
				{
					ArrayList<String> valueList = new ArrayList<String>();
					valueList.add(value);
					headers.put(key, valueList);
				}
			}
		}
		if(!headers.containsKey("host") && version.equalsIgnoreCase("1.1"))
		{
			//invalid http 1.1 request; respond with 400 error;
			logger.warn("HTTP 1.1 request without Host header - "+httpRequest);
			validRequest=false;
		}
	}
	
	public void updatePaths()
	{
		if(servletUrl!=null)
		{
			logger.info("Servlet URl - "+servletUrl);
			if(servletUrl.contains(".*"))
			{
				if(servletUrl.startsWith(".*"))
				{
					servletPath="";
				}
				else
				{
					servletPath = servletUrl.substring(0, servletUrl.indexOf(".*"));
				}
			}
			else
			{
				servletPath = servletUrl;
			}
			logger.info("Servlet PATH - "+servletPath);
			String path = resource.substring(servletPath.length());
			if(path.contains("?"))
			{
				pathInfo = path.split("\\?")[0];
				queryString = path.split("\\?")[1];
			}
			else
			{
				pathInfo = path;
			}
			logger.info("Path info - "+pathInfo);
			logger.info("quesry string - "+queryString);
		}
	}
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
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

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Map<String, ArrayList<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, ArrayList<String>> headers) {
		this.headers = headers;
	}

	public boolean isValidRequest() {
		return validRequest;
	}

	public void setValidRequest(boolean validRequest) {
		this.validRequest = validRequest;
	}

	@Override
	public String toString() {
		return "HttpRequest [operation=" + operation + ", protocol=" + protocol
				+ ", version=" + version + ", resource=" + resource
				+ ", headers=" + headers + ", validRequest=" + validRequest
				+ ", servletUrl=" + servletUrl + ", contextPath=" + contextPath
				+ ", servletPath=" + servletPath + ", pathInfo=" + pathInfo
				+ ", queryString=" + queryString + ", requestBody="
				+ requestBody + "]";
	}

	public String getServletUrl() {
		return servletUrl;
	}

	public void setServletUrl(String servletUrl) {
		this.servletUrl = servletUrl;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public HashMap<String,ArrayList<String>> getParams() {
		String paramString=null;
		HashMap<String,ArrayList<String>> parameters =  new HashMap<String,ArrayList<String>>();
		if(operation.equalsIgnoreCase("GET"))
		{
			paramString = queryString;
		}
		else if(operation.equalsIgnoreCase("POST"))
		{
			paramString = requestBody;
		}
		if(paramString!=null)
		{
			for(String parameter : paramString.split("&"))
			{
				if(parameter.contains("="))
				{
					String name = parameter.split("=")[0].trim();
					String value = parameter.split("=")[1].trim();
					if(parameters.containsKey(name))
					{
						parameters.get(name).add(value);
					}
					else
					{
						ArrayList<String> values = new ArrayList<String>();
						values.add(value);
						parameters.put(name, values);
					}
				}
			}
		}
		return parameters;
	}
	
}

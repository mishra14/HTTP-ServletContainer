package edu.upenn.cis.cis455.http;

/*
 * GET /test HTTP/1.1 
 * Host: localhost:8080
 * User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:28.0) Gecko/20100101 Firefox/28.0
 * Accept: text/html,application/xhtml+xml,application/xml;q=0.9;q=0.8
 * Accept-Language: en-US,en;q=0.5
 * Accept-Encoding: gzip, deflate
 * Connection: keep-alive
 * 
 */
public class Request 
{
	private String operation;
	private String protocol;
	private String version;
	private String resource;
	private boolean keepAlive;

}

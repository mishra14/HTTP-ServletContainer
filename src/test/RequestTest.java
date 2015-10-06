package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.upenn.cis.cis455.http.HttpRequest;
import edu.upenn.cis.cis455.servlet.Request;

public class RequestTest {
	 @Test
	  public void authTypeShouldReturnBasic() {
	
	    Request request = new Request();
	    assertEquals("BASIC", request.getAuthType());
	    
	 }
	 
	 @Test
	 public void getMethodShouldReturnGET() {
		 
		 HttpRequest httpRequest = new HttpRequest("GET / HTTP/1.0");
		 Request request = new Request(httpRequest);
		 assertEquals("GET", request.getMethod());
	 }
	
	 @Test
	 public void getProtocolShouldReturnHTTP() {
		 
		 HttpRequest httpRequest = new HttpRequest("GET / HTTP/1.0");
		 Request request = new Request(httpRequest);
		 assertEquals("HTTP/1.0", request.getProtocol());
	 }
	 
	 @Test
	 public void getSessionShouldNotCreateNewSession() {
		 
		 HttpRequest httpRequest = new HttpRequest("GET / HTTP/1.0");
		 Request request = new Request(httpRequest);
		 assertEquals(null, request.getSession(false));
	 }
}
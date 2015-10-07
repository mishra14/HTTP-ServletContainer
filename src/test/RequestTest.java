package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.upenn.cis.cis455.http.HttpRequest;
import edu.upenn.cis.cis455.servlet.Request;

/**
 * test class for Request class
 * @author cis455
 *
 */
public class RequestTest {
	/**
	 * check auth type 
	 */
	 @Test
	  public void authTypeShouldReturnBasic() {
	
	    Request request = new Request();
	    assertEquals("BASIC", request.getAuthType());
	    
	 }
	 /**
	  * test get method
	  */
	 @Test
	 public void getMethodShouldReturnGET() {
		 
		 HttpRequest httpRequest = new HttpRequest("GET / HTTP/1.0");
		 Request request = new Request(httpRequest);
		 assertEquals("GET", request.getMethod());
	 }
	/**
	 * test get protocol
	 */
	 @Test
	 public void getProtocolShouldReturnHTTP() {
		 
		 HttpRequest httpRequest = new HttpRequest("GET / HTTP/1.0");
		 Request request = new Request(httpRequest);
		 assertEquals("HTTP/1.0", request.getProtocol());
	 }
	 /**
	  * test get session with false param
	  */
	 @Test
	 public void getSessionShouldNotCreateNewSession() {
		 
		 HttpRequest httpRequest = new HttpRequest("GET / HTTP/1.0");
		 Request request = new Request(httpRequest);
		 assertEquals(null, request.getSession(false));
	 }
}
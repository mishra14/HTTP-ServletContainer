package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import edu.upenn.cis.cis455.http.HttpResponse;
import edu.upenn.cis.cis455.servlet.Request;
import edu.upenn.cis.cis455.servlet.Response;

public class ResponseTest {
	
	 @Test
	  public void contentTypeShouldReturnTheSetType() {

	    Request request = new Request();
	    HttpResponse httpResponse = new HttpResponse();
	    Response response = new Response(httpResponse, request);
	    response.setContentType("text/html");
	    assertEquals("text/html", response.getContentType());
	    }
	 
	 @Test
	 public void testSetStatus() {
		 
		int status = 500;
		Request request = new Request();
	    HttpResponse httpResponse = new HttpResponse();
	    Response response = new Response(httpResponse, request);
	    response.setStatus(status);
	    assertEquals(response.getHttpResponse().getResponseCode(), String.valueOf(status));
	 }
}
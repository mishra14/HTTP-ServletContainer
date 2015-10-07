package edu.upenn.cis.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Dummy servlet to test container
 * @author cis455
 *
 */
public class DummyServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DummyServlet.class);
	private String message;

	  public void init() throws ServletException
	  {
	      // Do required initialization
	      message = "Dummy servlet";
	  }

	  public void doGet(HttpServletRequest request,
	                    HttpServletResponse response)
	            throws ServletException, IOException
	  {
		  logger.info("Servlet in dummy servlet");
	      response.setContentType("text/html");
	      String html = "<html><body> Dummy Servlet</body></html>";
	      // Actual logic goes here.
	      PrintWriter out = response.getWriter();
	      out.println(html);
	      //response.setContentLength(html.length());
	      response.flushBuffer();
	  }
	  
	  public void destroy()
	  {
	      // do nothing.
	  }

	@Override
	public String toString() {
		return "WebServlet [message=" + message + "]";
	}
	


}

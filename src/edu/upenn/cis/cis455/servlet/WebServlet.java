package edu.upenn.cis.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class WebServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(WebServlet.class);
	private String message;

	  public void init() throws ServletException
	  {
	      // Do required initialization
	      message = "Hello World";
	  }

	  public void doGet(HttpServletRequest request,
	                    HttpServletResponse response)
	            throws ServletException, IOException
	  {
		  logger.info("Cookies - ");
		  for(Cookie cookie : request.getCookies())
		  {
			  logger.info(cookie.getName()+"="+cookie.getValue());
		  }
		  logger.info(request.getParameter("name1"));
		  logger.info("Servlet in WebServlet");
	      // Set response content type
	      response.setContentType("text/html");
	      String html = "<html><body> Hi There</body></html>";
	      // Actual logic goes here.
	      PrintWriter out = response.getWriter();
	      out.println(html);
	      response.setContentLength(html.length());
	      response.flushBuffer();
	  }
	  
	  public void doPost(HttpServletRequest request,
			  			 HttpServletResponse response)
			  throws ServletException, IOException
		{
			logger.info("Cookies - ");
			for(Cookie cookie : request.getCookies())
			{
				  logger.info(cookie.getName()+"="+cookie.getValue());
			}
			logger.info(request.getParameter("name1"));
			logger.info("Servlet in WebServlet - DO POST");
			// Set response content type
			response.setContentType("text/html");
			String html = "<html><body> Hi There</body></html>";
			// Actual logic goes here.
			PrintWriter out = response.getWriter();
			out.println(html);
			response.setContentLength(html.length());
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
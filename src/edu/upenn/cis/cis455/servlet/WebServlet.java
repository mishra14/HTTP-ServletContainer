package edu.upenn.cis.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.http.HTTP;

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
		  if(request.getSession()==null)
		  {
			  logger.info("No valid session, requesting a new one");
			  request.getSession(true);
			  logger.info("New session id - "+request.getSession(false).getId());
		  }
		  else
		  {
			  logger.info("Session id - "+request.getSession(false).getId());
		  }
		  logger.info("setting session time out to 8 seconds for - "+request.getSession(false).getId());
		  request.getSession(false).setMaxInactiveInterval(30);
		  logger.info("parameters - "+request.getParameterMap());
		  logger.info(request.getParameter("name1"));
		  logger.info("Servlet in WebServlet");
		  response.sendRedirect("inside");
	      // Set response content type
		  //logger.info("Error codes - "+HTTP.getResponseCodes());
		  Cookie cookie = new Cookie("Name", "Ankit");
		  response.addCookie(cookie);
		  //response.sendError(500);
	      response.setContentType("text/html");
	      String html = "<html><body> Hi There</body></html>";
	      // Actual logic goes here.
	      PrintWriter out = response.getWriter();
	      out.println(html);
	      //response.setContentLength(html.length());
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
			logger.info("parameters - "+request.getParameterMap());
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
	      logger.warn("WebServlet shutting down - "+toString());
	  }

	@Override
	public String toString() {
		return "WebServlet [message=" + message + "]";
	}
	
}
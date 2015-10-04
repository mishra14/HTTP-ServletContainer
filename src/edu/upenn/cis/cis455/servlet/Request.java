package edu.upenn.cis.cis455.servlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.upenn.cis.cis455.http.HTTP;
import edu.upenn.cis.cis455.http.HttpRequest;
import edu.upenn.cis.cis455.webserver.HttpServer;

/**
 * @author Todd J. Green
 */
public class Request implements HttpServletRequest {

	private HashMap<String, ArrayList<String>> parameters = new HashMap<String, ArrayList<String>>();
	private Properties m_props = new Properties();
	private Session m_session = null;
	private String m_method;
	private String urlpattern;
	private HttpRequest httpRequest;
	private Socket socket;
	private ArrayList<Cookie> cookies = new ArrayList<Cookie>();
	private String characterEncoding = "ISO-8859-1";
	private Locale locale;
	public Request() {
	}
	
	public Request(Session session) {
		m_session = session;
	}
	
	public Request(HttpRequest httpRequest)
	{
		this.httpRequest = httpRequest;
	}
	
	
	public String getUrlpattern() {
		return urlpattern;
	}

	public void setUrlpattern(String urlpattern) {
		this.urlpattern = urlpattern;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		return "BASIC";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() 
	{
		cookies = new ArrayList<Cookie>();
		if(httpRequest.getHeaders().containsKey("cookie"))
		{
			for(String cookieString : httpRequest.getHeaders().get("cookie"))
			{
				String[] cookiePairs = cookieString.split(";");
				for(String cookiePair : cookiePairs)
				{
					cookies.add(new Cookie(cookiePair.split("=")[0].trim(),cookiePair.split("=")[1].trim()));
				}
			}
		}
		Cookie[] cookieArray = ((ArrayList<Cookie>)cookies).toArray(new Cookie[cookies.size()]);
		return cookieArray;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String headerName) throws IllegalArgumentException {

		if(httpRequest.getHeaders().containsKey(headerName))
		{
			String dateString = httpRequest.getHeaders().get(headerName).get(0);
			Calendar date = HTTP.getDateFromString(dateString);
			if(date == null)
			{
				throw new IllegalArgumentException("Header - "+headerName+" is not a date header");
			}
			else
			{
				return date.getTimeInMillis();
			}
		}
		else
		{
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String headerName) {
		if(httpRequest.getHeaders().containsKey(headerName))
		{
			return httpRequest.getHeaders().get(headerName).get(0);
		}
		else
		{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration getHeaders(String headerName) {
		if(httpRequest.getHeaders().containsKey(headerName))
		{
			return Collections.enumeration(httpRequest.getHeaders().get(headerName));
		}
		else
		{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration getHeaderNames() {
		if(httpRequest.getHeaders()!=null)
		{
			if(httpRequest.getHeaders().isEmpty())
			{
				return Collections.emptyEnumeration();
			}
			else
			{
				return Collections.enumeration(httpRequest.getHeaders().keySet());
			}
		}
		else
		{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String headerName) throws NumberFormatException {
		if(httpRequest.getHeaders().containsKey(headerName))
		{
			int result = Integer.parseInt(httpRequest.getHeaders().get(headerName).get(0));
			return result;
		}
		else
		{
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return httpRequest.getOperation();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		return httpRequest.getPathInfo();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		return httpRequest.getQueryString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		if(httpRequest.getResource().contains("?"))
		{
			return httpRequest.getResource().split("\\?")[0];
		}
		else
		{
			return httpRequest.getResource();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		StringBuffer result = new StringBuffer();
		result.append("http://localhost:"+HttpServer.getPort()+getRequestURI());
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		return httpRequest.getServletPath();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean arg0) {
		if (arg0) {
			if (! hasSession()) {
				m_session = new Session();
			}
		} else {
			if (! hasSession()) {
				m_session = null;
			}
		}
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
		characterEncoding = encoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		// not to do
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String parameter) {
		if(parameters.containsKey(parameter))
		{
			return parameters.get(parameter).get(0);
		}
		else
		{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String parameter) {
		if(parameters.containsKey(parameter))
		{
			return parameters.get(parameter).toArray(new String[parameters.get(parameter).size()]);
		}
		else
		{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		return parameters;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		return httpRequest.getProtocol()+"/"+httpRequest.getVersion();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		if(httpRequest.getHeaders().containsKey("host"))
		{
			String host = httpRequest.getHeaders().get("host").get(0);
			if(host.contains(":"))
			{
				return host.split(":")[0];
			}
			else
			{
				return host;
			}
		}
		else
		{
			return HttpServer.getDaemonSocket().getInetAddress().getHostAddress();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		if(httpRequest.getHeaders().containsKey("host"))
		{
			String host = httpRequest.getHeaders().get("host").get(0);
			if(host.contains(":"))
			{
				return Integer.valueOf(host.split(":")[1]);
			}
			else
			{
				return HttpServer.getDaemonSocket().getLocalPort();
			}
		}
		else
		{
			return HttpServer.getDaemonSocket().getLocalPort();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		String remote = socket.getRemoteSocketAddress().toString();
		if(remote!=null)
		{
			if(remote.contains("/"))
			{
				remote = remote.substring(remote.indexOf("/")+1);
			}
			if(remote.contains(":"))
			{
				return remote.split(":")[0];
			}
			else
			{
				return remote;
			}
		}
		return remote;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String key) {
		m_props.remove(key);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration getLocales() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		String remote = socket.getRemoteSocketAddress().toString();
		if(remote!=null)
		{
			if(remote.contains("/"))
			{
				remote = remote.substring(remote.indexOf("/")+1);
			}
			if(remote.contains(":"))
			{
				return Integer.valueOf(remote.split(":")[1]);
			}
			else
			{
				return -1;	//error case
			}
		}
		return -1;	//error case
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		return HttpServer.getDaemonSocket().getInetAddress().getHostName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		return HttpServer.getDaemonSocket().getInetAddress().getHostAddress();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		return HttpServer.getDaemonSocket().getLocalPort();
	}

	void setMethod(String method) {
		m_method = method;
	}
	
	void setParameter(String key, String value) {
		if(parameters.containsKey(key))
		{
			parameters.get(key).add(value);
		}
		else
		{
			ArrayList<String> values = new ArrayList<String>();
			values.add(value);
			parameters.put(key, values);
		}
	}
	
	void clearParameters() {
		parameters.clear();
	}
	
	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}

	public Properties getM_props() {
		return m_props;
	}

	public void setM_props(Properties m_props) {
		this.m_props = m_props;
	}

	public Session getM_session() {
		return m_session;
	}

	public void setM_session(Session m_session) {
		this.m_session = m_session;
	}

	public String getM_method() {
		return m_method;
	}

	public void setM_method(String m_method) {
		this.m_method = m_method;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public void setCookies(ArrayList<Cookie> cookies) {
		this.cookies = cookies;
	}

	public void setparameters(HashMap<String, ArrayList<String>> params) {
		this.parameters = params;		
	}
	
	
		
}

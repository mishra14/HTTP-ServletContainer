package edu.upenn.cis.cis455.servlet;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class Handler extends DefaultHandler{

	private int m_state = 0;
	private String m_servletName;
	private String m_paramName;
	HashMap<String,String> m_servlets = new HashMap<String,String>();
	HashMap<String,String> m_contextParams = new HashMap<String,String>();
	HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();
	HashMap<String, String> m_urlPattern = new HashMap<String, String>();
	private String tempServletName;
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		System.out.println("Start element "+m_state);
		if (qName.compareTo("servlet-name") == 0) {
			m_state = (m_state == 5) ? 15 : 1;
		} else if (qName.compareTo("servlet-class") == 0) {
			m_state = 2;
		} else if (qName.compareTo("context-param") == 0) {
			m_state = 3;
		} else if (qName.compareTo("init-param") == 0) {
			m_state = 4;
		} else if (qName.compareTo("param-name") == 0) {
			m_state = (m_state == 3) ? 10 : 20;
		} else if (qName.compareTo("param-value") == 0) {
			m_state = (m_state == 10) ? 11 : 21;
		} else if(qName.compareTo("servlet-mapping") == 0) {
			m_state = 5;
		} else if(qName.compareTo("url-pattern") == 0) {
			m_state = (m_state == 15) ? 25 : 35;
		} else if(qName.compareTo("display-name") == 0) {
			m_state = 6;
		}
		System.out.println("Start element "+qName+" "+m_state);
	}
	public void characters(char[] ch, int start, int length) {
		String value = new String(ch, start, length);
		if (m_state == 1) {
			m_servletName = value;
			m_state = 0;
		} else if (m_state == 2) {
			m_servlets.put(m_servletName, value);
			m_state = 0;
		} else if (m_state == 6) {
			m_contextParams.put("display-name", value);
			m_state = 0;
		} else if (m_state == 10 || m_state == 20) {
			m_paramName = value;
		} else if (m_state == 11) {
			if (m_paramName == null) {
				System.err.println("Context parameter value '" + value + "' without name");
				System.exit(-1);
			}
			m_contextParams.put(m_paramName, value);
			m_paramName = null;
			m_state = 0;
		} else if (m_state == 21) {
			if (m_paramName == null) {
				System.err.println("Servlet parameter value '" + value + "' without name");
				System.exit(-1);
			}
			HashMap<String,String> p = m_servletParams.get(m_servletName);
			if (p == null) {
				p = new HashMap<String,String>();
				m_servletParams.put(m_servletName, p);
			}
			p.put(m_paramName, value);
			m_paramName = null;
			m_state = 0;
		} else if(m_state == 15) {
			tempServletName = value;
		} else if(m_state == 25) {
			if(!value.startsWith("/"))
			{
				value = "/"+value;
			}
			if(value.contains("/*"))
			{
				value=value.replace("*", ".*");
				m_urlPattern.put(value, tempServletName);
				value=value.replace("/.*", "");
				m_urlPattern.put(value, tempServletName);
			}
			else
			{
				m_urlPattern.put(value, tempServletName);
			}
			m_state = 0;
		} else if(m_state == 35) {
			m_state=0;
		}
		System.out.println("character");
	}
	public int getM_state() {
		return m_state;
	}
	public void setM_state(int m_state) {
		this.m_state = m_state;
	}
	public String getM_servletName() {
		return m_servletName;
	}
	public void setM_servletName(String m_servletName) {
		this.m_servletName = m_servletName;
	}
	public String getM_paramName() {
		return m_paramName;
	}
	public void setM_paramName(String m_paramName) {
		this.m_paramName = m_paramName;
	}
	public HashMap<String, String> getM_servlets() {
		return m_servlets;
	}
	public void setM_servlets(HashMap<String, String> m_servlets) {
		this.m_servlets = m_servlets;
	}
	public HashMap<String, String> getM_contextParams() {
		return m_contextParams;
	}
	public void setM_contextParams(HashMap<String, String> m_contextParams) {
		this.m_contextParams = m_contextParams;
	}
	public HashMap<String, HashMap<String, String>> getM_servletParams() {
		return m_servletParams;
	}
	public void setM_servletParams(
			HashMap<String, HashMap<String, String>> m_servletParams) {
		this.m_servletParams = m_servletParams;
	}	
	public HashMap<String, String> getM_urlPattern() {
		return m_urlPattern;
	}
	@Override
	public String toString() {
		return "Handler [m_state=" + m_state + ", m_servletName="
				+ m_servletName + ", m_paramName=" + m_paramName
				+ ", m_servlets=" + m_servlets + ", m_contextParams="
				+ m_contextParams + ", m_servletParams=" + m_servletParams
				+ ", m_urlPattern=" + m_urlPattern + ", tempServletName="
				+ tempServletName + "]";
	}
	
	
}

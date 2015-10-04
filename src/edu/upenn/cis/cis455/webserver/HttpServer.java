package edu.upenn.cis.cis455.webserver;


import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.queue.Queue;
import edu.upenn.cis.cis455.servlet.Context;
import edu.upenn.cis.cis455.servlet.Handler;
import edu.upenn.cis.cis455.servlet.Config;
import edu.upenn.cis.cis455.servlet.Session;
import edu.upenn.cis.cis455.thread.DaemonThread;

public class HttpServer {

	private static final Logger logger = Logger.getLogger(HttpServer.class);
	private static final int ARGS_LENGTH=3;
	private static final int THREAD_POOL_SIZE=100;
	private static int port;
	private static String homeDirectory;
	private static String webXmlPath;
	private static File webXml;
	private static Queue requestQueue;
	private static DaemonThread daemonThread;
	private static ServerSocket daemonSocket;
	private static HashMap<String, String> urlPatterns;
	private static HashMap<String,HttpServlet> servlets;
	private static HashMap<String, Session> sessions;
	
public static void main(String[] args) {
		
		if(args.length !=ARGS_LENGTH)
		{
			logger.warn("Invalid number of arguments\nAnkit Mishra\nmankit");
			System.exit(1);
		}
		try
		{
			port=Integer.valueOf(args[0]);
			homeDirectory=args[1].trim();
			if(!(new File(homeDirectory).exists()))
			{
				logger.warn("Invalid home directory = "+homeDirectory+"\nAnkit Mishra\nmankit");
				System.exit(1);
			}
			webXmlPath=args[1].trim();
			webXml=new File(webXmlPath);
			if(!(webXml.exists()))
			{
				logger.warn("Invalid web xml directory = "+homeDirectory+"\nAnkit Mishra\nmankit");
				System.exit(1);
			}
		}
		catch(NumberFormatException e)
		{
			logger.warn("Invalid port number\nAnkit Mishra\nmankit");
			System.exit(1);
		}
		
		
		if(homeDirectory.endsWith("/"))
		{
			homeDirectory=new String(homeDirectory.substring(0,homeDirectory.length()-1));
		}		
		
		Handler handler;
		try {
			handler = parseWebdotxml(args[2]);
			Context context = createContext(handler);
			HashMap<String,HttpServlet> servlets = createServlets(handler, context);
			urlPatterns = new HashMap<String, String>(handler.getM_urlPattern());
			sessions = new HashMap<String, Session>();
			logger.info(servlets.toString());
			logger.info(handler.getM_servletParams().toString());
			logger.info(handler.getM_servlets());
			logger.info(handler.toString());
			logger.info(urlPatterns);
			logger.info(sessions);
		} catch (Exception e) {
			logger.error("Exception while parsing web xml", e);
		}
		try 
		{
			daemonSocket= new ServerSocket(port);
			requestQueue = new Queue();
			daemonThread = new DaemonThread(requestQueue, THREAD_POOL_SIZE, homeDirectory, port, daemonSocket);
			daemonThread.start();	//start daemon thread that starts the thread pool
			while(daemonThread.getThreadPool().isRun())
			{	
				logger.info("Main thread waiting on daemonSocket.accept");
				Socket socket=daemonSocket.accept();
				logger.info("Main thread finished waiting on daemonSocket.accept");
				requestQueue.enqueue(socket);
			} 
			logger.warn("Main Thread waiting for Daemon thread to shut down");
			daemonSocket.close();
			daemonThread.join();
		}
		catch (IOException e){
			if(daemonThread.getThreadPool().isRun())
			{
				logger.error("IO exception while opening serversocket",e);
			}
		} 
		catch (InterruptedException e) {
			logger.error("Exception while joining daemon thread",e);
		}
		logger.warn("Main Thread Shutting down");
		System.exit(0);
	}
	
	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler handler = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, handler);
		return handler;
	}
	
	private static Context createContext(Handler h) {
		Context context = new Context();
		for (String param : h.getM_contextParams().keySet()) {
			context.setInitParam(param, h.getM_contextParams().get(param));
		}
		return context;
	}
	private static HashMap<String,HttpServlet> createServlets(Handler handler, Context context) throws Exception {
		servlets = new HashMap<String,HttpServlet>();
		for (String servletName : handler.getM_servlets().keySet()) {
			Config config = new Config(servletName, context);
			String className = handler.getM_servlets().get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = handler.getM_servletParams().get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static int getArgsLength() {
		return ARGS_LENGTH;
	}

	public static int getThreadPoolSize() {
		return THREAD_POOL_SIZE;
	}

	public static int getPort() {
		return port;
	}

	public static String getHomeDirectory() {
		return homeDirectory;
	}

	public static String getWebXmlPath() {
		return webXmlPath;
	}

	public static File getWebXml() {
		return webXml;
	}

	public static Queue getRequestQueue() {
		return requestQueue;
	}

	public static DaemonThread getDaemonThread() {
		return daemonThread;
	}

	public static ServerSocket getDaemonSocket() {
		return daemonSocket;
	}

	public static HashMap<String, String> getUrlPatterns() {
		return urlPatterns;
	}

	public static HashMap<String, HttpServlet> getServlets() {
		return servlets;
	}

	public static HashMap<String, Session> getSessions() {
		return sessions;
	}
	
	
}
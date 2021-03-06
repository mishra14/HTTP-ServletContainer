package edu.upenn.cis.cis455.webserver;


import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;

import edu.upenn.cis.cis455.queue.Queue;
import edu.upenn.cis.cis455.servlet.Context;
import edu.upenn.cis.cis455.servlet.Handler;
import edu.upenn.cis.cis455.servlet.Config;
import edu.upenn.cis.cis455.servlet.Session;
import edu.upenn.cis.cis455.thread.DaemonThread;

/**
 * Main class to start servlet container and server
 * 
 * @author cis455
 *
 */
public class HttpServer {

	private static final Logger logger = Logger.getLogger(HttpServer.class);
	private static final int ARGS_LENGTH=3;
	private static final int THREAD_POOL_SIZE=100;
	private static final long SESSION_VALIDATION_PERIOD = 5000;
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
	private static Timer timer;
	private static Context context;	
	private static WriterAppender appender;
	private static StringWriter writer;
	
	/**
	 * Timer task to invalidate sessions
	 */
	private static TimerTask validateSessions = new TimerTask() {
		@Override
		public void run() 
		{
			synchronized(sessions)
			{
				//logger.info("Validating Sessions");
				Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator();
				while(iterator.hasNext())
				{
					Map.Entry<String, Session> sessionEntry = iterator.next();
					Session session = sessionEntry.getValue();
					Date current = new Date();
					
					if((session.getMaxInactiveInterval()!=-1) && (current.getTime() - session.getLastAccessedTime())>(session.getMaxInactiveInterval()*1000))
					{
						logger.info("Session id - "+session.getId()+" has expired - Invalidating and removing it fromt he session map");
						session.invalidate();
						iterator.remove();
					}
				}
			}
		}
	};
	
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
		//create servlets
		//create context
		//parse web xml
		Handler handler;
		try {
			handler = parseWebdotxml(args[2]);
			context = createContext(handler);
			HashMap<String,HttpServlet> servlets = createServlets(handler, context);
			urlPatterns = new HashMap<String, String>(handler.getM_urlPattern());
			sessions = new HashMap<String, Session>();
			logger.info(servlets.toString());
			logger.info(handler.getM_servletParams());
			logger.info(handler.getM_servlets());
			logger.info(handler.getM_contextParams());
			logger.info(handler.toString());
			logger.info(context.getServletContextName());
			logger.info("Starting session validator thread");
			writer = new StringWriter();
			appender = new WriterAppender(new HTMLLayout(), writer);
			appender.setName("CONTROL_PAGE_APPENDER");
			appender.setThreshold(org.apache.log4j.Level.ERROR);
			Logger.getRootLogger().addAppender(appender);
			timer = new Timer();
			timer.schedule(validateSessions, 0, SESSION_VALIDATION_PERIOD);
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
			timer.cancel();
		}
		catch (IOException e)
		{
			if(daemonThread.getThreadPool().isRun())
			{
				logger.error("IO exception while opening serversocket",e);
			}
		} 
		catch (InterruptedException e) 
		{
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

	public static Context getContext() {
		return context;
	}

	public static WriterAppender getAppender() {
		return appender;
	}

	public static StringWriter getWriter() {
		return writer;
	}
	
	
}
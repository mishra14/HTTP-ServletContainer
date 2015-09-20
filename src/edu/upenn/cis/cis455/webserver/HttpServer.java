package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.queue.Queue;
import edu.upenn.cis.cis455.thread.DaemonThread;

public class HttpServer {

	private static final Logger logger = Logger.getLogger(HttpServer.class);
	private static final int ARGS_LENGTH=2;
	private static final int QUEUE_LENGTH=50;
	private static final int THREAD_POOL_SIZE=10;
	private static int port;
	private static String homeDirectory;
	private static Queue requestQueue;
	private static DaemonThread daemonThread;
	
	public static void main(String[] args) {
		
		if(args.length !=ARGS_LENGTH)
		{
			logger.warn("Invalid number of arguments\nAnkit Mishra\nmankit");
			System.exit(1);
		}
		int i=0;
		port=Integer.valueOf(args[0]);
		homeDirectory=args[1].trim();
		if(homeDirectory.endsWith("/"))
		{
			homeDirectory=new String(homeDirectory.substring(0,homeDirectory.length()-1));
		}
		requestQueue = new Queue(QUEUE_LENGTH);
		daemonThread = new DaemonThread(requestQueue,THREAD_POOL_SIZE, homeDirectory,port);
		daemonThread.start();	//start daemon thread that starts the thread pool
		ServerSocket daemonSocket = null;
		try 
		{
			daemonSocket= new ServerSocket(port);
			while(true)
			{	
				Socket socket=daemonSocket.accept();
				if(requestQueue.enqueue(socket)==0)
				{
					//thread pool and the request queue are full
					//send a 500 error message
					logger.warn("Queue is also full; handle this");
				}
				else
				{
					logger.info("Entered request - " +(++i) );
				}
			} 
		}
		catch (IOException e){
				logger.error("IO exception while opening serversocket",e);
		}
	}
	
	

}
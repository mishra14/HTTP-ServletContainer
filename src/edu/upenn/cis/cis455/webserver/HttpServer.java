package edu.upenn.cis.cis455.webserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.queue.Queue;
import edu.upenn.cis.cis455.thread.DaemonThread;

public class HttpServer {

	private static final Logger logger = Logger.getLogger(HttpServer.class);
	private static final int ARGS_LENGTH=2;
	private static final int THREAD_POOL_SIZE=100;
	private static int port;
	private static String homeDirectory;
	private static Queue requestQueue;
	private static DaemonThread daemonThread;
	private static ServerSocket daemonSocket;
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
				logger.warn("Invalid home directory\nAnkit Mishra\nmankit");
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
}
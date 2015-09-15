package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.thread.ServerThread;
import edu.upenn.cis.cis455.thread.ThreadPool;

public class HttpServer {

	private static final Logger logger = Logger.getLogger(HttpServer.class);
	private static final int ARGS_LENGTH=2;
	private static int port;
	private static String homeDirectory;
	private static ThreadPool threadPool = new ThreadPool(100,"ThreadPool");
	
	public static void main(String[] args) {
		
		if(args.length !=ARGS_LENGTH)
		{
			System.out.println("Invalid number of arguments\nAnkit Mishra\nmankit");
			System.exit(1);
		}
		
		threadPool.startThreadPool();
		port=Integer.valueOf(args[0]);
		homeDirectory=args[1];
		ServerSocket daemonSocket = null;
		try 
		{
			daemonSocket= new ServerSocket(port);
			while(true)
			{	
				Socket socket=daemonSocket.accept();
				if(threadPool.getThreadPool().size()>0)
				{
					threadPool.displayPool();
					ServerThread serverThread=threadPool.getThreadPool().remove(0);
					threadPool.displayPool();
					synchronized(serverThread)
					{
						serverThread.setSocket(socket);
						serverThread.notify();
					}
				}
				else	//TODO no threads free
				{
					//send 500 to client
					System.out.println("No threads free; handle this");
				}
			} 
			
		}
		catch (IOException e){
				logger.error("IO exception while opening serversocket",e);
		}
	}
	
	

}
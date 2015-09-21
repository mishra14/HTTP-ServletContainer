package edu.upenn.cis.cis455.thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.queue.Queue;

public class DaemonThread extends Thread{

	private static final Logger logger = Logger.getLogger(DaemonThread.class);
	private Queue requestQueue;
	private ThreadPool threadPool;
	private ServerSocket daemonSocket;
	
	public DaemonThread(Queue requestQueue, int threadPoolSize, String homeDirectory, int port, ServerSocket daemonSocket) {
		this.requestQueue = requestQueue;
		this.threadPool = new ThreadPool(threadPoolSize,"ThreadPool", homeDirectory, port, this);
		this.daemonSocket = daemonSocket;
	}
	
	public ThreadPool getThreadPool() {
		return threadPool;
	}

	public void run()
	{
		threadPool.startThreadPool();
		threadPool.displayPool();
		ServerThread serverThread;
		Socket socket;
		while(threadPool.isRun())
		{
			socket=null;
			while(threadPool.isRun() && socket==null)
			{
				synchronized(requestQueue)
				{
					if(requestQueue.getSize()>0)
					{
						socket = requestQueue.dequeue();
						logger.info("Removed request");
					}
					else
					{
						try 
						{
							logger.warn("Daemon thread sleeping on request queue");
							requestQueue.wait();
							logger.warn("Daemon thread waking on request queue");
						} 
						catch (InterruptedException e) 
						{
							if(threadPool.isRun())
							{
								logger.error("Exception while waiting for request queue",e);
							}
						}
					}
				}
			}
			serverThread=null;
			while(threadPool.isRun() && serverThread==null)
			{
				synchronized(threadPool)
				{
					if(threadPool.getThreadPool().size()>0)
					{
						//logger.info("Before - ");
						//threadPool.displayPool();
						serverThread=threadPool.getThreadPool().remove(0);
						//logger.info("After - ");
						//threadPool.displayPool();
						synchronized(serverThread)
						{
							//logger.info("Passing request to thread "+serverThread.getId());
							serverThread.setSocket(socket);
							serverThread.notify();
						}
					}
					else	//no threads free; wait for threads to be free
					{
						try 
						{
							logger.info("Daemon thread sleeping on thread pool");
							threadPool.wait();
							logger.info("Daemon thread waking on thread pool");
						} 
						catch (InterruptedException e) 
						{
							if(threadPool.isRun())
							{
								logger.error("Exception while waiting for thread pool",e);
							}
						}
					}
				}
			}
		}
		logger.warn("Daemon thread killing the request queue to stop the main thread");
		try {
			daemonSocket.close();
		} 
		catch (NullPointerException e) {
			logger.error("Exception while closing the daemon socket - The server will shut down after accepting 1 new request",e);
		}
		catch (IOException e) {
			logger.error("Exception while closing the daemon socket - The server will shut down after accepting 1 new request",e);
		}
		logger.warn("Daemon Thread killing free threads from the thread pool");
		for(ServerThread thread : threadPool.getThreadPool())
		{
			thread.interrupt();
		}
		logger.warn("Daemon Thread waiting for thread pool to shut down");
		for(ServerThread thread : threadPool.getThreadList())
		{
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error("Exception while joining thread pool threads",e);
			}
		}
		logger.warn("Daemon Thread Shutting down");
	}
}

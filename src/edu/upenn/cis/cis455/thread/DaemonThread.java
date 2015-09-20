package edu.upenn.cis.cis455.thread;

import java.net.Socket;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.queue.Queue;

public class DaemonThread extends Thread{

	private static final Logger logger = Logger.getLogger(DaemonThread.class);
	private Queue requestQueue;
	private ThreadPool threadPool;
	
	public DaemonThread(Queue requestQueue, int threadPoolSize, String homeDirectory, int port) {
		this.requestQueue = requestQueue;
		this.threadPool = new ThreadPool(threadPoolSize,"ThreadPool", homeDirectory, port);
	}
	
	public void run()
	{
		threadPool.startThreadPool();
		threadPool.displayPool();
		ServerThread serverThread;
		Socket socket;
		while(true)
		{
			socket=null;
			while(socket==null)
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
							logger.info("Daemon thread sleeping on request queue");
							requestQueue.wait();
							logger.info("Daemon thread waking on request queue");
						} 
						catch (InterruptedException e) 
						{
							logger.error("Exception while waiting for request queue",e);
						}
					}
				}
			}
			serverThread=null;
			while(serverThread==null)
			{
				synchronized(threadPool)
				{
					if(threadPool.getThreadPool().size()>0)
					{
						System.out.println("Before - ");
						threadPool.displayPool();
						serverThread=threadPool.getThreadPool().remove(0);
						System.out.println("After - ");
						threadPool.displayPool();
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
							logger.error("Exception while waiting for request queue",e);
						}
					}
				}
			}
			
		}
	}
}

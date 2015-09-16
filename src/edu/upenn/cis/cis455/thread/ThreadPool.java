package edu.upenn.cis.cis455.thread;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class ThreadPool {
	
	private static final Logger logger= Logger.getLogger(ThreadPool.class);
	private String threadPoolName;
	private ArrayList<ServerThread> threadPool;
	private int threadCount;
	private String homeDirectory;
	
	public ThreadPool(int threadCount, String threadPoolName, String homeDirectory) {
		super();
		this.threadPool = new ArrayList<ServerThread>();
		this.threadCount = threadCount;
		this.threadPoolName = threadPoolName;
		this.homeDirectory = homeDirectory;
	}
	
	public synchronized ArrayList<ServerThread> getThreadPool() {
		return threadPool;
	}

	public synchronized void setThreadPool(ArrayList<ServerThread> threadPool) {
		this.threadPool = threadPool;
	}

	public synchronized int getThreadCount() {
		return threadCount;
	}

	public synchronized void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	
	public void startThreadPool()
	{
		for(int i=0;i<threadCount;i++)
		{
			threadPool.add(new ServerThread(i,this, homeDirectory));
		}
		System.out.println("Thread pool has "+threadPool.size()+" free threads");
		for(ServerThread thread : threadPool)
		{
			thread.start(); 
		}
	}
	public void displayPool()
	{
		logger.info("Name - "+threadPoolName);
		logger.info("Size - "+threadPool.size());
		/*for(ServerThread serverThread:threadPool)
		{
			System.out.println(serverThread.getId());
		}*/
	}
	public void clearThreadPool()
	{
		//TODO stop all runing threads and delete them
	}
}

package edu.upenn.cis.cis455.thread;

import java.util.ArrayList;

public class ThreadPool {
	
	private String threadPoolName;
	private ArrayList<ServerThread> threadPool;
	private int threadCount;
	
	public ThreadPool(int threadCount, String threadPoolName) {
		super();
		this.threadPool = new ArrayList<ServerThread>();
		this.threadCount = threadCount;
		this.threadPoolName = threadPoolName;
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
			threadPool.add(new ServerThread(i,this));
		}
		System.out.println("Thread pool has "+threadPool.size()+" free threads");
		for(ServerThread thread : threadPool)
		{
			thread.start(); 
		}
	}
	public void displayPool()
	{
		System.out.println("Name - "+threadPoolName);
		System.out.println("Size - "+threadPool.size());
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

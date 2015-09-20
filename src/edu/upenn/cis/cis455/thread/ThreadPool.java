package edu.upenn.cis.cis455.thread;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class ThreadPool {
	
	private static final Logger logger= Logger.getLogger(ThreadPool.class);
	private String threadPoolName;
	private ArrayList<ServerThread> threadPool;
	private ArrayList<ServerThread> threadList;
	private int threadCount;
	private String homeDirectory;
	private int port;
	private boolean run;
	private DaemonThread daemonThread;
	
	public ThreadPool(int threadCount, String threadPoolName, String homeDirectory, int port, DaemonThread daemonThread) {
		super();
		this.threadPool = new ArrayList<ServerThread>();
		this.threadList = new ArrayList<ServerThread>();
		this.threadCount = threadCount;
		this.threadPoolName = threadPoolName;
		this.homeDirectory = homeDirectory;
		this.port=port;
		this.run=true;
		this.daemonThread = daemonThread;
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

	public String getThreadPoolName() {
		return threadPoolName;
	}

	public void setThreadPoolName(String threadPoolName) {
		this.threadPoolName = threadPoolName;
	}

	public ArrayList<ServerThread> getThreadList() {
		return threadList;
	}

	public String getHomeDirectory() {
		return homeDirectory;
	}
	
	public int getPort() {
		return port;
	}

	
	public synchronized boolean isRun() {
		return run;
	}

	public synchronized void setRun(boolean run) {
		this.run = run;
	}

	
	public DaemonThread getDaemonThread() {
		return daemonThread;
	}

	public void startThreadPool()
	{
		for(int i=0;i<threadCount;i++)
		{
			threadPool.add(new ServerThread(i,this, homeDirectory));
			threadList.add(threadPool.get(i));
		}
		logger.info("Thread pool has "+threadPool.size()+" free threads");
		for(ServerThread thread : threadPool)
		{
			thread.start(); 
		}
	}
	public void displayPool()
	{
		logger.info("Name - "+threadPoolName);
		logger.info("Free Size - "+threadPool.size());
		/*for(ServerThread serverThread:threadPool)
		{
			logger.info(serverThread.getId());
		}*/
		logger.info("Total Size - "+threadList.size());
		/*for(ServerThread serverThread:threadList)
		{
			logger.info(serverThread.getId());
		}*/
	}
	
}

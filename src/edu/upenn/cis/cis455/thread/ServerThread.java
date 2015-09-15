package edu.upenn.cis.cis455.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

public class ServerThread extends Thread {
	
	private static final Logger logger = Logger.getLogger(ServerThread.class);
	private ThreadPool parentThreadPool;
	private int id;
	private Socket socket;
	public ServerThread(int id, ThreadPool parentThreadPool)
	{
		this.id=id;
		this.socket=new Socket();
		this.parentThreadPool=parentThreadPool;
	}
	
	
	public long getId() {
		return id;
	}


	public Socket getSocket() {
		return socket;
	}


	public void setSocket(Socket socket) {
		this.socket = socket;
	}


	public void run()
	{
		///System.out.println("Starting thread "+id);
		while(true)
		{
			try 
			{
				synchronized(this)
				{
					//System.out.println("Waiting for request - "+id);
					wait();
					System.out.println("obtained request - "+id);
					if(socket!=null)	//correct transfer of socket to handler thread
					{
						String inputData, outputData;
						PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						while((inputData=in.readLine()) != null)
						{
							System.out.println(inputData);
						}
					}
					else 	//error in socket log it and retry
					{
						System.out.println("Error in getting socket; trying again - "+id);
					}
					if(!parentThreadPool.getThreadPool().contains(this))
					{
						parentThreadPool.getThreadPool().add(this);
						System.out.println("Thread "+id+" going back into threadpool");
					}
				}
			} catch (InterruptedException e) {
				System.out.println("Interrupted Exception while waiting on socket "+e);
			} catch (IOException e) {
				System.out.println("IOException while reading from socket "+e);
			}
		}
	}

}
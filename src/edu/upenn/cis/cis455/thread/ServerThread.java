package edu.upenn.cis.cis455.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.http.HttpRequest;

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
						HttpRequest httpRequest=parseRequest(in);
						if(httpRequest==null)
						{
							//TODO respond with error code 400
						}
						else if(!httpRequest.isValidRequest())
						{
							//TODO respond with error code 400
						}
						else
						{
							System.out.println(httpRequest);
						}
					}
					else 	//error in socket log it and retry
					{
						logger.warn("Error in getting socket; trying again - "+id);
					}
					if(!parentThreadPool.getThreadPool().contains(this))
					{
						parentThreadPool.getThreadPool().add(this);
						System.out.println("Thread "+id+" going back into threadpool");
					}
				}
			} catch (InterruptedException e) {
				logger.error("Interrupted Exception while waiting on socket ",e);
			} catch (IOException e) {
				logger.error("IOException while reading from socket ",e);
			}
		}
	}

	private HttpRequest parseRequest(BufferedReader in) throws IOException {

		String inLineString;
		StringBuilder requestString=null;
		HttpRequest httpRequest=null;
		while(!((inLineString=in.readLine()).equals("")))
		{
			if(requestString==null)	//the request was valid
			{
				requestString = new StringBuilder();
			}
			requestString.append(inLineString+"\n");
		}
		if(requestString!=null)	//the request was valid
		{
			httpRequest=new HttpRequest(requestString.toString());
		}
		
		return httpRequest;
	}

}
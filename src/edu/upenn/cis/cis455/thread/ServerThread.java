package edu.upenn.cis.cis455.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.http.HTTP;
import edu.upenn.cis.cis455.http.HttpRequest;
import edu.upenn.cis.cis455.http.HttpResponse;

public class ServerThread extends Thread {

	private static final Logger logger = Logger.getLogger(ServerThread.class);
	private static final String CONTENT_TYPE_KEY="Content-type";
	private static final String CONTENT_LENGTH_KEY="Content-Length";
	private static final String LAST_MODIFIED_KEY="Last-Modified";
	private static final String DATE_KEY="Date";
	private static final String CONNECTION_KEY="Connection";
	private ThreadPool parentThreadPool;
	private int id;
	private Socket socket;
	private String homeDirectory;
	public ServerThread(int id, ThreadPool parentThreadPool,
			String homeDirectory) {
		this.id = id;
		this.socket = new Socket();
		this.parentThreadPool = parentThreadPool;
		this.homeDirectory = homeDirectory;
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
					System.out.println("Waiting for request - "+id);
					wait();
					System.out.println("obtained request - "+id);
					if(socket!=null)	//correct transfer of socket to handler thread
					{
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
							String data="<html><body>It works!</body></html>";
							System.out.println(httpRequest);
							logger.info("Requesting - "+homeDirectory.concat(httpRequest.getResource()));
							File resourceFile=new File(homeDirectory.concat(httpRequest.getResource()));
							if(resourceFile.exists())
							{
								//valid file request
								if(resourceFile.isDirectory())
								{
									//send the list of all the files inside
									File[] filesInDirectory = resourceFile.listFiles();
									StringBuilder dataBuilder=new StringBuilder();
									dataBuilder.append("<html><body>"+httpRequest.getResource()+"<br/>");
									for(File file : filesInDirectory)
									{
										dataBuilder.append("<a href=\"localhost:8080/"+file.getName()+"\">"+file.getName()+"</a><br/>");
									}
									dataBuilder.append("</body></html>");
									data=dataBuilder.toString();
								}
								else if(resourceFile.isFile())
								{
									//send the file
								}
								else
								{
									//send some error code
								}
							}
							else
							{
								logger.info("requested file does not exist");
								//invalid request
							}
							Map<String, String> headers=new HashMap<String, String>();
							headers.put(DATE_KEY, new Date().toString());
							headers.put(CONTENT_TYPE_KEY,"text/html; charset=utf-8");
							headers.put(CONTENT_LENGTH_KEY,""+data.length());
							headers.put(CONNECTION_KEY,"Close");
							String protocol = HTTP.getProtocol();
							String version = HTTP.getVersion11();
							String responseCode = "200";
							String responseCodeString = HTTP.getResponseCodes().get(responseCode);
							HttpResponse httpResponse = new HttpResponse(protocol, version, responseCode, responseCodeString, headers, data);
							logger.info(httpResponse.toString());
							logger.info(httpResponse.getResponseString());
							out.println(httpResponse.getResponseString());
							//generate response header
							//generate response data
							//send response
						}
					}
					else 	//error in socket log it and retry
					{
						logger.warn("Error in getting socket; trying again - "+id);
					}
					synchronized(parentThreadPool)
					{
						if(!parentThreadPool.getThreadPool().contains(this))
						{
							parentThreadPool.getThreadPool().add(this);
							parentThreadPool.notify();
							System.out.println("Thread "+id+" going back into threadpool");
						}
					}
					
				}
			} catch (InterruptedException e) {
				logger.error("Interrupted Exception while waiting on socket ",e);
			} catch (IOException e) {
				logger.error("IOException while reading from socket ",e);
			} catch (NullPointerException e) {
				logger.error("NullPointerException while reading from socket ",e);
			}
			finally
			{
				synchronized(parentThreadPool)
				{
					if(!parentThreadPool.getThreadPool().contains(this))
					{
						parentThreadPool.getThreadPool().add(this);
						parentThreadPool.notify();
						System.out.println("Thread going back into threadpool "+id);
					}
				}
			}
		}
	}

	private HttpRequest parseRequest(BufferedReader in) throws IOException {

		String inLineString;
		StringBuilder requestString = null;
		HttpRequest httpRequest = null;
		while (in != null && !((inLineString = in.readLine()).equals(""))) {
			if (requestString == null) {
				requestString = new StringBuilder();
			}
			requestString.append(inLineString + "\n");
		}
		if (requestString != null) // the request was valid
		{
			httpRequest = new HttpRequest(requestString.toString());
		}

		return httpRequest;
	}

}
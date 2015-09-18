package edu.upenn.cis.cis455.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
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
						OutputStream out=socket.getOutputStream();
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						HttpRequest httpRequest=parseRequest(in);
						if(httpRequest==null)
						{
							// respond with error code 400
							out.write(HTTP.getError400().getResponseString().getBytes());
						}
						else if(!httpRequest.isValidRequest())
						{
							// respond with error code 400
							out.write(HTTP.getError400().getResponseString().getBytes());
						}
						else
						{
							Map<String, String> headers=new HashMap<String, String>();
							String data = "";
							String protocol = HTTP.getProtocol();
							String version = HTTP.getVersion11();
							String responseCode = "200";
							String responseCodeString = HTTP.getResponseCodes().get(responseCode);
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
									logger.info(homeDirectory);
									for(File file : filesInDirectory)
									{
										if(!file.getName().endsWith("~"))
											dataBuilder.append("<a href=\"http://localhost:8080"+file.getAbsolutePath().substring(homeDirectory.length())+"\">"+file.getName()+"</a><br/>");
									}
									dataBuilder.append("</body></html>");
									data=dataBuilder.toString();
									headers.put(DATE_KEY, new Date().toString());
									headers.put(CONTENT_TYPE_KEY,"text/html; charset=utf-8");
									headers.put(CONTENT_LENGTH_KEY,""+data.length());
									headers.put(CONNECTION_KEY,"Close");
									HttpResponse httpResponse = new HttpResponse(protocol, version, responseCode, responseCodeString, headers, data);
									if(httpRequest.getOperation().equalsIgnoreCase("GET"))
									{
										out.write(httpResponse.getResponseString().getBytes());
									}
									else if(httpRequest.getOperation().equalsIgnoreCase("HEAD"))
									{
										out.write(httpResponse.getResponseStringHeadersOnly().getBytes());
									}
									else if(httpRequest.getOperation().equalsIgnoreCase("POST"))
									{
										out.write(HTTP.getErrorPOST().getResponseString().getBytes());
									}
									else
									{
										out.write(HTTP.getError400().getResponseString().getBytes());
									}
									logger.info(httpResponse.toString());
									logger.info(httpResponse.getResponseString());

									
								}
								else if(resourceFile.isFile())
								{
									//send the file
									FileInputStream fis = new FileInputStream(resourceFile);
									byte[] bytes = new byte[(int) resourceFile.length()];
									if(fis.read(bytes, 0, bytes.length)!=resourceFile.length())
									{
										//did not read the file completely; send back an error code 500
										logger.warn("Length error while reading file - "+resourceFile.getAbsolutePath());
										out.write(HTTP.getError500().getResponseString().getBytes());
										
									}
									else
									{
										//read the file correctly; send the file over
										if(Files.probeContentType(resourceFile.toPath())==null)
										{
											//no content type; send an error code 500
											logger.warn("Error in detecting file type on file- "+resourceFile.getAbsolutePath());
											out.write(HTTP.getError500().getResponseString().getBytes());
										}
										else
										{
											data=new String(bytes);
											logger.info(bytes);
											headers.put(CONTENT_TYPE_KEY,Files.probeContentType(resourceFile.toPath())+"; charset=utf-8");
											headers.put(CONTENT_LENGTH_KEY,""+data.length());
											headers.put(DATE_KEY, new Date().toString());
											HttpResponse httpResponse = new HttpResponse(protocol, version, responseCode, responseCodeString, headers, data);
											//logger.info(httpResponse.toString());
											//logger.info(httpResponse.getResponseString());
											if(httpRequest.getOperation().equalsIgnoreCase("GET"))
											{
												out.write(httpResponse.getResponseStringHeadersOnly().getBytes());
												out.write(bytes);
											}
											else if(httpRequest.getOperation().equalsIgnoreCase("HEAD"))
											{
												out.write(httpResponse.getResponseStringHeadersOnly().getBytes());
											}
											else if(httpRequest.getOperation().equalsIgnoreCase("POST"))
											{
												out.write(HTTP.getErrorPOST().getResponseString().getBytes());
											}
											else
											{
												out.write(HTTP.getError400().getResponseString().getBytes());
											}
											logger.info(bytes.toString());
										}
									}
									fis.close();
								}
								else
								{
									logger.warn("requested file is neither a file nor a directory - "+resourceFile.getAbsolutePath());
									//requested data is neither file nor directory; send 400 error code
									out.write(HTTP.getError400().getResponseString().getBytes());
								}
							}
							else
							{
								logger.warn("requested file does not exist - "+resourceFile.getAbsolutePath());
								//invalid request; respond with 404
								out.write(HTTP.getError404().getResponseString().getBytes());
							}
							
							out.flush();
							out.close();
							//generate response header
							//generate response data
							//send response
						}
						socket.close();
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

		logger.info("parsing request");
		String inLineString="";
		StringBuilder requestString = null;
		HttpRequest httpRequest = null;
		while (inLineString != null && in != null && !((inLineString = in.readLine()).equals(""))) {
			if (requestString == null) {
				requestString = new StringBuilder();
				logger.info("string builder created");
			}
			requestString.append(inLineString + "\n");
			logger.info(inLineString);
		}
		if (requestString != null) // the request was valid
		{
			httpRequest = new HttpRequest(requestString.toString());
		}

		return httpRequest;
	}
	
}
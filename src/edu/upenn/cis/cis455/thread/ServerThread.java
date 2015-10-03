package edu.upenn.cis.cis455.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.http.HTTP;
import edu.upenn.cis.cis455.http.HttpRequest;
import edu.upenn.cis.cis455.http.HttpResponse;
import edu.upenn.cis.cis455.servlet.Request;
import edu.upenn.cis.cis455.servlet.Response;
import edu.upenn.cis.cis455.webserver.HttpServer;

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
	private HttpResponse httpResponse;
	private HttpRequest httpRequest;
	
	public ServerThread(int id, ThreadPool parentThreadPool,
			String homeDirectory) {
		this.id = id;
		this.socket = new Socket();
		this.parentThreadPool = parentThreadPool;
		this.homeDirectory = homeDirectory;
		this.httpRequest=null;
		this.httpResponse=null;
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
	
	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public void run()
	{
		///logger.info("Starting thread "+id);
		while(parentThreadPool.isRun())
		{
			try 
			{
				synchronized(this)
				{
					logger.info("Waiting for request - "+id);
					wait();
					logger.info("obtained request - "+id);
					//parentThreadPool.displayPool();
					if(socket!=null)	//correct transfer of socket to handler thread
					{
						OutputStream out=socket.getOutputStream();
						InputStream socketInputStream = socket.getInputStream();
						InputStreamReader inputStreamReader = new InputStreamReader(socketInputStream);
						BufferedReader in = new BufferedReader(inputStreamReader);
						httpRequest=parseRequest(in);
						if(httpRequest==null)
						{
							logger.info("null Http Request ");
							// respond with error code 400
							out.write(HTTP.getError400().getResponseString().getBytes());
						}
						else if(!httpRequest.isValidRequest())
						{
							logger.info("Invalid http request - "+httpRequest.getResource());
							// respond with error code 400
							out.write(HTTP.getError400().getResponseString().getBytes());
						}
						else
						{
							//deal with an Expect header
							if(httpRequest.getHeaders().containsKey("expect"))
							{
								//respond with a 100 error code
								out.write(HTTP.getError100().getResponseString().getBytes());
							}
							//dead with absolute urls
							if(httpRequest.getResource().contains("http://localhost:"+parentThreadPool.getPort()))
							{
								logger.info("Absolute url in GET request - "+httpRequest.getResource());
								httpRequest.setResource(httpRequest.getResource().substring(("http://localhost:"+parentThreadPool.getPort()).length()));
							}
							if(HttpServer.getUrlPatterns()!=null)
							{
								int longestLength=0;
								String longestMatch="";
								for(Map.Entry<String, String> entry : HttpServer.getUrlPatterns().entrySet())
								{
									System.out.println("Pattern - "+entry.getKey());
									System.out.println("Resource - "+httpRequest.getResource());
									Pattern urlPattern = Pattern.compile(entry.getKey());
									if(urlPattern.matcher(httpRequest.getResource()).matches())
									{
										int length = entry.getKey().contains("*")?entry.getKey().indexOf("*")-1:entry.getKey().length();
										if(length>longestLength)
										{
											longestLength = length;
											longestMatch = entry.getKey();
										}
									}
								}

								//send request to servlet 
								if(HttpServer.getServlets().containsKey(longestMatch))
								{
									System.out.println("Matching - "+HttpServer.getServlets().get(longestMatch));
									httpRequest.setServletUrl(longestMatch);
									httpRequest.updatePaths();
									Request request = new Request(httpRequest);
									Response response = new Response(new HttpResponse());
									request.setSocket(socket);
									response.setSocket(socket);
									logger.info("Client socket - "+request.getRemoteAddr());
									HttpServer.getServlets().get(longestMatch).service(request, response);
									continue;
								}
								
							}
							Map<String, ArrayList<String>> headers=new HashMap<String, ArrayList<String>>();
							ArrayList<String> values;
							String data = "";
							String protocol = HTTP.getProtocol();
							String version = HTTP.getVersion11();
							String responseCode = "200";
							String responseCodeString = HTTP.getResponseCodes().get(responseCode);
							logger.info(httpRequest);
							logger.info("Requesting - "+homeDirectory.concat(httpRequest.getResource()));
							File resourceFile=new File(homeDirectory.concat(httpRequest.getResource()));
							if(resourceFile.exists())
							{
								if(!resourceFile.canRead()) //403 if the file is not readable
								{
									logger.warn("User accessing non readable file - "+resourceFile.getAbsolutePath());
									out.write(HTTP.getError403().getResponseString().getBytes());
								}
								//valid file request
								else if(resourceFile.isDirectory())
								{
									//send the list of all the files inside
									File[] filesInDirectory = resourceFile.listFiles();
									StringBuilder dataBuilder=new StringBuilder();
									dataBuilder.append("<html><body>"+httpRequest.getResource()+"<br/>");
									logger.info(homeDirectory);
									for(File file : filesInDirectory)
									{
										if(!file.getName().endsWith("~"))
											dataBuilder.append("<a href=\"http://localhost:"+parentThreadPool.getPort()+httpRequest.getResource()+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");
									}
									dataBuilder.append("</body></html>");
									data=dataBuilder.toString();
									values = new ArrayList<String>();
									values.add(HTTP.getHttpDateFormat().format(new GregorianCalendar().getTime()));
									headers.put(DATE_KEY, values);											
									values = new ArrayList<String>();
									values.add("text/html; charset=utf-8");
									headers.put(CONTENT_TYPE_KEY, values);
									values = new ArrayList<String>();
									values.add(""+data.length());
									headers.put(CONTENT_LENGTH_KEY, values);
									values = new ArrayList<String>();
									values.add("Close");
									headers.put(CONNECTION_KEY, values);
									httpResponse = new HttpResponse(protocol, version, responseCode, responseCodeString, headers, data);
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
										out.write(HTTP.getError405().getResponseString().getBytes());
									}
									else
									{
										logger.info("Unknown operation in request - "+httpRequest.getResource());
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
											if(httpRequest.getHeaders().containsKey("if-modified-since") || httpRequest.getHeaders().containsKey("if-unmodified-since"))
											{
												logger.info("if-modified or if-unmodified header detected");
												Calendar ifModifiedDate = new GregorianCalendar();
												try {
													ifModifiedDate.setTime(httpRequest.getHeaders().containsKey("if-modified-since")?HTTP.getHttpDateFormat().parse(httpRequest.getHeaders().get("if-modified-since").get(0)):HTTP.getHttpDateFormat().parse(httpRequest.getHeaders().get("if-unmodified-since").get(0)));
												} catch (ParseException e) {
													logger.error("ParseException while parsing if-modified-date  ",e);
													out.write(HTTP.getError500().getResponseString().getBytes());
												}
												Calendar fileModifiedDate=new GregorianCalendar();
												fileModifiedDate.setTimeInMillis(resourceFile.lastModified());
												logger.info("File modified Date - "+HTTP.getHttpDateFormat().format(fileModifiedDate.getTime()));
												logger.info("If modified Date - "+HTTP.getHttpDateFormat().format(ifModifiedDate.getTime()));
												if(fileModifiedDate.after(ifModifiedDate))
												{
													logger.info("File modified since if modified date");
												}
												else
												{
													logger.info("File not modified since if modified date");
												}
												if(!fileModifiedDate.after(ifModifiedDate) && httpRequest.getHeaders().containsKey("if-modified-since"))
												{
													logger.info("Requesting a non modified file through if-modified - "+resourceFile.getAbsolutePath());
													//send a 304 error code
													out.write(HTTP.getError304().getResponseString().getBytes());
												}
												else if(fileModifiedDate.after(ifModifiedDate) && httpRequest.getHeaders().containsKey("if-unmodified-since"))
												{
													logger.info("Requesting a modified file through if-unmodified - "+resourceFile.getAbsolutePath());
													//send a 412 error code
													out.write(HTTP.getError412().getResponseString().getBytes());
												}
												else
												{
													data=new String(bytes);
													logger.info(bytes);
													values = new ArrayList<String>();
													values.add(Files.probeContentType(resourceFile.toPath())+"; charset=utf-8");
													headers.put(CONTENT_TYPE_KEY, values);
													values = new ArrayList<String>();
													values.add(""+data.length());
													headers.put(CONTENT_LENGTH_KEY,values);
													values = new ArrayList<String>();
													values.add(HTTP.getHttpDateFormat().format(new GregorianCalendar().getTime()));
													headers.put(DATE_KEY, values);													
													httpResponse = new HttpResponse(protocol, version, responseCode, responseCodeString, headers, data);
													
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
														out.write(HTTP.getError405().getResponseString().getBytes());
													}
													else
													{
														logger.info("Unknown operation in request - "+httpRequest.getResource());
														out.write(HTTP.getError400().getResponseString().getBytes());
													}
													logger.info(bytes.toString());
												}
											}
											else
											{
												data=new String(bytes);
												logger.info(bytes);
												values = new ArrayList<String>();
												values.add(Files.probeContentType(resourceFile.toPath())+"; charset=utf-8");
												headers.put(CONTENT_TYPE_KEY, values);
												values = new ArrayList<String>();
												values.add(""+data.length());
												headers.put(CONTENT_LENGTH_KEY,values);
												values = new ArrayList<String>();
												values.add(HTTP.getHttpDateFormat().format(new GregorianCalendar().getTime()));
												headers.put(DATE_KEY, values);	
												httpResponse = new HttpResponse(protocol, version, responseCode, responseCodeString, headers, data);
												//logger.info(httpResponse.toString());
												//logger.info(httpResponse.getResponseString());
												if(httpRequest.getOperation().equalsIgnoreCase("GET"))
												{
													out.write(httpResponse.getResponseStringHeadersOnly().getBytes());
													out.flush();
													out.write(bytes);
												}
												else if(httpRequest.getOperation().equalsIgnoreCase("HEAD"))
												{
													out.write(httpResponse.getResponseStringHeadersOnly().getBytes());
												}
												else if(httpRequest.getOperation().equalsIgnoreCase("POST"))
												{
													out.write(HTTP.getError405().getResponseString().getBytes());
												}
												else
												{
													logger.info("Unknown operation in request - "+httpRequest.getResource());
													out.write(HTTP.getError400().getResponseString().getBytes());
												}
												logger.info(bytes.toString());
											}

											
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
								
								if(httpRequest.getResource().equalsIgnoreCase("/control"))
								{
									StringBuilder dataBuilder=new StringBuilder();
									dataBuilder.append("<html><body>"+httpRequest.getResource()+"<br/>Ankit Mishra<br/>mankit<br/><br/>");
									dataBuilder.append("ThreadPool Stats - <br/>Total Threads = "+parentThreadPool.getThreadList().size()+"<br/>");
									dataBuilder.append("Free Threads = "+parentThreadPool.getThreadPool().size()+"<br/>");
									dataBuilder.append("Busy Threads = "+(parentThreadPool.getThreadList().size()-parentThreadPool.getThreadPool().size())+"<br/>");
									dataBuilder.append("<a href=\"http://localhost:"+parentThreadPool.getPort()+"/shutdown\">Shut Down Server</a><br/>");
									dataBuilder.append("<br/>List of all threads - <br/>");
									for(ServerThread thread : parentThreadPool.getThreadList())
									{
										dataBuilder.append(thread.id+" "+thread.getState());
										logger.info(thread.id+" "+thread.getState());
										if(thread.getState().equals(Thread.State.RUNNABLE))
										{
											int threadPoolPort = parentThreadPool.getPort();
											String threadResource = (thread.getHttpRequest()!=null)?thread.getHttpRequest().getResource():"";
											String url = "http://localhost:"+threadPoolPort+threadResource;
											dataBuilder.append(" <a href=\""+url+"\">"+url+"</a>");
										}
										dataBuilder.append("<br/>");
									}
									dataBuilder.append("</body></html>");
									data=dataBuilder.toString();
									values = new ArrayList<String>();
									values.add(Files.probeContentType(resourceFile.toPath())+"; charset=utf-8");
									headers.put(CONTENT_TYPE_KEY, values);
									values = new ArrayList<String>();
									values.add(""+data.length());
									headers.put(CONTENT_LENGTH_KEY,values);
									values = new ArrayList<String>();
									values.add(HTTP.getHttpDateFormat().format(new GregorianCalendar().getTime()));
									headers.put(DATE_KEY, values);	
									values = new ArrayList<String>();
									values.add("Close");
									headers.put(CONNECTION_KEY,values);
									httpResponse = new HttpResponse(protocol, version, responseCode, responseCodeString, headers, data);
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
										out.write(HTTP.getError405().getResponseString().getBytes());
									}
									else
									{
										logger.info("Unknown operation in request - "+httpRequest.getResource());
										out.write(HTTP.getError400().getResponseString().getBytes());
									}
									logger.info(httpResponse.toString());
									logger.info(httpResponse.getResponseString());

								}
								else if(httpRequest.getResource().equalsIgnoreCase("/shutdown"))
								{
									//indicate to all threads that a shut down has been initiated
									parentThreadPool.setRun(false);
									//interrupt daemon and main threads
									parentThreadPool.getDaemonThread().interrupt();
									StringBuilder dataBuilder=new StringBuilder();
									dataBuilder.append("<html><body>"+httpRequest.getResource()+"<br/>Ankit Mishra<br/>mankit<br/><br/>");
									dataBuilder.append("This page has started the server shutdown <br/>");
									dataBuilder.append("</body></html>");
									data=dataBuilder.toString();
									values = new ArrayList<String>();
									values.add(Files.probeContentType(resourceFile.toPath())+"; charset=utf-8");
									headers.put(CONTENT_TYPE_KEY, values);
									values = new ArrayList<String>();
									values.add(""+data.length());
									headers.put(CONTENT_LENGTH_KEY,values);
									values = new ArrayList<String>();
									values.add(HTTP.getHttpDateFormat().format(new GregorianCalendar().getTime()));
									headers.put(DATE_KEY, values);	
									values = new ArrayList<String>();
									values.add("Close");
									headers.put(CONNECTION_KEY,values);
									httpResponse = new HttpResponse(protocol, version, responseCode, responseCodeString, headers, data);
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
										out.write(HTTP.getError405().getResponseString().getBytes());
									}
									else
									{
										logger.info("Unknown operation in request - "+httpRequest.getResource());
										out.write(HTTP.getError400().getResponseString().getBytes());
									}
									logger.info(httpResponse.toString());
									logger.info(httpResponse.getResponseString());
								}
								else
								{
									logger.warn("requested file does not exist - "+resourceFile.getAbsolutePath());
									//invalid request; respond with 404
									out.write(HTTP.getError404().getResponseString().getBytes());
								}
							}
						}
						in.close();
						inputStreamReader.close();
						socketInputStream.close();
						out.flush();
						out.close();		//close the output buffer
						httpRequest=null;	//clear the http request object
						httpResponse=null;	//clear the http response object
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
							logger.info("Thread "+id+" going back into threadpool");
						}
					}
					
				}
			} catch (InterruptedException e) {
				if(parentThreadPool.isRun()) {
					logger.error("Interrupted Exception while waiting on socket ",e);
				}
			} catch (IOException e) {
				logger.error("IOException while reading from socket ",e);
			} catch (NullPointerException e) {
				logger.error("NullPointerException while reading from socket ",e);
			} catch (ServletException e) {
				logger.error("ServletException while sending request to servlet ",e);
			}
			finally
			{
				synchronized(parentThreadPool)
				{
					if(!parentThreadPool.getThreadPool().contains(this))
					{
						parentThreadPool.getThreadPool().add(this);
						parentThreadPool.notify();
						logger.info("Thread going back into threadpool "+id);
					}
				}
			}
		}
		logger.warn("Thread Shutting down - "+id);
	}

	private HttpRequest parseRequest(BufferedReader in) throws IOException, NullPointerException {

		logger.info("parsing request");
		String inLineString="";
		StringBuilder requestString = null;
		HttpRequest httpRequest = null;
		while (((inLineString = in.readLine())!=null && !inLineString.equals(""))) {
			if (requestString == null) {
				requestString = new StringBuilder();
				logger.info("string builder created");
			}
			requestString.append(inLineString + "\n");
			logger.warn(inLineString);
		}
		if (requestString != null) // the request was valid
		{
			httpRequest = new HttpRequest(requestString.toString());
		}

		return httpRequest;
	}
	
}
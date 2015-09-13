package edu.upenn.cis.cis455.webserver;

import java.util.HashSet;
import java.util.Set;

import edu.upenn.cis.cis455.thread.ServerThread;

public class HttpServer {

	private static final int ARGS_LENGTH=2;
	private static Set<ServerThread> threadPool = new HashSet<ServerThread>();
	
	public static void main(String[] args) {
		
		if(args.length !=ARGS_LENGTH)
		{
			System.out.println("Invalid arguments\nAnkit Mishra\nmankit");
			System.exit(1);
		}
		
		for(int i=0;i<100;i++)
		{
			threadPool.add(new ServerThread(i));
		}
		System.out.println("Thread pool has "+threadPool.size()+" free threads");
		for(ServerThread thread : threadPool)
		{
			thread.start(); 
		}
	}

}
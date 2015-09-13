package edu.upenn.cis.cis455.thread;

public class ServerThread extends Thread {
	
	private int id;
	
	public ServerThread(int id)
	{
		this.id=id;
	}
	public void run()
	{
		System.out.println("Hello - "+id);
	}

}
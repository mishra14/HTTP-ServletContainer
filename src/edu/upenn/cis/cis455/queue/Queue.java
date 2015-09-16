package edu.upenn.cis.cis455.queue;

import java.net.Socket;
import java.util.ArrayList;

public class Queue 
{
	private ArrayList<Socket> queue;
	private int limit;
	
	public Queue(int limit)
	{
		queue=new ArrayList<Socket>();
		this.limit=limit;
	}
	public ArrayList<Socket> getQueue() {
		return queue;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public synchronized int getSize()
	{
		return queue.size();
	}
	public synchronized int enqueue(Socket socket)
	{
		if(queue.size()>=limit)
		{
			return 0;		//not adding element; return 0 error code
		}
		queue.add(queue.size(), socket);		//add element to the end of the array list
		this.notify();
		return 1;				//adding element successfully
	}
	public synchronized Socket dequeue()
	{
		return queue.remove(0);					//remove element from the beginning of the array list
	}
	
}

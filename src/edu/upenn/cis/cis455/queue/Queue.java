package edu.upenn.cis.cis455.queue;

import java.net.Socket;
import java.util.ArrayList;

/**
 * que class
 * @author cis455
 *
 */
public class Queue 
{
	private ArrayList<Socket> queue;
	
	public Queue()
	{
		queue=new ArrayList<Socket>();
	}
	public ArrayList<Socket> getQueue() {
		return queue;
	}
	public synchronized int getSize()
	{
		return queue.size();
	}
	public synchronized void enqueue(Socket socket)
	{
		queue.add(queue.size(), socket);		//add element to the end of the array list
		this.notify();
	}
	public synchronized Socket dequeue()
	{
		return queue.remove(0);					//remove element from the beginning of the array list
	}
	
}

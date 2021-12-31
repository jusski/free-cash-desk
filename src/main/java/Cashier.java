import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Data;

public class Cashier implements Runnable
{
	final int MAX_QUEUE_LENGTH = 10;

	@Data
	private static class Node
	{
		Visitor item;
		Node next;
		volatile int position;

		Node(Visitor item)
		{
			this.item = item;
		}
	}

	private final AtomicInteger count = new AtomicInteger();
	private final int capacity = MAX_QUEUE_LENGTH;

	private Node head;
	private Node tail;
	
	public Cashier()
	{
		head = tail = new Node(null);
		head.next = tail;
	}

	private final ReentrantLock lock = new ReentrantLock();
	private final Condition notEmpty = lock.newCondition();

	public Node put(Visitor visitor)
	{
		final AtomicInteger count = this.count;
		if (count.get() == capacity) return null;

		Node node = new Node(visitor);
		lock.lock();
		try
		{
			if (count.get() < capacity)
			{
				tail = tail.next = node; 
				node.position = count.getAndIncrement(); 
				if (node.position == 0) notEmpty.signal(); 
			}
		}
		finally
		{
			lock.unlock();
		}
		
		return node;
	}

	public Visitor take()
	{
		final AtomicInteger count = this.count;
		if (count.get() == 0) return null;
		Visitor visitor;
		lock.lock();
		try
		{
			if(count.get() == 0)
			{
				return null;
			}
			System.out.println(head);
			Node first = head;
			head = head.next;
			first.next = null;
			visitor = head.item;
			head.item = null;
			count.getAndDecrement();
			
			Node it = head.next;
			int counter = 0;
			while(it != null)
			{
				it.position = counter++;
				it = it.next;
				System.out.println(it);
			}
			
		}
		finally
		{
			lock.unlock();
		}
		
		return visitor;
	}

	public void remove(Visitor visitor)
	{
		lock.lock();
		try
		{
			Node it = head.next;
			Node previous = head;
			while(it != null)
			{
				if(it.item == visitor) // reference equals should be ok.
				{
					previous.next = it.next;
					it.item = null;
					it.next = it;
					if(it == tail) tail = previous;
					count.getAndDecrement();
					
					it = previous.next;
					while(it != null)
					{
						it.position -= 1;
						it = it.next;
					}
					break;
				}
				previous = it;
				it = it.next;
			}
			
		}
		finally
		{
			lock.unlock();
		}
        
	}

	@Override
	public void run()
	{
		Visitor visitor;
		while (true)
		{
			while ((visitor = take()) != null)
			{
				visitor.offerFood();
			}
			lock.lock();
			try
			{
				notEmpty.await();
			}
			catch (InterruptedException e)
			{
				ExceptionUtils.rethrowUnchecked(e);
			}
			finally
			{
				lock.unlock();
			}
		}

	}
}

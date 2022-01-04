import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class Cashier implements Runnable
{
	static final int MAX_QUEUE_LENGTH = 10;

	public ArrayList<Visitor> queue = new ArrayList<>(MAX_QUEUE_LENGTH);

	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	
	public ReadLock readLock = lock.readLock();
	public WriteLock writeLock = lock.writeLock();
	
	public final Condition notEmpty = writeLock.newCondition();
	public final Condition notFull = writeLock.newCondition();
	
	
	public int getQueueLength()
	{
		try
		{
			readLock.lock();
			return queue.size();
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public int indexOf(Visitor visitor)
	{
		try
		{
			readLock.lock();
			return queue.indexOf(visitor);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public void add(Visitor visitor)
	{
		try
		{
			writeLock.lock();
			while(queue.size() > MAX_QUEUE_LENGTH)
			{
				notFull.awaitUninterruptibly();
			}
			queue.add(visitor);
			notEmpty.signal();
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public boolean tryAdd(Visitor visitor)
	{
		boolean result = false;
		if (writeLock.tryLock())
		{
			if (queue.size() < MAX_QUEUE_LENGTH)
			{
				queue.add(visitor);
				notEmpty.signal();
				result = true;
			}
			writeLock.unlock();
		}

		return result;
	}
	
	public Visitor remove()
	{
		try
		{
			writeLock.lock();
			while(queue.size() == 0)
			{
				notEmpty.awaitUninterruptibly();
			}
			Visitor visitor = queue.remove(0);
			notFull.signal();
			return visitor;
		}
		finally
		{
			writeLock.unlock();
		}
        
	}

	public boolean remove(Visitor visitor)
	{
		try
		{
			writeLock.lock();
			int index = queue.indexOf(visitor);
			if(index != -1)
			{
				queue.remove(index);
				notFull.signal();
				return true;
			}
		}
		finally
		{
			writeLock.unlock();
		}

		return false;
	}

	@Override
	public void run()
	{
		while(true)
		{
			Visitor visitor = remove();
			visitor.served = true;
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				ExceptionUtils.rethrowUnchecked(e);
			}
		}

	}
}

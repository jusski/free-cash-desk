import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NotEmptyConditionTest
{

	
	public static void main(String[] args) throws InterruptedException
	{
		ReentrantLock lock = new ReentrantLock();
		Condition notEmpty = lock.newCondition();
		System.out.println("Program started.");
		new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				lock.lock();
				notEmpty.signal();
				lock.unlock();
				
			}
		}).start();
		
		Thread.sleep(200);
		new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				lock.lock();
				try
				{
					try
					{
						notEmpty.await(10, TimeUnit.SECONDS);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("notempty got signal");
				}
				finally
				{
					lock.unlock();
				}
				
			}
		}).start();
		
		
		

	}

}

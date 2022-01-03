import java.util.Comparator;

public class Visitor implements Runnable
{
	public Cashier cashier;
	public volatile boolean served = false;

	public void enterFastFoodRestaurant() throws InterruptedException
	{
		while (true)
		{
			cashier = FastFoodRestaurant.getCashiers().stream()
					.min(Comparator.comparingInt(Cashier::getQueueLength))
					.get();
			if(cashier.tryAdd(this)) break;
			Thread.sleep(10);
		}

	}

	public void run()
	{
		try
		{
			enterFastFoodRestaurant();
			while (served == false)
			{
				Thread.sleep(100);

			   Cashier cashierWithMinimalQueue = FastFoodRestaurant.getCashiers().stream()
						.min(Comparator.comparingInt(Cashier::getQueueLength))
						.get();
				if (cashier.indexOf(this) > cashierWithMinimalQueue.getQueueLength())
				{
					try
					{
						cashier.writeLock.lock();
						if (cashierWithMinimalQueue.writeLock.tryLock())
						{
							if(cashier.indexOf(this) > cashierWithMinimalQueue.getQueueLength())
							cashier.remove(this);

							cashierWithMinimalQueue.add(this);
							cashierWithMinimalQueue.writeLock.unlock();
						}

					}
					finally
					{
						cashier.writeLock.unlock();
					}

				}
			}
		}
		catch (InterruptedException e)
		{
			ExceptionUtils.rethrowUnchecked(e);
		}

	}
}

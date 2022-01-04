import java.util.ArrayList;

import cash.ConsoleUtils;
import lombok.val;

public class Main
{
	static class DebugPrinter implements Runnable
	{

		@Override
		public void run()
		{
			ConsoleUtils.cls();

			ArrayList<Cashier> cashiers = FastFoodRestaurant.getCashiers();
			while (true)
			{
				for (int i = 0; i < cashiers.size(); ++i)
				{
					Cashier cashier = cashiers.get(i);
					int queueLength = cashier.queue.size();
					ConsoleUtils.printf(0, i, String.format("%5d", queueLength));
					ConsoleUtils.printf(10, i, String.format("%5d", cashier.lock.getQueueLength()));
					ConsoleUtils.printf(20, i, String.format("%5d", cashier.lock.getReadHoldCount()));
					ConsoleUtils.printf(30, i, String.format("%5d", cashier.lock.getReadLockCount()));
				}
				try
				{
					Thread.sleep(33);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	public static void main(String[] args) throws InterruptedException
	{

		new Thread(new DebugPrinter()).start();
		Thread.sleep(2000);
		for (int i = 0; i < 1000; ++i)
		{
			val visitor = new Visitor();
			new Thread(visitor, "visitor " + i).start();
		}

	}
}

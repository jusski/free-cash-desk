import java.util.ArrayList;
import java.util.Arrays;

import cash.ConsoleUtils;
import cash.ConsoleUtils.Table;
import lombok.val;

public class Main
{
	static class DebugPrinter implements Runnable
	{

		@Override
		public void run()
		{
			ConsoleUtils.cls();
			
			while (true)
			{
				// for (int i = 0; i < cashiers.size(); ++i)
				// {
				// Cashier cashier = cashiers.get(i);
				// int queueLength = cashier.queue.size();
				// ConsoleUtils.println(0, i, String.format("%5d",
				// queueLength));
				// ConsoleUtils.println(10, i, String.format("%5d",
				// cashier.lock.getQueueLength()));
				// ConsoleUtils.println(20, i, String.format("%5d",
				// cashier.lock.getReadHoldCount()));
				// ConsoleUtils.println(30, i, String.format("%5d",
				// cashier.lock.getReadLockCount()));
				ArrayList<Cashier> cashiers = FastFoodRestaurant.getCashiers();
			
				Table table = ConsoleUtils.Table.createTable("              Queue");
				
				int row = 1;
				for (Cashier cashier : cashiers)
				{
					int size = cashier.getQueueLength();
					char[] stars = new char[size];
					Arrays.fill(stars, '*');
					table.printColumn(row++, 0, new String(stars));
				}
				try
				{
					Thread.sleep(100);
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
		for (int i = 0; i < 50; ++i)
		{
			val visitor = new Visitor();
			new Thread(visitor, "visitor " + i).start();
		}

	}
}

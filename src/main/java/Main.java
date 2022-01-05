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
			ConsoleUtils.attachConsole();
			
			while (true)
			{
				ArrayList<Cashier> cashiers = FastFoodRestaurant.getCashiers();
			
				Table table = ConsoleUtils.Table.createTable("Q", "R", "Queue");
				int row = 1;
				for (Cashier cashier : cashiers)
				{
					int size = cashier.getQueueLength();
					char[] stars = new char[size];
					Arrays.fill(stars, '*');
					table.printColumn(row, 0, cashier.lock.getQueueLength());
					table.printColumn(row, 1, cashier.lock.getReadHoldCount());
					table.printColumn(row, 2, new String(stars));
					row += 1;
				}
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

		}

	}

	public static void main(String[] args) throws InterruptedException
	{

		new Thread(new DebugPrinter()).start();
		Thread.sleep(2000);
		for (int i = 0; i < 500; ++i)
		{
			val visitor = new Visitor();
			new Thread(visitor, "visitor " + i).start();
		}

	}
}

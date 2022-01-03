package cash;

import java.util.Arrays;
import java.util.Collection;

import lombok.val;

public class Main {
    public static void main (String[] args) throws InterruptedException {
    	new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				 ConsoleUtils.cls();
			        Collection<Cashier> cashiers = FastFoodRestaurant.getCashiers();
			        while(true)
			        {
			        	int counter = 0;
			        	for(Cashier cashier : cashiers)
			        	{
			        		counter += 1;
			        		int queueLength = cashier.getQueueLength();
			        		char[] stars = new char[queueLength];
			        		Arrays.fill(stars, '*');
			        		ConsoleUtils.printf(0, counter, new String(stars));
			        		
			        		
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
		}).start();
    	
        for (int i = 0; i < 500; ++i) {
            val visitor = new Customer(i);
            new Thread(visitor).start();
        }
        ((Cashier)FastFoodRestaurant.getCashiers().toArray()[2]).speed = 100;
        
       
    }
}

import java.util.ArrayList;

import lombok.val;

public class Main {
    public static void main (String[] args) {
        for (int i = 0; i < 5; ++i) {
            val visitor = new Visitor();
            new Thread(visitor).start();
        }
        ConsoleUtils.cls();
        
        ArrayList<Cashier> cashiers = FastFoodRestaurant.getCashiers();
        while(true)
        {
        	for(int i = 0; i < cashiers.size(); ++i)
        	{
        		Cashier cashier = cashiers.get(i);
        		int queueLength = cashier.getQueueLength();
        		ConsoleUtils.printf(0, i, String.format("Cashier length = %5d", queueLength));
        	}
        }
    }
}

import java.util.Collection;

import lombok.val;

public class Main {
    public static void main (String[] args) {
        for (int i = 0; i < 3; ++i) {
            val visitor = new Visitor();
            new Thread(visitor).start();
        }
        
        FastFoodRestaurant restaurant = new FastFoodRestaurant();
        Collection<Cashier> cashiers = restaurant.getCashiers();
        while(true)
        {
        	for(int i = 0; i < cashiers.size(); ++i)
        }
    }
}

package cash;

import lombok.val;

public class Main {
    public static void main (String[] args) {
        for (int i = 0; i < 5000; ++i) {
            val visitor = new Customer(i);
            new Thread(visitor).start();
        }
        ((Cashier)FastFoodRestaurant.getCashiers().toArray()[2]).speed = 100;
    }
}

package cash;

import java.util.*;

public class FastFoodRestaurant {
    private static final int           NUMBER_OF_CASHIERS = 2;
    private static final int           MAX_QUEUE_LENGTH   = 5;
    private static final List<Cashier> cashiers;
    public static final  Random        random             = new Random(123);

    static {
        cashiers = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CASHIERS; ++i) {
            Cashier cashier = new Cashier(i, 1 + random.nextFloat(), MAX_QUEUE_LENGTH);
            new Thread(cashier).start();
            cashiers.add(cashier);
        }
        cashiers.get(1).speed = 100;
    }

    public static Collection<Cashier> getCashiers () {
        return Collections.unmodifiableCollection(cashiers);
    }
}

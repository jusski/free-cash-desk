import lombok.val;

import java.util.*;

public class FastFoodRestaurant {
    private static final int           NUMBER_OF_CASHIERS = 4;
    private static final List<Cashier> cashiers;

    static {
        cashiers = new ArrayList<>();
        val random = new Random();
        for (int i = 0; i < NUMBER_OF_CASHIERS; ++i) {
            Cashier cashier = new Cashier(i, 1 + random.nextFloat());
            new Thread(cashier).start();
            cashiers.add(cashier);
        }
    }

    public static Collection<Cashier> getCashiers () {
        return Collections.unmodifiableCollection(cashiers);
    }
}

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FastFoodRestaurant {
    private static final int           NUMBER_OF_CASHIERS = 4;
    private static final ArrayList<Cashier> cashiers;
    public static final ReentrantLock restaurantLock = new ReentrantLock();
    public static final Condition queueChanged = restaurantLock.newCondition();

    static {
        cashiers = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CASHIERS; ++i) {
            Cashier cashier = new Cashier();
            new Thread(cashier).start();
            cashiers.add(cashier);
        }
    }

    public static Collection<Cashier> getCashiers () {
        return Collections.unmodifiableCollection(cashiers);
    }
}

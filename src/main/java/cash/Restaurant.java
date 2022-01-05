package cash;

import lombok.var;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Restaurant {
    private static final int           NUMBER_OF_CASHIERS = 5;
    private static final int           MAX_QUEUE_LENGTH   = 20;
    private static final List<Cashier> cashiers;
    public static final  Random        random             = new Random(123);
    public static final  Semaphore     semaphore;
    public static final  AtomicInteger nServed            = new AtomicInteger();

    static {
        var capacity = 0;
        cashiers = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CASHIERS; ++i) {
            Cashier cashier = new Cashier(i, 1 + random.nextFloat(), MAX_QUEUE_LENGTH);
            new Thread(cashier).start();
            cashiers.add(cashier);
            capacity += MAX_QUEUE_LENGTH;
        }
        cashiers.get(1).speed = 100;
        semaphore             = new Semaphore(capacity);
    }

    public static Collection<Cashier> getCashiers () {
        return Collections.unmodifiableCollection(cashiers);
    }
}

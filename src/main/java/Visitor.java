import lombok.val;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Visitor implements Runnable {
    private          Collection<Cashier> cashiers;
    public           Cashier             cashier;
    private volatile boolean             served = false;

    public       int           id;
    public final AtomicInteger lastPositionInQueue = new AtomicInteger(Integer.MAX_VALUE);
    public final AtomicInteger positionInQueue     = new AtomicInteger(Integer.MAX_VALUE);

    Visitor (int id) {
        this.id = id;
    }

    public boolean standInQueue () throws ExceptionUtils {
        val cashier = cashiers.stream()
                .min(Comparator.comparingLong(Cashier::getQueueLength))
                .filter(e -> e.getQueueLength() < positionInQueue.get())
                .orElseThrow(ExceptionUtils::new);
        return cashier.enterQueue(this);
    }

    public void enterFastFoodRestaurant () {
        cashiers = FastFoodRestaurant.getCashiers();
    }

    public void offerFood () {
        try {
            Thread.sleep((long) (new Random().nextInt(1000) / cashier.speed));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        served = true;
        System.out.printf("[%2d %3d] Food is offered, %d\n", cashier.id, id, Thread.currentThread().getId());
    }

    public void run () {
        System.out.printf("[   %3d]+ visitor enter\n", id);
        enterFastFoodRestaurant();
        try {
            while (!standInQueue()) {
                Thread.sleep(1000);
            }
            while (!served) {
                Thread.sleep(10); // thread sleep?
                Cashier cashierWithSmallestQueueLength = cashiers.stream()
                        .min(Comparator.comparingInt(Cashier::getQueueLength))
                        .filter(e -> e.getQueueLength() < positionInQueue.get())
                        .orElse(cashier);
                if (cashier != cashierWithSmallestQueueLength) {
                    System.out.printf("[%2d %3d] cashier with the smallest queue: %d (len = %d)\n",
                            cashier.id, id, cashierWithSmallestQueueLength.id, cashierWithSmallestQueueLength.getQueueLength());
                    cashier.remove(this);
                    final boolean b = cashierWithSmallestQueueLength.enterQueue(this);
                }
            }
        } catch (InterruptedException | ExceptionUtils e) {
            ExceptionUtils.rethrowUnchecked(e);
        }
        System.out.printf("[   %3d]- visitor leave\n", id);
    }
}

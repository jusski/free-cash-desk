package cash;

import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Comparator;

@Log4j2
public class Customer implements Runnable {
    private         Collection<Cashier> cashiers;
    public          Cashier             cashier;
    public volatile boolean             served             = false;
    public volatile Node                node               = null;
    public          long                putInQueueTime     = 0;
    public          long                putInLastQueueTime = 0;
    public          long                sleep              = 10;

    public int id;

    Customer (int id) {
        this.id = id;
    }

    public void enterFastFoodRestaurant () {
        cashiers = FastFoodRestaurant.getCashiers();
    }

    public long getSpentTimeInQueue () {
        return putInQueueTime == 0 ? 0 : System.currentTimeMillis() - putInQueueTime;
    }

    public long getSpentTimeInLastQueue () {
        return putInLastQueueTime == 0 ? 0 : System.currentTimeMillis() - putInLastQueueTime;
    }

    public void run () {
        log.debug(String.format("[   %3d] +enter", id));
        enterFastFoodRestaurant();
        try {
            while (!served) {
                Thread.sleep(sleep);
                Cashier queue = cashiers.stream()
                        .min(Comparator.comparingInt(Cashier::getQueueLength))
                        .filter(e -> node == null || e.getQueueLength() < node.positionInQueue.get())
                        .orElse(cashier);
                if (cashier != queue) {
                    log.debug(String.format("[%2d %3d] min queue len: %d (sleep: %d)",
                            queue.id, id, queue.getQueueLength(), sleep));
                    if (cashier != null) {
                        cashier.remove(this);
                    }
                    if (queue.put(this) == null) {
                        sleep = Math.min(512, sleep << 1);
                    } else {
                        sleep = 10;
                    }
                }
            }
        } catch (Exception ignored) {}
        log.debug(String.format("[   %3d] -leave", id));
    }
}

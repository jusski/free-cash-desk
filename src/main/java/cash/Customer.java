package cash;

import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Comparator;

@Log4j2
public class Customer implements Runnable {
    private         Collection<Cashier> cashiers;
    public          Cashier             cashier;
    public volatile boolean             served       = false;
    public volatile Cashier.Node        node         = null;
    public          long                totalServiceTime = 0;
    public          long                totalServiceInLastCashier = 0;

    public int id;

    Customer (int id) {
        this.id = id;
    }

    public void enterFastFoodRestaurant () {
        cashiers = FastFoodRestaurant.getCashiers();
    }

    public void run () {
        log.debug(String.format("[   %3d]+ visitor enter", id));
        enterFastFoodRestaurant();
        try {
            long sleep = 10;
            while (!served) {
                Thread.sleep(sleep);
                Cashier queue = cashiers.stream()
                        .min(Comparator.comparingInt(Cashier::getQueueLength))
                        .filter(e -> node == null || e.getQueueLength() < node.positionInQueue.get())
                        .orElse(cashier);
                if (cashier != queue) {
                    log.debug(String.format("[%2d %3d] cashier with the smallest queue: %d (len = %d)",
                            queue.id, id, queue.id,
                            queue.getQueueLength()));
                    if (cashier != null) cashier.remove(this);
                    queue.put(this);
                    sleep = 10;
                } else {
                    sleep = Math.min(1024, sleep << 1);
                }
            }
        } catch (Exception ignored) {}
        log.debug(String.format("[   %3d]- visitor leave", id));
    }
}

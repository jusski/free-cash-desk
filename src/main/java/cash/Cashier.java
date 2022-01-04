package cash;

import lombok.extern.log4j.Log4j2;
import lombok.val;
import lombok.var;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static cash.FastFoodRestaurant.random;

@Log4j2
public class Cashier implements Runnable {


    private final AtomicInteger count    = new AtomicInteger();
    private final Node          head     = new Node(null);
    private final ReentrantLock lock     = new ReentrantLock(true);
    private final Semaphore     semaphore;
    private final Condition     notEmpty = lock.newCondition();

    public int           id;
    public float         speed;
    public int           maxQueueLength;
    public long          maxTimeCustomerSpentInQueue = 0;
    public long          waitingForLockTime          = 0;
    public long          maxWaitingForLockTime       = 0;
    public AtomicInteger nServed                     = new AtomicInteger();


    public Cashier (int id, float speed, int maxQueueLength) {
        this.id             = id;
        this.speed          = speed;
        this.maxQueueLength = maxQueueLength;
        semaphore           = new Semaphore(maxQueueLength);
        head.previous       = head.next = head;
    }

    @Override
    public void run () {
        log.debug(String.format("[%2d    ] cashier started", id));
        Customer customer;
        while (true) {
            while ((customer = take()) != null) {
                try {
                    serviceCustomer(customer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                customer.cashier.maxTimeCustomerSpentInQueue
                        = Math.max(customer.cashier.maxTimeCustomerSpentInQueue, customer.getSpentTimeInLastQueue());
            }

            var time = System.currentTimeMillis();
            lock.lock();
            waitingForLockTime    = System.currentTimeMillis() - time;
            maxWaitingForLockTime = Math.max(maxWaitingForLockTime, waitingForLockTime);
            log.debug(String.format("[%2d    ]   lock (run)", id));
            try {
                notEmpty.await(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                ExceptionUtils.rethrowUnchecked(e);
            } finally {
                lock.unlock();
                log.debug(String.format("[%2d    ] unlock (run)", id));
            }
        }
    }

    public int getQueueLength () {
        return count.get();
    }

    public Customer get (int index) {
        if (index >= count.get()) return null;
        int counter = 0;
        var node    = head;
        while ((node = node.next) != head) {
            if (counter++ == index) {
                return node.item;
            }
        }
        return null;
    }

    public Node put (Customer customer) throws InterruptedException {
        if (count.get() >= maxQueueLength) return null;
        log.debug(String.format("[%2d %3d] waiting for semaphore (put)", id, customer.id));
        semaphore.acquire();
        log.debug(String.format("[%2d %3d] waiting for lock (put)", id, customer.id));
        lock.lock();
        log.debug(String.format("[%2d %3d]   lock (put)", id, customer.id));
//        if (customer.served) return null;
        Node node = null;
        try {
            var cnt = count.get();
            if (cnt < maxQueueLength) {
                node          = new Node(customer);
                node.previous = head.previous;
                node.next     = head;

                node.next.previous = node;
                node.previous.next = node;

                node.positionInQueue.set(count.getAndIncrement());
                customer.cashier            = this;
                customer.putInLastQueueTime = System.currentTimeMillis();
                if (customer.putInLastQueueTime == 0) {
                    customer.putInQueueTime = customer.putInLastQueueTime;
                }
                val position     = node.positionInQueue.get();
                val lastPosition = node.lastPositionInQueue.get();
                log.debug(String.format("[%2d %3d] put to position: %d (%d)", id, customer.id, position, lastPosition - position));
                if (cnt == 0) notEmpty.signal();
            }
        } finally {
            lock.unlock();
            log.debug(String.format("[%2d %3d] unlock (put)", id, customer.id));
        }
        return node;
    }

    public Customer take () {
        if (count.get() == 0) return null;
        Customer result;
        log.debug(String.format("[%2d    ] waiting for take next visitor", id));
        var time = System.currentTimeMillis();
        lock.lock();
        waitingForLockTime    = System.currentTimeMillis() - time;
        maxWaitingForLockTime = Math.max(maxWaitingForLockTime, waitingForLockTime);
        log.debug(String.format("[%2d    ]   lock (take)", id));
        try {
            if (count.get() == 0) return null;
            count.getAndDecrement();
            result = dequeue();
            int  counter = 0;
            Node node    = head;
            while ((node = node.next) != head) {
                node.positionInQueue.set(counter++);
                log.debug(String.format("[%2d %3d] changed position to: %d", id, node.item.id, node.positionInQueue.get()));
            }
            semaphore.release();
            log.debug(String.format("[%2d    ] semaphore release (take)", id));
        } finally {
            lock.unlock();
            log.debug(String.format("[%2d    ] unlock (take)", id));
        }
        log.debug(String.format("[%2d %3d] took the next visitor", id, result.id));
        return result;
    }

    private Customer dequeue () {
        Node node = head.next;
        head.next          = node.next;
        head.next.previous = head;
        Customer result = node.item;
        node.item     = null;
        node.previous = node.next = null;
        return result;
    }

    public boolean remove (Customer customer) {
        lock.lock();
        try {
            if (customer == null || customer.node == null || customer.served) return false;
            log.debug(String.format("[%2d %3d]   lock (remove)", id, customer.id));
            log.debug(String.format("[%2d %3d] waiting for remove", id, customer.id));

            var node = customer.node;
            node.lastPositionInQueue.set(node.positionInQueue.get());
            node.previous.next = node.next;
            node.next.previous = node.previous;
            var temp = node.previous;
            log.debug(String.format("[%2d %3d] removed (position: %d)", id, customer.id, node.positionInQueue.get()));
            node.item        = null;
            node.next        = node.previous = null;
            customer.cashier = null;
            customer.node    = null;
            count.getAndDecrement();
            node = temp;
            while ((node = node.next) != head) {
                node.positionInQueue.getAndDecrement();
            }
            semaphore.release();
            log.debug(String.format("[%2d %3d] semaphore release (remove)", id, customer.id));
        } finally {
            lock.unlock();
            log.debug(String.format("[%2d %3d] unlock (remove)", id, customer.id));
        }
        return true;
    }

    public void serviceCustomer (Customer customer) throws InterruptedException {
        Thread.sleep((long) (random.nextInt(100) / speed));
        nServed.getAndIncrement();
        customer.served = true;
        log.debug(String.format("[%2d %3d] Food is offered, %d", id, customer.id, Thread.currentThread().getId()));
    }
}

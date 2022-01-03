package cash;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import lombok.var;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class Cashier implements Runnable {
    static final int MAX_QUEUE_LENGTH = 10;

    private final AtomicInteger count = new AtomicInteger();
    public        float         speed = 1.f;
    private final Node          head;

    public int           id;
    public AtomicInteger servedVisitors = new AtomicInteger();

    public Cashier (int id, float speed) {
        this();
        this.id    = id;
        this.speed = speed;
    }

    public int getQueueLength () {
        return count.get();
    }

    @Data
    public static class Node {
        public Customer item;
        public Node     next;
        public Node     previous;

        public final AtomicInteger lastPositionInQueue = new AtomicInteger(Integer.MAX_VALUE);
        public final AtomicInteger positionInQueue     = new AtomicInteger(Integer.MAX_VALUE);

        Node (Customer customer) {
            if ((this.item = customer) != null) {
                customer.node = this;
            }
        }

        @Override
        public String toString () {
            return "";
        }
    }

    public Cashier () {
        head          = new Node(null);
        head.previous = head.next = head;
    }

    private final ReentrantLock lock     = new ReentrantLock();
    private final Condition     notEmpty = lock.newCondition();

    public Node put (Customer customer) {
        if (count.get() >= MAX_QUEUE_LENGTH) return null;
        log.debug(String.format("[%2d %3d] waiting for put", id, customer.id));
        lock.lock();
        log.debug(String.format("[%2d %3d]   lock (put)", id, customer.id));
        if (customer.served) return null;
        Node node = null;
        try {
            var cnt = count.get();
            if (cnt < MAX_QUEUE_LENGTH) {
                node          = new Node(customer);
                node.previous = head.previous;
                node.next     = head;

                node.next.previous = node;
                node.previous.next = node;

                node.positionInQueue.set(count.getAndIncrement());
                customer.cashier = this;
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
        lock.lock();
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
        } finally {
            lock.unlock();
            log.debug(String.format("[%2d %3d] unlock (remove)", id, customer.id));
        }
        return true;
    }

    public void serviceCustomer (Customer customer) {
        try {
            Thread.sleep((long) (new Random().nextInt(100) / speed));
            servedVisitors.getAndIncrement();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        customer.served = true;
        log.debug(String.format("[%2d %3d] Food is offered, %d", id, customer.id, Thread.currentThread().getId()));
    }

    @Override
    public void run () {
        log.debug(String.format("[%2d    ] cashier started", id));
        Customer customer;
        while (true) {
            while ((customer = take()) != null) {
                serviceCustomer(customer);
            }
            lock.lock();
            log.debug(String.format("[%2d    ]   lock (run)", id));
            try {
                notEmpty.await();
            } catch (InterruptedException e) {
                ExceptionUtils.rethrowUnchecked(e);
            } finally {
                lock.unlock();
                log.debug(String.format("[%2d    ] unlock (run)", id));
            }
        }
    }
}

import lombok.Data;
import lombok.val;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Cashier implements Runnable {
    static final int MAX_QUEUE_LENGTH = 10;

    private final AtomicInteger count = new AtomicInteger();
    public        float         speed = 1.f;
    private       Node          head;
    private       Node          tail;

    public int id;

    public Cashier (int id, float speed) {
        this();
        this.id    = id;
        this.speed = speed;
    }

    public int getQueueLength () {
        return count.get();
    }

    public boolean enterQueue (Visitor visitor) {
        return put(visitor) != null;
    }

    @Data
    public static class Node {
        Visitor item;
        Node    next;
        Node    previous;

        Node (Visitor item) {
            this.item = item;
        }

        @Override
        public String toString () {
            return "";
        }
    }

    public Cashier () {
        head          = tail = new Node(null);
        head.next     = tail;
        tail.previous = head;
    }

    private final ReentrantLock lock     = new ReentrantLock();
    private final Condition     notEmpty = lock.newCondition();

    public Node put (Visitor visitor) {
        if (count.get() == MAX_QUEUE_LENGTH) return null;
        System.out.printf("[%2d %3d] waiting for put\n", id, visitor.id);
        lock.lock();
        Node node = null;
        try {
            if (count.get() < MAX_QUEUE_LENGTH) {
                node          = new Node(visitor);
                node.previous = tail;
                tail          = tail.next = node;
                visitor.positionInQueue.set(count.getAndIncrement());
                visitor.cashier = this;
                val position     = visitor.positionInQueue.get();
                val lastPosition = visitor.lastPositionInQueue.get();
                System.out.printf("[%2d %3d] put to position: %d (%d)\n", id, visitor.id, position, lastPosition - position);
                if (position == 0) notEmpty.signal();
            }
        } finally {
            lock.unlock();
        }
        return node;
    }

    public Visitor take () {
        if (count.get() == 0) return null;
        Visitor visitor;
        System.out.printf("[%2d    ] waiting for take next visitor\n", id);
        lock.lock();
        try {
            if (count.get() == 0) return null;
            count.getAndDecrement();
            Node first = head;
            head       = head.next;
            first.next = null;
            visitor    = head.item;
            head.item  = null;

            Node node    = head.next;
            int  counter = 0;
            while (node != null) {
                node.item.positionInQueue.set(counter++);
                System.out.printf("[%2d %3d] changed position to: %d\n", id, node.item.id, node.item.positionInQueue.get());
                node = node.next;
            }
        } finally {
            lock.unlock();
        }
        System.out.printf("[%2d %3d] took the next visitor\n", id, visitor.id);
        return visitor;
    }

    public void remove (Visitor visitor) {
        System.out.printf("[%2d %3d] waiting for remove\n", id, visitor.id);
        visitor.lastPositionInQueue.set(visitor.positionInQueue.get());
        lock.lock();
        try {
            Node node     = head.next;
            Node previous = head;
            while (node != null) {
                if (node.item == visitor) { // reference equals should be ok.
                    previous.next = node.next;
                    node.item     = null;
                    node.next     = node;
                    if (node == tail) tail = previous;
                    count.getAndDecrement();

                    node = previous.next;
                    while (node != null) {
                        node.item.positionInQueue.getAndDecrement();
                        node = node.next;
                    }
                    break;
                }
                previous = node;
                node     = node.next;
            }

        } finally {
            lock.unlock();
        }
        System.out.printf("[%2d %3d] removed (position: %d)\n", id, visitor.id, visitor.positionInQueue.get());
    }

    @Override
    public void run () {
        System.out.printf("[%2d    ] cashier started\n", id);
        Visitor visitor;
        while (true) {
            while ((visitor = take()) != null) {
                visitor.offerFood();
            }
            lock.lock();
            try {
                notEmpty.await();
            } catch (InterruptedException e) {
                ExceptionUtils.rethrowUnchecked(e);
            } finally {
                lock.unlock();
            }
        }
    }
}

import lombok.Data;
import lombok.val;
import lombok.var;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Cashier implements Runnable {
    static final int MAX_QUEUE_LENGTH = 10;

    private final AtomicInteger count = new AtomicInteger();
    public        float         speed = 1.f;
    private final Node          head;

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
        public Visitor item;
        public Node    next;
        public Node    previous;

        public final AtomicInteger lastPositionInQueue = new AtomicInteger(Integer.MAX_VALUE);
        public final AtomicInteger positionInQueue     = new AtomicInteger(Integer.MAX_VALUE);

        Node (Visitor visitor) {
            if ((this.item = visitor) != null) {
                visitor.node = this;
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

    public Node put (Visitor visitor) {
        if (count.get() == MAX_QUEUE_LENGTH) return null;
        System.out.printf("[%2d %3d] waiting for put\n", id, visitor.id);
        lock.lock();
        Node node = null;
        try {
            if (count.get() < MAX_QUEUE_LENGTH) {
                node          = new Node(visitor);
                node.previous = head.previous;
                node.next     = head;

                node.next.previous = node;
                node.previous.next = node;

                node.positionInQueue.set(count.getAndIncrement());
                visitor.cashier = this;
                val position     = node.positionInQueue.get();
                val lastPosition = node.lastPositionInQueue.get();
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
            Node node = head.next;
            head.next          = node.next;
            head.next.previous = head;
            visitor            = node.item;
            node.item          = null;
            node.previous      = node.next = null;
            int counter = 0;
            node = head;
            while ((node = node.next) != head) {
                node.positionInQueue.set(counter++);
                System.out.printf("[%2d %3d] changed position to: %d\n", id, node.item.id, node.positionInQueue.get());
            }
        } finally {
            lock.unlock();
        }
        System.out.printf("[%2d %3d] took the next visitor\n", id, visitor.id);
        return visitor;
    }

    public boolean remove (Visitor visitor) {
        lock.lock();
        try {
            if (visitor == null || visitor.node == null) return false;
            System.out.printf("[%2d %3d] waiting for remove\n", id, visitor.id);

            var node = visitor.node;
            node.lastPositionInQueue.set(node.positionInQueue.get());
            var second = node.next;
            node.previous.next = second;
            System.out.printf("[%2d %3d] removed (position: %d)\n", id, visitor.id, node.positionInQueue.get());
            node.item = null;
            node.next = null;
            count.getAndDecrement();
            while ((node = second) != head) {
                node.positionInQueue.getAndDecrement();
            }
        } finally {
            lock.unlock();
        }
        return true;
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

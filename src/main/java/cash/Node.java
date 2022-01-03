package cash;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Node {
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
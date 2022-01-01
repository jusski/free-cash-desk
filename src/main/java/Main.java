import lombok.val;

public class Main {
    public static void main (String[] args) {
        for (int i = 0; i < 500; ++i) {
            val visitor = new Visitor(i);
            new Thread(visitor).start();
        }
    }
}

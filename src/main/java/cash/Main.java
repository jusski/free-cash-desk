package cash;

import lombok.val;
import lombok.var;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

public class Main {
    public static final NumberFormat nf03;

    static {
        val localeUs = new Locale("en", "US");

        nf03 = NumberFormat.getNumberInstance(localeUs);
        nf03.setMaximumFractionDigits(3);
    }

    public static void main (String[] args) {
        val nCustomers = 10000;
        new Thread(() -> {
            ConsoleUtils.cls();
            val cashiers  = FastFoodRestaurant.getCashiers();
            val startTime = System.currentTimeMillis();
            while (true) {
                ConsoleUtils.cls();
                var y     = 1;
                var total = 0;
                ConsoleUtils.printf(0, y++, "Max T.  Time Served Queue");
                for (Cashier cashier : cashiers) {
                    int    queueLength = cashier.getQueueLength();
                    char[] stars       = new char[queueLength];
                    Arrays.fill(stars, '*');
                    ConsoleUtils.printf(20, y,
                            String.format(String.format("%%-%ds", cashier.maxQueueLength), new String(stars)));
                    var customer = cashier.get(0);
                    if (customer != null) {
                        ConsoleUtils.printf(0, y, String.format("%6d", cashier.maxTimeCustomerSpentInQueue));
                        ConsoleUtils.printf(6, y, String.format("%6d", customer.getSpentTimeInLastQueue()));
                    }
                    var served = cashier.nServed.get();
                    total += served;
                    ConsoleUtils.printf(13, y, String.format("%6d", served));
                    ++y;
                }
                ConsoleUtils.printf(6, y, String.format("Total: %6d", total));
                if (total < nCustomers) {
                    ConsoleUtils.printf(0, 0, String.format("Time: %s",
                            nf03.format((System.currentTimeMillis() - startTime) / 1000f)));
                }
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();

        for (int i = 0; i < nCustomers; ++i) {
            val visitor = new Customer(i);
            new Thread(visitor).start();
        }
        ((Cashier) FastFoodRestaurant.getCashiers().toArray()[2]).speed = 100;
    }
}

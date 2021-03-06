package cash;

import lombok.val;
import lombok.var;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Main {
    public static final NumberFormat nf03;

    static {
        val localeUs = new Locale("en", "US");

        nf03 = NumberFormat.getNumberInstance(localeUs);
        nf03.setMaximumFractionDigits(3);
    }

    enum Column {
        MaxTimeInQueue,
        TimeInQueue,
        Served,
        TimeOfWaitingLock,
        MaxTimeOfWaitingLock,
        Queue;

        String getColumnName () {
            switch (this) {
                case MaxTimeInQueue: {return "Max T. ";}
                case TimeInQueue: {return "Time ";}
                case Served: {return "Served ";}
                case TimeOfWaitingLock: {return "Wait L ";}
                case MaxTimeOfWaitingLock: {return "Max W.L ";}
                case Queue: {return "Queue ";}
                default: {return "";}
            }
        }

        int getColumnWidth () {
            switch (this) {
                case MaxTimeInQueue:
                case TimeInQueue:
                case Served:
                case TimeOfWaitingLock:
                case MaxTimeOfWaitingLock: {return Math.max(getColumnName().length(), 7);}
                case Queue: {return 20;}
                default: {return 0;}
            }
        }

        int getX () {
            var x = 0;
            for (var c : Column.values()) {
                if (c == this) return x;
                x += c.getColumnWidth();
            }
            return 0;
        }
    }

    public static void main (String[] args) throws InterruptedException {
        val nCustomers = 10000;
        val cashiers   = Restaurant.getCashiers();

        new Thread(() -> {
            ConsoleUtils.cls();

            val startTime = System.currentTimeMillis();
            while (true) {
                var y     = 1;
                var total = 0;
                for (val c : Column.values()) {
                    ConsoleUtils.printf(c.getX(), y, c.getColumnName());
                }
                ++y;
                for (Cashier cashier : cashiers) {
                    int    queueLength = cashier.getQueueLength();
                    char[] stars       = new char[queueLength];
                    Arrays.fill(stars, '*');
                    ConsoleUtils.printf(Column.Queue.getX(), y,
                            String.format("%%-%ds", cashier.maxQueueLength), new String(stars));
                    var customer = cashier.get(0);
                    if (customer != null) {
                        ConsoleUtils.printf(Column.MaxTimeInQueue.getX(), y,
                                String.format("%%%dd", Column.MaxTimeInQueue.getColumnWidth()),
                                cashier.maxTimeCustomerSpentInQueue);
                        ConsoleUtils.printf(Column.TimeInQueue.getX(), y,
                                String.format("%%%dd", Column.TimeInQueue.getColumnWidth()),
                                customer.getSpentTimeInLastQueue());
                    }
                    var served = cashier.nServed.get();
                    total += served;
                    ConsoleUtils.printf(Column.Served.getX(), y,
                            String.format("%%%dd", Column.Served.getColumnWidth()),
                            served);
                    ConsoleUtils.printf(Column.TimeOfWaitingLock.getX(), y,
                            String.format("%%%dd", Column.TimeOfWaitingLock.getColumnWidth()),
                            cashier.waitingForLockTime);
                    ConsoleUtils.printf(Column.MaxTimeOfWaitingLock.getX(), y,
                            String.format("%%%dd", Column.MaxTimeOfWaitingLock.getColumnWidth()),
                            cashier.maxWaitingForLockTime);
                    ++y;
                }
                ConsoleUtils.printf(0, y, "Total:");
                ConsoleUtils.printf(Column.Served.getX(), y,
                        String.format("%%%dd", Column.Served.getColumnWidth()),
                        total);
                if (total < nCustomers) {
                    ConsoleUtils.printf(0, 0, "Time: %s", nf03.format((System.currentTimeMillis() - startTime) / 1000f));
                }
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();

        val          LUCKY_SIZE   = Math.min(10, nCustomers);
        long[]       luckyThreads = new long[LUCKY_SIZE];
        Set<Integer> luckyNumbers = new HashSet<>();
        while (luckyNumbers.size() < LUCKY_SIZE) {
            luckyNumbers.add(Restaurant.random.nextInt(nCustomers));
        }
        var nLuckies = 0;
        for (int i = 0; i < nCustomers; ++i) {
            val visitor = new Customer(i);
            val thread  = new Thread(visitor);
            thread.start();
            if (luckyNumbers.contains(i)) {
                luckyThreads[nLuckies++] = thread.getId();
            }
        }
        val threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        while (Restaurant.nServed.get() < nCustomers) {
            final ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(luckyThreads, true, false);
            ConsoleUtils.currentLine = 10;
            for (val info : threadInfo) {
                if (info == null) {
                    ConsoleUtils.println("");
                } else {
                    ConsoleUtils.println(String.valueOf(info.getWaitedCount()));
                }
            }
            Thread.sleep(100);
        }
        //System.exit(0);
    }
}

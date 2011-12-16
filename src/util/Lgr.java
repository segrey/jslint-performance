package util;

/**
 * @author Sergey Simonchik
 */
public class Lgr {
    private final String myOwner;

    public Lgr(String myOwner) {
        this.myOwner = myOwner;
    }

    public void log(String actionName, long startNanoTime) {
        long endNanoTime = System.nanoTime();
        System.out.printf("[%s] %s takes %.3f ms\n", myOwner, actionName, (endNanoTime - startNanoTime) / 1000000.0);
    }

    public void logWithThread(String actionName, long startNanoTime) {
        long endNanoTime = System.nanoTime();
        String threadName = Thread.currentThread().getName();
        System.out.printf(threadName + "-[%s] %s takes %.3f ms\n", myOwner, actionName, (endNanoTime - startNanoTime) / 1000000.0);
    }

    public void log(String actionName, long startNanoTime, int count) {
        long endNanoTime = System.nanoTime();
        double total = (endNanoTime - startNanoTime) / 1000000.0;
        System.out.printf("[%s] average %s takes %.3f ms\n", myOwner, actionName, total / count);
    }
}

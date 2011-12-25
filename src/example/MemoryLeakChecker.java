package example;

import main.*;
import util.Helper;
import util.Lgr;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Sergey Simonchik
 */
public class MemoryLeakChecker {

    private static final Lgr LGR = new Lgr(ThreadSafetyChecker.class.getSimpleName());
    private final AtomicInteger totalProcessedTasks = new AtomicInteger(0);

    private void test() throws InterruptedException, ExecutionException {
        final ScriptRunner runner = new ThreadSafeCompileFunction3();
        final String source = Helper.AWAPS;
        final String rightAnswer = runner.run(Helper.OPTIONS, source);

        while (true) {
            long startTimeNano = System.nanoTime();
            doIt(rightAnswer, source, runner, 500);
            LGR.log("doIt", startTimeNano);
            System.out.println("Total processed tasks: " + totalProcessedTasks.intValue());
        }
    }

    private void doIt(final String rightAnswer, final String source, final ScriptRunner runner, int taskCount) {
        ExecutorService executorService = createExecutorService();
        CompletionService<String> completionService = new ExecutorCompletionService<String>(executorService);

        final AtomicInteger runCount = new AtomicInteger(0);
        for (int i = 0; i < taskCount; i++) {
            completionService.submit(new Callable<String>() {
                @Override
                public String call() {
                    int rc = runCount.incrementAndGet();
                    if (rc % 100 == 0) {
                        System.out.println(Thread.currentThread().getName() + " running #" + rc);
                    }
                    return runner.run(Helper.OPTIONS, source);
                }
            });
        }

        AtomicInteger completedTasks = new AtomicInteger(0);
        for (int i = 0; i < taskCount; i++) {
            Future<String> future;
            try {
                future = completionService.take();
                String local = future.get();
                if (!rightAnswer.equals(local)) {
                    throw new RuntimeException("Answers are different!");
                }
                int id = completedTasks.incrementAndGet();
                if (id % 100 == 0) {
                    System.out.println("Completed tasks: " + id);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        totalProcessedTasks.addAndGet(taskCount);
        Future<String> next = completionService.poll();
        if (next != null) {
            throw new RuntimeException("Next should be null!");
        }
    }

    static final AtomicInteger THREAD_ID = new AtomicInteger(0);
    static ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("worker-" + THREAD_ID.incrementAndGet());
            return thread;
        }
    };

    private static ExecutorService createExecutorService() {
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Found " + processors + " processors");
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(10);
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(processors * 2, processors * 2, 2, TimeUnit.SECONDS, queue, threadFactory);
        tpe.allowCoreThreadTimeOut(true);
        tpe.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    queue.put(r);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return tpe;
    }

    public static void main(String[] args) throws Exception {
        new MemoryLeakChecker().test();
    }

}

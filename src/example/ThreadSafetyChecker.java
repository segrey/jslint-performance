package example;

import main.*;
import util.Helper;
import util.Lgr;

import java.util.concurrent.*;

/**
 * @author Sergey Simonchik
 */
public class ThreadSafetyChecker {

    private static final Lgr LGR = new Lgr(ThreadSafetyChecker.class.getSimpleName());

    private void test() throws InterruptedException, ExecutionException {
        final ScriptRunner runner = new ThreadSafeCompileFunction3();
        final String source = Helper.AUTO;
        String rightAnswer = runner.run(Helper.OPTIONS, source);
        int tasks = 10000;
//        ExecutorService service = Executors.newSingleThreadExecutor();
        ExecutorService service = Executors.newFixedThreadPool(16);
        CompletionService<String> completionService = new ExecutorCompletionService<String>(service);
        long startTime = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            completionService.submit(new Callable<String>() {
                @Override
                public String call() {
                    return runner.run(Helper.OPTIONS, source);
                }
            });
        }
        for (int i = 0; i < tasks; i++) {
            Future<String> future = completionService.take();
            String local = future.get();
            if (!rightAnswer.equals(local)) {
                throw new RuntimeException("Answers are different!");
            }
            System.out.println("Done " + i);
        }
        LGR.log("all", startTime, tasks);
        service.shutdown();
    }

    public static void main(String[] args) throws Exception {
        new ThreadSafetyChecker().test();
    }

}

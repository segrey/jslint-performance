package example;

import main.*;
import util.Helper;
import util.Lgr;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Sergey Simonchik
 */
public class ThreadSafeBreaker {

    private static final Lgr LGR = new Lgr(ThreadSafeBreaker.class.getSimpleName());

    private void test() throws InterruptedException, ExecutionException {
        final ScriptRunner runner = new ThreadSafeCompileFunction3();
        String rightAnswer = runner.run(Helper.OPTIONS, Helper.AUTO);
        int tasks = 1000;
//        ExecutorService service = Executors.newSingleThreadExecutor();
        ExecutorService service = Executors.newFixedThreadPool(16);
        CompletionService<String> completionService = new ExecutorCompletionService<String>(service);
        long startTime = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            completionService.submit(new Callable<String>() {
                @Override
                public String call() {
                    return runner.run(Helper.OPTIONS, Helper.AUTO);
                }
            });
        }
        for (int i = 0; i < tasks; i++) {
            Future<String> future = completionService.take();
            String local = future.get();
            if (!rightAnswer.equals(local)) {
                throw new RuntimeException("Answers are different!");
            }
//            System.out.println("Done " + i);
        }
        LGR.log("all", startTime, tasks);
        service.shutdown();
    }

    public static void main(String[] args) throws Exception {
        new ThreadSafeBreaker().test();
    }

}

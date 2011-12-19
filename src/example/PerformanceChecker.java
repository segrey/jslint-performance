package example;

import main.*;
import util.Helper;
import util.Lgr;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PerformanceChecker {

    private void checkPerformance() throws IOException {
        Lgr lgr = new Lgr(PerformanceChecker.class.getSimpleName());
        String source = Helper.AUTO;
        List<List<ScriptRunner>> scriptRunnerGroups = Arrays.asList(
                Arrays.<ScriptRunner>asList(
                        new JavaxScriptingRunner(),
                        new TestRhinoCompile(9),
                        new TestRhinoEvaluate()
                ),
                Arrays.<ScriptRunner>asList(
                        new TestRhinoCompileFunction(9),
                        new TestRhinoEvaluate2()
                )
        );
        System.out.println("Warming up...");
        for (List<ScriptRunner> group : scriptRunnerGroups) {
            String answer = null;
            for (ScriptRunner runner : group) {
                String gAns = findAnswer(runner, source, lgr);
                if (answer == null) {
                    answer = gAns;
                }
                else if (!answer.equals(gAns)) {
                    throw new RuntimeException("Answers are different! 2");
                }
            }
            System.out.println("=======");
            System.out.println("Answer is " + answer);
        }
        System.out.println("Running...");
        for (List<ScriptRunner> group: scriptRunnerGroups) {
            for (ScriptRunner runner : group) {
                int ans = 0;
                int cntRuns = 100;
                long globalStartTime = System.nanoTime();
                for (int i = 1; i <= cntRuns; i++) {
                    String result = runner.run(Helper.OPTIONS, source);
                    ans += result.length();
                }
                lgr.log(runner.getName() + ": " + cntRuns + " runs, ans " + ans, globalStartTime, cntRuns);
            }
        }
    }

    private String findAnswer(ScriptRunner runner, String source, Lgr lgr) {
        String gAns = null;
        for (int i = 1; i <= 2; i++) {
            long startTime = System.nanoTime();
            String lAns = runner.run(Helper.OPTIONS, source);
            lgr.log(runner.getName() + "#" + i, startTime);
            if (gAns == null) {
                gAns = lAns;
            }
            else if (!gAns.equals(lAns)) {
                throw new RuntimeException("Answers are different!");
            }
        }
        if (gAns == null) {
            throw new RuntimeException("gAns is null!");
        }
        return gAns;
    }

    public static void main(String[] args) throws IOException {
        new PerformanceChecker().checkPerformance();
    }
}

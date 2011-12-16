import main.*;
import util.Helper;
import util.Lgr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PerformanceChecker {

    private void checkPerformance() throws IOException {
        Lgr lgr = new Lgr(PerformanceChecker.class.getSimpleName());
        String source = Helper.JSLINT_CODE_ORIGINAL;
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
                String gAns = null;
                for (int i = 1; i <= 10; i++) {
                    long startTime = System.nanoTime();
                    String lAns = runner.run(Helper.OPTIONS, source);
                    lgr.log(runner.getName() + "#" + i, startTime);
                    if (gAns == null) {
                        gAns = lAns;
                    } else {
                        if (!gAns.equals(lAns)) {
                            throw new RuntimeException("Answers are different!");
                        }
                    }
                }
                if (answer == null) {
                    answer = gAns;
                } else {
                    if (!answer.equals(gAns)) {
                        throw new RuntimeException("Answers are different! 2");
                    }
                }
            }
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

    public static void main(String[] args) throws IOException {
        new PerformanceChecker().checkPerformance();
    }
}

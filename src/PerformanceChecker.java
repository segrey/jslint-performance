import java.io.IOException;

public class PerformanceChecker {

    private void checkPerformance() throws IOException {
        Lgr lgr = new Lgr(PerformanceChecker.class.getSimpleName());
        String source = Helper.JSLINT_CODE_ORIGINAL;//Helper.readContent(AUTO_JS_SOURCE);
        ScriptRunner[] runners = {
//                new JavaxScriptingRunner()
                new TestRhinoCompile(9)
                , new TestRhinoCompileFunction(9)
//                , new NodeJSRunner()
                , new TestRhinoEvaluate()
        };
        System.out.println("Warming up...");
        for (ScriptRunner runner : runners) {
            String gAns = null;
            for (int i = 1; i <= 3; i++) {
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
        }
        System.out.println("Running...");
        for (ScriptRunner runner : runners) {
            int ans = 0;
            int cntRuns = 10;
            long globalStartTime = System.nanoTime();
            for (int i = 1; i <= cntRuns; i++) {
                String result = runner.run(Helper.OPTIONS, source);
                ans += result.length();
            }
            lgr.log(runner.getName() + ": " + cntRuns + " runs, ans " + ans, globalStartTime, cntRuns);
        }
    }

    public static void main(String[] args) throws IOException {
        new PerformanceChecker().checkPerformance();
    }
}

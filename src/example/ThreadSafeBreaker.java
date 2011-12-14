package example;

import main.*;
import util.Lgr;

import java.util.*;

/**
 * @author Sergey Simonchik
 */
public class ThreadSafeBreaker {

    private static final Lgr LGR = new Lgr(ThreadSafeBreaker.class.getSimpleName());

    private void test() {
        List<Collection<ScriptRunner>> groups = new ArrayList<Collection<ScriptRunner>>();
        groups.add(Arrays.asList(
                new JavaxScriptingRunner(),
                new TestRhinoCompile(9),
                new TestRhinoCompileFunction(9),
                new TestRhinoEvaluate()
        ));
    }

    public static void main(String[] args) {
        new ThreadSafeBreaker().test();
    }

}

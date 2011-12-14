package main;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import util.Helper;

import javax.script.ScriptException;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class TestRhinoEvaluate implements ScriptRunner {

    private void log(String actionName, long startNanoTime) {
        long endTime = System.nanoTime();
        System.out.printf("[Rhino] %s takes %.3f ms\n", actionName, (endTime - startNanoTime) / 1000000.0);
    }

    @Override
    public String getName() {
        return TestRhinoEvaluate.class.getSimpleName();
    }

    @Override
    public String run(String options, String jsSourceCodeToLint) {
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();

            Scriptable var = ScriptRuntime.toObject(cx, scope, new String[]{options, jsSourceCodeToLint});
            scope.put("params", scope, var);

            Object resObj = cx.evaluateString(scope, Helper.JSLINT_CODE_MODIFIED, "jslint", 1, null);
            return (String) resObj;
        } finally {
            Context.exit();
        }
    }

    public static void main(String[] args) throws ScriptException, IOException {
        new TestRhinoEvaluate().run(Helper.OPTIONS, Helper.EXT_JS_DEBUG_WITH_COMMENTS);
    }

}

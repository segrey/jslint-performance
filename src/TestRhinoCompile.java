import org.mozilla.javascript.*;
import util.Helper;
import util.Lgr;

import javax.script.*;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class TestRhinoCompile implements ScriptRunner {

    private static final Lgr LGR = new Lgr(TestRhinoCompile.class.getSimpleName());

    private final Script myScript;

    public TestRhinoCompile(int optimizationLevel) {
        long startCompileNanoTime = System.nanoTime();
        myScript = compile(optimizationLevel);
        LGR.log("compile", startCompileNanoTime);
    }

    public static Script compile(int optimizationLevel) {
        Context context = Context.enter();
        try {
            context.setOptimizationLevel(optimizationLevel);
            Object resObj = context.compileString(Helper.JSLINT_CODE_MODIFIED, "<jslint>", 1, null);
            if (resObj instanceof Script) {
                Script script = (Script) resObj;
                return script;
            }
            return null;
        } finally {
            Context.exit();
        }
    }

    @Override
    public String getName() {
        return TestRhinoCompile.class.getSimpleName();
    }

    @Override
    public String run(String options, String jsSourceCodeToLint) {
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();

            Scriptable var = ScriptRuntime.toObject(cx, scope, new String[]{options, jsSourceCodeToLint});
            scope.put("params", scope, var);

            Object resObj = myScript.exec(cx, scope);
            return (String) resObj;
        } finally {
            Context.exit();
        }
    }

    public static void main(String[] args) throws ScriptException, IOException {
        new TestRhinoCompile(9).run(Helper.OPTIONS, Helper.JSLINT_CODE_MODIFIED);
    }

}

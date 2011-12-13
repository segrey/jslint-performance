import com.sun.istack.internal.NotNull;
import org.mozilla.javascript.*;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public class TestRhinoCompileFunction implements ScriptRunner {

    private static final Lgr LGR = new Lgr(TestRhinoCompileFunction.class.getSimpleName());

    private final Function myJSLINTFunction;

    public TestRhinoCompileFunction(int optimizationLevel) {
        long startCompileNanoTime = System.nanoTime();
        myJSLINTFunction = compile(optimizationLevel);
        LGR.log("compile", startCompileNanoTime);
    }

    public static Function compile(int optimizationLevel) {
        Context context = Context.enter();
        try {
            context.setOptimizationLevel(optimizationLevel);
            Script script = context.compileString(Helper.JSLINT_CODE_ORIGINAL, "<jslint>", 1, null);
            Scriptable scope = context.initStandardObjects();
            script.exec(context, scope);
            Object jsLintObj = scope.get("JSLINT", scope);
            if (jsLintObj instanceof Function) {
                Function jsLint = (Function) jsLintObj;
                return jsLint;
            } else {
                throw new RuntimeException("JSLINT is undefined or not a function.");
            }
        } finally {
            Context.exit();
        }
    }

    @Override
    public String getName() {
        return TestRhinoCompileFunction.class.getSimpleName();
    }

    @Override
    public String run(String options, String jsSourceCodeToLint) {
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("indent", 4);
            map.put("maxerr", 100000);
            NativeObject optionsNativeObject = convertOptionsToNativeObject(map);
            Object[] args = {jsSourceCodeToLint, optionsNativeObject};
            Object status = myJSLINTFunction.call(cx, scope, scope, args);
            Boolean noErrors = (Boolean) Context.jsToJava(status, Boolean.class);
            if (!noErrors) {
                StringBuilder builder = new StringBuilder();
                for (Object error : ((NativeArray) myJSLINTFunction.get("errors", scope)).toArray()) {
                    if (error instanceof NativeObject) {
                        if (builder.length() > 0) {
                            builder.append("\n");
                        }
                        builder.append(errorToString((NativeObject) error));
                    }
                }
                return builder.toString();
            }
            return "";
        } finally {
            Context.exit();
        }
    }

    private static int toInt(Object obj) {
//        if (obj instanceof Number) {
            Number number = (Number) obj;
            return number.intValue();
//        } else {
//            System.out.println();
//            return 0;
//        }
    }

    private static String errorToString(@NotNull NativeObject error) {
        return toInt(error.get("line")) + "^^^" + toInt(error.get("character")) + "^^^" + error.get("reason");
    }

    private NativeObject convertOptionsToNativeObject(Map<String, Object> options) {
        NativeObject object = new NativeObject();
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            final Object value = entry.getValue();
            final Object nativeObj;
            if (value instanceof Boolean) {
                nativeObj = value;
            } else if (value instanceof String) {
                nativeObj = new NativeArray(value.toString().split(","));
            } else if (value instanceof Number) {
                nativeObj = value;
            } else {
                throw new RuntimeException();
            }
            String key = entry.getKey();
            object.defineProperty(key, nativeObj, ScriptableObject.READONLY);
        }
        return object;
    }

    public static void main(String[] args) throws ScriptException, IOException, InterruptedException {
        TestRhinoCompileFunction test = new TestRhinoCompileFunction(9);
        long startTime = System.nanoTime();
        String res = test.run(Helper.OPTIONS, Helper.EXT_JS_DEBUG_WITH_COMMENTS);
        LGR.log("run", startTime);
        System.out.println("res is " + res);
    }

}

package main;

import com.sun.istack.internal.NotNull;
import org.mozilla.javascript.*;
import util.Helper;
import util.Lgr;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public class ThreadSafeCompileFunction3 implements ScriptRunner {

    private static final Lgr LGR = new Lgr(ThreadSafeCompileFunction3.class.getSimpleName());
    private final ThreadLocal<Runner> myThreadLocalFunction = new ThreadLocal<Runner>() {
        @Override
        protected Runner initialValue() {
            if (myScriptData == null) {
                myScriptData = new ScriptData(9);
            }
            return new Runner(myScriptData);
        }
    };
    private volatile ScriptData myScriptData;

    @Override
    public String getName() {
        return ThreadSafeCompileFunction3.class.getName();
    }

    @Override
    public String run(String options, String jsSourceCodeToLint) {
        Runner runner = myThreadLocalFunction.get();
        return runner.run(Helper.OPTIONS, Helper.AUTO);
    }

    private static class ScriptData {
        private final Script myScript;

        private ScriptData(int optimizationLevel) {
            long startCompileNanoTime = System.nanoTime();
            myScript = compile(optimizationLevel);
            LGR.logWithThread("compile", startCompileNanoTime);
        }

        private static Script compile(int optimizationLevel) {
            Context context = Context.enter();
            try {
                context.setOptimizationLevel(optimizationLevel);
                return context.compileString(Helper.JSLINT_CODE_ORIGINAL, "<jslint>", 1, null);
            } finally {
                Context.exit();
            }
        }

        public Script getScript() {
            return myScript;
        }
    }

    private static class Runner {
        private final FunctionWithScope myFunctionWithScope;

        public Runner(ScriptData scriptData) {
            long startCompileNanoTime = System.nanoTime();
            myFunctionWithScope = extractFunctionWithScope(scriptData.getScript());
            LGR.logWithThread("extractFunctionWithScope", startCompileNanoTime);
        }

        public static FunctionWithScope extractFunctionWithScope(Script script) {
            Context context = Context.enter();
            try {
                Scriptable scope = context.initStandardObjects();
                script.exec(context, scope);
                Object jsLintObj = scope.get("JSLINT", scope);
                if (jsLintObj instanceof Function) {
                    Function jsLint = (Function) jsLintObj;
                    return new FunctionWithScope(jsLint, scope);
                } else {
                    throw new RuntimeException("JSLINT is undefined or not a function.");
                }
            } finally {
                Context.exit();
            }
        }

        public String run(String options, String jsSourceCodeToLint) {
            Context cx = Context.enter();
            try {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("indent", 4);
                map.put("maxerr", 100000);
                NativeObject optionsNativeObject = convertOptionsToNativeObject(map);
                Object[] args = {jsSourceCodeToLint, optionsNativeObject};
                Function function = myFunctionWithScope.myFunction;
                Scriptable scope = myFunctionWithScope.myScriptable;
                Object status = function.call(cx, scope, scope, args);
                Boolean noErrors = (Boolean) Context.jsToJava(status, Boolean.class);
                if (!noErrors) {
                    StringBuilder builder = new StringBuilder();
                    for (Object error : ((NativeArray) function.get("errors", scope)).toArray()) {
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
            Number number = (Number) obj;
            return number.intValue();
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
    }

    private static class FunctionWithScope {
        private final Function myFunction;
        private final Scriptable myScriptable;

        private FunctionWithScope(Function myFunction, Scriptable myScriptable) {
            this.myFunction = myFunction;
            this.myScriptable = myScriptable;
        }
    }
}

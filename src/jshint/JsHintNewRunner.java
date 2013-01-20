package jshint;

import com.sun.istack.internal.NotNull;
import main.ScriptRunner;
import org.mozilla.javascript.*;
import util.Helper;
import util.Lgr;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class JsHintNewRunner implements ScriptRunner {

    private static final Lgr LGR = new Lgr(JsHintNewRunner.class.getSimpleName());
  private static final String JSHINT_PATH_R12 = "./jshint/jshint-r12.js";
  private static final String JSHINT_PATH_1_0_0_r4 = "./jshint/jshint-1.0.0-r4.js";
  private static final String JSHINT_PATH_NEW = "./jshint/jshint-new.js";

    private final ThreadLocal<Runner> myThreadRunner = new ThreadLocal<Runner>() {
        @Override
        protected Runner initialValue() {
            if (myScriptData == null) {
                myScriptData = new ScriptData(9);
            }
            return new Runner(myScriptData.getScript());
        }
    };
    private volatile ScriptData myScriptData;

    @Override
    public String getName() {
        return JsHintNewRunner.class.getName();
    }

    @Override
    public String run(String options, String jsSourceCodeToLint) {
        Runner runner = myThreadRunner.get();
        return runner.run(options, jsSourceCodeToLint);
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
                File jshintJsSrc = new File(JSHINT_PATH_NEW);
                return context.compileString(Helper.readContent(jshintJsSrc), "<jshint>", 1, null);
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

        public Runner(Script script) {
            long startCompileNanoTime = System.nanoTime();
            myFunctionWithScope = extractFunctionWithScope(script);
            LGR.logWithThread("extractFunctionWithScope", startCompileNanoTime);
        }

        public static FunctionWithScope extractFunctionWithScope(Script script) {
            Context context = Context.enter();
            try {
                Scriptable scope = context.initStandardObjects();
                script.exec(context, scope);
                Object jsHintObj = scope.get("JSHINT", scope);
                if (jsHintObj instanceof Function) {
                    Function jsHint = (Function) jsHintObj;
                    return new FunctionWithScope(jsHint, scope);
                } else {
                    throw new RuntimeException("JSHINT is undefined or not a function.");
                }
            } finally {
                Context.exit();
            }
        }

        public String run(String options, String jsSourceCodeToLint) {
            Context cx = Context.enter();
            try {
                Map<String, Object> optionMap = convertOptionsToMap(options);
                NativeObject optionsNativeObject = convertOptionsToNativeObject(optionMap);
                Object[] args = {jsSourceCodeToLint, optionsNativeObject};
                Function function = myFunctionWithScope.myFunction;
                Scriptable scope = myFunctionWithScope.myScriptable;
                Object status = function.call(cx, scope, scope, args);
                Boolean noErrors = (Boolean) Context.jsToJava(status, Boolean.class);
                if (!noErrors) {
                    StringBuilder builder = new StringBuilder();
                    NativeArray errors = (NativeArray) function.get("errors", scope);
                    for (Object error : errors.toArray()) {
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

        private Map<String, Object> convertOptionsToMap(String options) {
            Map<String, Object> result = new HashMap<String, Object>();
            if (options.isEmpty()) {
                return result;
            }
            String[] parts = options.split(Pattern.quote(","));
            for (String part : parts) {
                int colonInd = part.indexOf(':');
                final String name, value;
                if (colonInd == -1) {
                    throw new RuntimeException("No ':' symbol!");
                }
                name = part.substring(0, colonInd);
                value = part.substring(colonInd + 1);
                if ("true".equals(value) || "false".equals(value)) {
                    result.put(name, Boolean.valueOf(value));
                } else {
                    int intValue = Integer.parseInt(value);
                    result.put(name, intValue);
                }
            }
            return result;
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

    public static void main(String[] args) {
        JsHintNewRunner runner = new JsHintNewRunner();
        String out = runner.run("", Helper.readContent(new File("./test_source/app.js")));
        System.out.println(out);
    }
}


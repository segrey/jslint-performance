package main;

import com.sun.istack.internal.NotNull;
import org.mozilla.javascript.*;
import util.Helper;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestRhinoEvaluate2 implements ScriptRunner {

    @Override
    public String getName() {
        return TestRhinoEvaluate2.class.getSimpleName();
    }

    @Override
    public String run(String options, String jsSourceCodeToLint) {
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            Function jsLint = getJSLintFunction(cx, scope);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("indent", 4);
            map.put("maxerr", 100000);

            Object functionArgs[] = {jsSourceCodeToLint, convertOptionsToNativeObject(map)};
            Object status = jsLint.call(cx, scope, scope, functionArgs);
            Boolean noErrors = (Boolean) Context.jsToJava(status, Boolean.class);
            if (noErrors) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (Object error : ((NativeArray) jsLint.get("errors", scope)).toArray()) {
                if (error instanceof NativeObject) {
                    if (builder.length() > 0) {
                        builder.append("\n");
                    }
                    builder.append(errorToString((NativeObject) error));
                }
            }
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private Function getJSLintFunction(@NotNull Context cx, @NotNull Scriptable scope) throws IOException {
        cx.setOptimizationLevel(9);
        cx.evaluateString(scope, Helper.JSLINT_CODE_ORIGINAL, "jslint", 1, null);
        Object jsLint = scope.get("JSLINT", scope);
        if (!(jsLint instanceof Function)) {
            throw new RuntimeException("JSLINT is undefined or not a function.");
        }
        return (Function) jsLint;
    }

    public static void main(String[] args) throws ScriptException, IOException {
        System.out.println(new TestRhinoEvaluate2().run(Helper.OPTIONS, Helper.AWAPS));
    }

}

package common;

import com.google.common.base.Supplier;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * @author Sergey Simonchik
 */
public class RhinoFunctionManager {

  private final ThreadLocal<FunctionWithScope> myThreadLocalFunction = new ThreadLocal<FunctionWithScope>() {
    @Override
    protected FunctionWithScope initialValue() {
      if (myScript == null) {
        synchronized (myThreadLocalFunction) {
          if (myScript == null) {
            myScript = compileScript(9);
          }
        }
      }
      return extractFunctionWithScope(myScript);
    }
  };

  private volatile Script myScript;

  private final Supplier<String> myScriptSourceProvider;
  private final String myFunctionName;

  public RhinoFunctionManager(@NotNull Supplier<String> scriptSourceProvider,
                              @NotNull String functionName) {
    myScriptSourceProvider = scriptSourceProvider;
    myFunctionName = functionName;
  }

  @NotNull
  public String getFunctionName() {
    return myFunctionName;
  }

  private Script compileScript(int optimizationLevel) {
    long startNano = System.nanoTime();
    Context context = Context.enter();
    try {
      context.setOptimizationLevel(optimizationLevel);
      String scriptSource = myScriptSourceProvider.get();
      return context.compileString(scriptSource, "<" + myFunctionName + " script>", 1, null);
    } finally {
      Context.exit();
      System.out.println(formatMessage(startNano, myFunctionName + " script rhino compilation"));
    }
  }

  @NotNull
  private FunctionWithScope extractFunctionWithScope(@NotNull Script script) {
    long startNano = System.nanoTime();
    Context context = Context.enter();
    try {
      Scriptable scope = context.initStandardObjects();
      script.exec(context, scope);
      Object jsLintObj = scope.get(myFunctionName, scope);
      if (jsLintObj instanceof Function) {
        Function jsLint = (Function) jsLintObj;
        return new FunctionWithScope(jsLint, scope);
      } else {
        throw new RuntimeException(myFunctionName + " is undefined or not a function.");
      }
    } finally {
      Context.exit();
      System.out.println(formatMessage(startNano, myFunctionName + " function extraction"));
    }
  }

  private static String formatMessage(long startTimeNano, @NotNull String actionName) {
    long nanoDuration = System.nanoTime() - startTimeNano;
    return String.format("[%s] %s took %.2f ms",
                         Thread.currentThread().getName(),
                         actionName,
                         nanoDuration / 1000000.0);
  }

  @NotNull
  public FunctionWithScope getFunctionWithScope() {
    return myThreadLocalFunction.get();
  }

}

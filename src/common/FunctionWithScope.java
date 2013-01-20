package common;

import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class FunctionWithScope {
    private final Function myFunction;
    private final Scriptable myScope;

    public FunctionWithScope(@NotNull Function function, @NotNull Scriptable scope) {
        myFunction = function;
        myScope = scope;
    }

    @NotNull
    public Function getFunction() {
        return myFunction;
    }

    @NotNull
    public Scriptable getScope() {
        return myScope;
    }
}

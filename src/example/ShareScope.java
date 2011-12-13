package example;

import org.mozilla.javascript.*;
import util.Helper;

import java.io.File;
import java.util.Arrays;

/**
 * @author Sergey Simonchik
 */
public class ShareScope {
    private void test() {
        Function function = extractFunction();
        Context context = Context.enter();
        try {
//            Scriptable sharedScope = createSharedScope();
//            Scriptable newScope = context.newObject(sharedScope);
//            newScope.setPrototype(sharedScope);
//            newScope.setParentScope(null);
            ScriptableObject newScope = context.initStandardObjects();
            Scriptable obj = context.newObject(newScope, "String", new Object[] {"qqq"});
            newScope.put("b", newScope, obj);

            System.out.println(Arrays.toString(newScope.getAllIds()));
            System.out.println(newScope.get("b"));

            Object res = null;
            try {
                res = function.call(context, newScope, newScope, new Object[] {1});
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(Arrays.toString(newScope.getAllIds()));
            System.out.println(newScope.get("b"));
            System.out.println("result is " + res);
        } finally {
             Context.exit();
        }
    }

    private Scriptable createSharedScope() {
        Context context = Context.enter();
        try {
            Scriptable scriptable = context.initStandardObjects();
            Scriptable obj = ScriptRuntime.toObject(context, scriptable, "qqq");
            scriptable.put("b", scriptable, obj);
            return scriptable;
        } finally {
            Context.exit();
        }
    }

    private Function extractFunction() {
        Context context = Context.enter();
        try {
            context.setOptimizationLevel(9);
            String source = Helper.readContent(new File("./src/example/share.js"));
            Script script = context.compileString(source, "<jslint>", 1, null);
            ScriptableObject scope = context.initStandardObjects();
            Scriptable obj = context.newObject(scope, "String", new Object[] {"www"});
            scope.put("b", scope, obj);
            script.exec(context, scope);
            Object res = scope.get("func", scope);
            if (!(res instanceof Function)) {
                throw new RuntimeException(res + " is not a function");
            }
            return (Function) res;
        } finally {
            Context.exit();
        }
    }

    public static void main(String[] args) {
        new ShareScope().test();
    }
}

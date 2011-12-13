import javax.script.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public class JavaxScriptingRunner implements ScriptRunner {

    private static final Lgr LGR = new Lgr(JavaxScriptingRunner.class.getSimpleName());

    private final CompiledScript myCompiledScript;

    public JavaxScriptingRunner() {
        try {
            myCompiledScript = compile();
        } catch (Exception e) {
            throw new RuntimeException("Can't compile", e);
        }
    }

    private static CompiledScript compile() throws IOException, ScriptException {
        long startCompileTime = System.nanoTime();
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        Compilable compilable = (Compilable)engine;
        CompiledScript compiledScript = compilable.compile(Helper.JSLINT_CODE_MODIFIED);
        LGR.log("compiling", startCompileTime);
        return compiledScript;
    }

    @Override
    public String getName() {
        return JavaxScriptingRunner.class.getSimpleName();
    }

    @Override
    public String run(String options, String jsSourceCodeToLint) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("params", new String[] {options, jsSourceCodeToLint});

        Object resultObject;
        try {
            resultObject = myCompiledScript.eval(new SimpleBindings(m));
            return (String) resultObject;
        } catch (ScriptException e) {
            throw new RuntimeException("Can't run", e);
        }
    }

    public static void main(String[] args) throws ScriptException, IOException {
        JavaxScriptingRunner runner = new JavaxScriptingRunner();
        String res = runner.run(Helper.OPTIONS, Helper.EXT_JS_DEBUG_WITH_COMMENTS);
        System.out.println(res);
    }

}

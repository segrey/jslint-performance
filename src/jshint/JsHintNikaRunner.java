package jshint;

import main.ScriptRunner;
import util.Helper;

import javax.script.*;
import java.io.File;
import java.util.HashMap;

public class JsHintNikaRunner implements ScriptRunner {

    private final CompiledScript myCompiledJSLintScript;

    public JsHintNikaRunner() {
        this.myCompiledJSLintScript = compile();
    }

    private CompiledScript compile() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        if (!(engine instanceof Compilable)) {
            throw new RuntimeException("Engine is expected to be Compilable");
        }
        try {
            File jshintOldSrc = new File("./src/jshint/nika-jshint.js");
            final String linterText = Helper.readContent(jshintOldSrc);
            return ((Compilable) engine).compile(linterText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return JsHintNikaRunner.class.getSimpleName();
    }

    @Override
    public String run(String options, String jsSourceCodeToLint) {
        synchronized (myCompiledJSLintScript) {
            try {
                HashMap<String, Object> m = new HashMap<String, Object>();
                m.put("_source", jsSourceCodeToLint);
                m.put("_options", options);
                Object resultObject = myCompiledJSLintScript.eval(new SimpleBindings(m));
                return (String)resultObject;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        ScriptRunner runner = new JsHintNikaRunner();
        String source = Helper.readContent(new File("./test_source/app.js"));
        String out = runner.run("", source);
        System.out.println(out);
    }
}

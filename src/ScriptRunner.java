/**
 * @author Sergey Simonchik
 */
public interface ScriptRunner {
    String getName();
    String run(String options, String jsSourceCodeToLint);
}

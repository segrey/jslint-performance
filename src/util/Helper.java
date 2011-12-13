package util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Sergey Simonchik
 */
public class Helper {

    private static final File BASE_PATH = new File(".");

    private static final File MODIFIED_PATH = new File(BASE_PATH, "jslint_modified.js");
    private static final File ORIGINAL_PATH = new File(BASE_PATH, "jslint.js");
    public static final String OPTIONS = "indent:4,maxerr:100000";

    public static final String JSLINT_CODE_MODIFIED;
    public static final String JSLINT_CODE_ORIGINAL;
    public static final String EXT_JS_DEBUG_WITH_COMMENTS;
    public static final String AUTO;
    public static final String AWAPS;

    public static final String WRONG_INDENT =
            "if (1 === 1) {\n" +
            "  a = 1;\n" +
            "}";

    static {
        JSLINT_CODE_MODIFIED = readContent(MODIFIED_PATH);
        JSLINT_CODE_ORIGINAL = readContent(ORIGINAL_PATH);
        EXT_JS_DEBUG_WITH_COMMENTS = readContent(new File(BASE_PATH, "./test_source/ext-all-debug-w-comments.js"));
        AUTO = readContent(new File(BASE_PATH, "./test_source/auto_yandex_ru.js"));
        AWAPS = readContent(new File(BASE_PATH, "./test_source/awaps.js"));
    }

    public static String readContent(String fileName) throws IOException {
        return readContent(new File(BASE_PATH, fileName));
    }

    public static String readContent(File file) {
        try {
            return _readContent(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String _readContent(File file) throws IOException {
        Reader reader = new FileReader(file);
        StringBuilder builder = new StringBuilder();
        try {
            int ch;
            while ((ch = reader.read()) != -1) {
                builder.append((char) ch);
            }
            return builder.toString();
        }
        finally {
            reader.close();
        }
    }

}

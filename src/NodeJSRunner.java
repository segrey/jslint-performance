import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

/**
 * @author Sergey Simonchik
 */
public class NodeJSRunner implements ScriptRunner {
    private static final String NODE_PATH = "C:/Users/Sergey.Simonchik/nodes/node-v0.6.1/node.exe";

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String NODEJS_ADDON;

    static {
        try {
            NODEJS_ADDON = Helper.readContent("nodejs_addon.js");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final PrintWriter myPrintWriter;
    private final BufferedReader myBufferedReader;
    private final Process myProcess;

    public NodeJSRunner() throws IOException {
        File tempFile = File.createTempFile("jslint-nodejs", ".js");
        PrintWriter pw = new PrintWriter(tempFile);
        pw.println(Helper.JSLINT_CODE_ORIGINAL);
        pw.println();
        pw.println(NODEJS_ADDON);
        pw.close();

        ProcessBuilder processBuilder = new ProcessBuilder(NODE_PATH, tempFile.getAbsolutePath());
        processBuilder.directory(new File("."));
        Process process;
        try {
            process = processBuilder.start();
            myProcess = process;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        myBufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF8));
        myPrintWriter = new PrintWriter(process.getOutputStream(), false);
    }

    @Override
    public String getName() {
        return NodeJSRunner.class.getName();
    }

    static int ourRequestId = 1;
    @Override
    public String run(String options, String jsSourceCodeToLint) {
        final int requestId = ourRequestId++;

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String expectedLine = "Request#" + requestId + " has been handled!";
                while (true) {
                    String line;
                    try {
                        line = myBufferedReader.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (line == null) {
                        System.out.println("Null line encountered");
                        return;
                    }
                    System.out.println(line);
                    if (expectedLine.equals(line)) {
                        break;
                    }
                }
                System.out.println("Reading thread is ended.");
                countDownLatch.countDown();
            }
        }).start();
        String escapeSourceCode = escape(jsSourceCodeToLint, '|', '@');
        String escapedOptions = escape(options, '|', '@');
        String command = requestId + "@" + escapedOptions + "@" + escapeSourceCode + "@";
        String refinedCommand = command.replaceAll("\r\n", "\n");
        String escapedCommand =  escape(refinedCommand, '^', '\n', 'n');
/*
        try {
            PrintWriter pw = new PrintWriter("./encoded_command");
            pw.print(escapedCommand);
            pw.print('\n');
            pw.close();
            System.out.println("DONE writing");
            System.exit(1);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
*/
        myPrintWriter.print(escapedCommand);
        myPrintWriter.print("\n");
        myPrintWriter.flush();
/*
        try {
            countDownLatch.await();
            System.out.println("GOOD!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
*/

/*
        StringBuilder buffer = new StringBuilder();
        String expectedLine = "Request#" + requestId + " has been handled!";
        while (true) {
            String line;
            try {
                line = myBufferedReader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (line == null) {
                throw new RuntimeException("Line is null!");
            }
            System.out.println("@" + line + "@");
            if (expectedLine.equals(line)) {
                break;
            }
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append(line);
        }
        return buffer.toString();
*/
        return "<botva>";
    }

    private static String escape(String str, char escapeChar, char targetChar) {
        return escape(str, escapeChar, targetChar, targetChar);
    }

    private static String escape(String str, char escapeChar, char targetChar, char replacementChar) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == escapeChar) {
                builder.append(escapeChar).append(escapeChar);
            } else if (ch == targetChar) {
                builder.append(escapeChar).append(replacementChar);
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private void shutdown() throws InterruptedException {
        myProcess.destroy();
        myProcess.waitFor();
    }

    public static void main(String[] args) throws Exception {
        NodeJSRunner runner = new NodeJSRunner();
        String res = runner.run(Helper.OPTIONS, Helper.JSLINT_CODE_ORIGINAL);
//        System.out.println(res);
        runner.shutdown();
    }
}

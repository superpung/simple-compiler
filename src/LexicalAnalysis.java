package lexical;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author super
 */
public class LexicalAnalysis {
    private static final List<String> KEYWORDS = new ArrayList<>(Arrays.asList(
            "while", "for", "continue", "break", "if", "else", "float", "int", "char", "void", "return"
    ));
    private static final List<String> OP = new ArrayList<>(Arrays.asList(
            "+", "-", "*", "/", "%", "=", ">", "<", "==", "<=", ">=", "!=", "++", "--", "&&", "||", "+=", "-=", "*=", "/=", "%="
    ));
    private static final List<String> SE = new ArrayList<>(Arrays.asList(
            "(", ")", "{", "}", ";", ",", "[", "]"
    ));
    private static final List<String> OTHER_TOKENS = new ArrayList<>(Arrays.asList(
            "IDN", "INT", "FLOAT", "CHAR", "STR"
    ));

    private final String content;
    private List<Map<String, String>> result;

    public LexicalAnalysis(String filename) {
        content = readFromFile(filename);
        analyze();
    }

    public void printResult() {
        System.out.println(content);
    }

    private void analyze() {
        int length = content.length();
        int index = 0;

        while (index < length) {
            if (content.charAt(index) == ' ' || content.charAt(index) == '\n') {
                index++;
                continue;
            }
            if (Character.isLetter(content.charAt(index))) {
                int beginIndex = index;
                int endIndex = ++index;
                for (endIndex = index; Character.isLetter(content.charAt(index)); index++) {

                }
                String buffer = content.substring(beginIndex, endIndex);
                if (KEYWORDS.contains(buffer)) {
                    Map<String, String> token = new HashMap<>();
                    token.put(buffer.toUpperCase(), "");
                    result.add(token);
                }
            }
        }
    }

    private String readFromFile(String filename) {
        Reader reader = null;
        StringBuffer buf = new StringBuffer();
        try {
            char[] chars = new char[1024];
            reader = new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8);
            int readed = reader.read(chars);
            while (readed != -1) {
                buf.append(chars, 0, readed);
                readed = reader.read(chars);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            close(reader);
        }
        return buf.toString();
    }

    private void close(Closeable inout) {
        if (inout != null) {
            try {
                inout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

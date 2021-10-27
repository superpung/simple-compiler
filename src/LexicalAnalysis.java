import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final List<Character> SE = new ArrayList<>(Arrays.asList(
            '(', ')', '{', '}', ';', ',', '[', ']'
    ));
//    private static final List<String> OTHER_TOKENS = new ArrayList<>(Arrays.asList(
//            "IDN", "INT", "FLOAT", "CHAR", "STR"
//    ));
    private static final List<Character> SINGLE_OP = new ArrayList<>(Arrays.asList(
            '+', '-', '*', '/', '%', '=', '>', '<', '!', '&', '|'
    ));

    private final List<Character> analyzed;
    private final List<Character> analyzing;
    private final List<String> tokensResult;
    private final Map<String, String[]> tableResult;

    public LexicalAnalysis(String filename) {
        String content = readFromFile(filename) + '\n';
        analyzing = content.chars().mapToObj(e -> (char)e).collect(Collectors.toList());
        analyzed = new ArrayList<>();
        tokensResult = new ArrayList<>();
        tableResult = new HashMap<>();
        analyze();
    }

    public void printResult() {
        System.out.println(tokensResult.toString());
        for (String token: tokensResult) {
            String output = "";
            output += token + "\t<";
            String[] map = tableResult.get(token);
            output += map[0] + "," + map[1] + ">";
            System.out.println(output);
        }
    }

    private void analyze() {
        int state = 0;

        while (!analyzing.isEmpty()) {
            if (state == 0) {
                // state 0: initial state
                char input = doCheck();

                if (input == ' ' || input == '\t' || input == '\n') {
                    skipCheck();
                } else if (Character.isLetter(input) || input == '_') {
                    state = 1;
                } else if (Character.isDigit(input)) {
                    state = 2;
                } else if (SINGLE_OP.contains(input)) {
                    state = 3;
                } else if (SE.contains(input)) {
                    state = 4;
                } else if (input == '\''){
                    state = 10;
                } else if (input == '\"') {
                    state = 11;
                } else {
                    System.out.println("Unrecognized input: " + input + "!");
                }
            } else if (state == 1) {
                // state 1: 1 letter before
                char input = doCheck();

                if (Character.isLetter(input) || Character.isDigit(input) || input == '_') {
                    state = 1;
                } else {
                    state = 5;
                    undoCheck();
                }
            } else if (state == 2) {
                // state 2: 1 digit before
                char input = doCheck();

                if (Character.isDigit(input)) {
                    state = 2;
                } else if (input == '.') {
                    state = 6;
                } else {
                    state = 7;
                    undoCheck();
                }
            } else if (state == 3) {
                // state 3: single or double op
                char input = doCheck();

                if (!SINGLE_OP.contains(input)) {
                    undoCheck();
                }
                String token = getStringFromList(analyzed);
                if (OP.contains(token)) {
                    finishCheck("OP");
                    state = 0;
                } else {
                    state = 9;
                    undoCheck();
                }
            } else if (state == 4) {
                // state 4: se
                finishCheck("SE");
                state = 0;
            } else if (state == 5) {
                // state 5: keyword or idn
                String token = getStringFromList(analyzed);
                if (KEYWORDS.contains(token)) {
                    finishCheck(token.toUpperCase());
                } else {
                    finishCheck("IDN");
                }
                state = 0;
            } else if (state == 6) {
                // state 6: int .
                char input = doCheck();

                if (Character.isDigit(input)) {
                    state = 6;
                } else {
                    state = 7;
                    undoCheck();
                }
            } else if (state == 7) {
                // state 7: int or float
                finishCheck("CONST");
                state = 0;
            } else if (state == 9) {
                // state 9: single op
                finishCheck("OP");
                state = 0;
            } else if (state == 10) {
                // state 10: char
                char input = doCheck();

                if (input == '\'') {
                    finishCheck("CHAR");
                    state = 0;
                } else {
                    state = 10;
                }
            } else if (state == 11) {
                // state 11: string
                char input = doCheck();

                if (input == '\"') {
                    finishCheck("STR");
                    state = 0;
                } else {
                    state = 11;
                }
            } else {
                System.out.println("Unreachable state!");
            }
        }
    }

    private char doCheck() {
        char c = analyzing.get(0);
        analyzing.remove(0);
        analyzed.add(c);
        return c;
    }

    private void skipCheck() {
        analyzed.clear();
    }

    private void undoCheck() {
        char c = analyzed.get(analyzed.size() - 1);
        analyzed.remove(analyzed.size() - 1);
        analyzing.add(0, c);
    }

    private void finishCheck(String type) {
        String token = getStringFromList(analyzed);
        String value = "_";
        if ("IDN".equals(type) || "CONST".equals(type) || "CHAR".equals(type) || "STR".equals(type)) {
            value = token;
        }
        String[] typeAndValue = new String[]{type, value};
        tokensResult.add(token);
        tableResult.put(token, typeAndValue);
        analyzed.clear();
    }

    private String getStringFromList(List<Character> input) {
        StringBuilder output = new StringBuilder();
        for (char ch: input) {
            output.append(ch);
        }
        return output.toString();
    }

    private String readFromFile(String filename) {
        Reader reader = null;
        StringBuilder buf = new StringBuilder();
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

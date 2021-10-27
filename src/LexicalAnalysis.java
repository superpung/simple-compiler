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
    private static final List<String> OTHER_TOKENS = new ArrayList<>(Arrays.asList(
            "IDN", "INT", "FLOAT", "CHAR", "STR"
    ));
    private static final List<Character> SINGLE_OP = new ArrayList<>(Arrays.asList(
            '+', '-', '*', '/', '%', '=', '>', '<', '!', '&', '|'
    ));
//    private static final List<String> TOKENS  = new ArrayList<String>() {
//        {
//            addAll(KEYWORDS);
//            addAll(OP);
//            addAll(SE);
//            addAll(OTHER_TOKENS);
//        }
//    };

    List<Character> analyzed;
    List<Character> analyzing;
    private List<String> tokensResult;
    private Map<String, String[]> tableResult;

    public LexicalAnalysis(String filename) {
        String content = readFromFile(filename) + '\n';
        analyzing = content.chars().mapToObj(e -> (char)e).collect(Collectors.toList());
        analyzed = new ArrayList<>();
        tokensResult = new ArrayList<>();
        tableResult = new HashMap<>();
        analyze();
    }

    public void printResult() {
//        for (String token: tokensResult) {
//            System.out.println(token);
//        }
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
                char input = check();

                if (input == ' ' || input == '\t' || input == '\n') {
                    skipCheck();
                } else if (Character.isLetter(input)) {
                    state = 1;
                } else if (Character.isDigit(input)) {
                    state = 2;
                } else if (SINGLE_OP.contains(input)) {
                    state = 3;
                } else if (SE.contains(input)) {
                    state = 4;
                } else {
                    System.out.println("ERROR: " + input);
                }
            } else if (state == 1) {
                // state 1: 1 letter before
                char input = check();

                if (Character.isLetter(input) || Character.isDigit(input) || input == '_') {
                    state = 1;
                } else {
                    state = 5;
                    undoCheck();
                }
            } else if (state == 2) {
                // state 2: 1 digit before
                char input = check();

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
                char input = check();

                if (!SINGLE_OP.contains(input)) {
                    undoCheck();
                }
                String token = getStringFromList(analyzed);
                if (OP.contains(token)) {
                    finishCheck(token, "OP", "_");
                    state = 0;
                } else {
                    state = 9;
                    undoCheck();
                }
            } else if (state == 4) {
                // state 4: se
                String token = getStringFromList(analyzed);
                finishCheck(token, "SE", "_");
                state = 0;
            } else if (state == 5) {
                // state 5: keyword or idn
                String token = getStringFromList(analyzed);
                if (KEYWORDS.contains(token)) {
                    finishCheck(token, token.toUpperCase(), "_");
                } else {
                    finishCheck(token, "IDN", token);
                }
                state = 0;
            } else if (state == 6) {
                // state 6: int .
                char input = check();

                if (Character.isDigit(input)) {
                    state = 6;
                } else {
                    state = 8;
                    undoCheck();
                }
            } else if (state == 7) {
                // state 7: int
                String token = getStringFromList(analyzed);
                finishCheck(token, "INT", token);
                state = 0;
            } else if (state == 8) {
                // state 8: float
                String token = getStringFromList(analyzed);
                finishCheck(token, "FLOAT", token);
                state = 0;
            } else if (state == 9) {
                // state 9: single op
                String token = getStringFromList(analyzed);
                finishCheck(token, "OP", "_");
                state = 0;
            }
        }
//            if (content.charAt(index) == ' ' || content.charAt(index) == '\n') {
//                index++;
//                continue;
//            }
//            if (Character.isLetter(content.charAt(index))) {
//                int beginIndex = index;
//                int endIndex = ++index;
//                for (endIndex = index; Character.isLetter(content.charAt(index)); index++) {
//
//                }
//                String buffer = content.substring(beginIndex, endIndex);
//                if (KEYWORDS.contains(buffer)) {
//                    Map<String, String> token = new HashMap<>();
//                    token.put(buffer.toUpperCase(), "");
//                    result.add(token);
//                }
//            }
//        }
    }

    private char check() {
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

    private void finishCheck(String token, String type, String value) {
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

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
    private static final List<Character> SINGLE_OP = new ArrayList<>(Arrays.asList(
            '+', '-', '*', '/', '%', '=', '>', '<', '!', '&', '|'
    ));

    private final List<Character> analyzed;
    private final List<Character> analyzing;
    private final List<String> preTokensResult;
    private final Map<String, String[]> tableResult;

    public LexicalAnalysis(String filename) {
        String content = readFromFile(filename) + '\n';
        analyzing = content.chars().mapToObj(e -> (char)e).collect(Collectors.toList());
        analyzed = new ArrayList<>();
        preTokensResult = new ArrayList<>();
        tableResult = new HashMap<>();
        analyze();
    }

    public void printResult() {
        System.out.println("# 词素序列");
        System.out.println(preTokensResult.toString());
//        System.out.println("# 词法单元 token 序列");
//        System.out.println(getTokensResult().toString());
//        System.out.println("# 符号表");
        System.out.println("# 词法单元 token 序列");
        for (String token: preTokensResult) {
            String output = "";
            output += token + "\t<";
            String[] map = tableResult.get(token);
            output += map[0] + "," + map[1] + ">";
            System.out.println(output);
        }
    }

    public List<String> getTokensResult() {
        List<String> result = new ArrayList<>();
        for (String pre: preTokensResult) {
            String[] reals = tableResult.get(pre);
            String real = reals[0];
            if ("IDN".equals(real) || "CHAR".equals(real) || "STR".equals(real)) {
                result.add(real);
            } else if ("CONST".equals(real)) {
                List<String> splits = new ArrayList<>(Arrays.asList(reals[1].split("")));
                if (splits.contains(".")) {
                    result.add("FLOAT");
                } else {
                    result.add("INT");
                }
            } else {
                result.add(pre);
            }
        }
        return result;
    }

    private void analyze() {
        int state = 0;

        while (!analyzing.isEmpty()) {
            if (state == 0) {
                // state 0: initial state
                char input = doCheck();

                if (input == ' ' || input == '\t' || input == '\n' || input == '\r') {
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
                    skipCheck();
                    System.out.println("Unrecognized symbol: " + input);
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
                // 现在有一个运算符和一个未知字符
                if (!SINGLE_OP.contains(input)) {
                    undoCheck();
                }
                // 如果第二个字符不在 SINGLE_OP 里，那么肯定最多也就是一个单字符的运算符
                String token = getStringFromList(analyzed);
                // token 是可疑的单字符运算符，或者是可疑的双字符运算符
                if (OP.contains(token)) {
                    // 成功了
                    state = 9;
                } else {
                    // 失败了
                    if (token.length() == 2) {
                        // 双字符失败了，然后判断是不是单字符
                        undoCheck();
                        token = getStringFromList(analyzed);
                        if (OP.contains(token)) {
                            state = 9;
                        } else {
                            // 单字符失败了
                            skipCheck();
                            System.out.println("Unrecognized symbol: " + token);
                            state = 0;
                        }
                    } else {
                        // 单字符失败了
                        skipCheck();
                        System.out.println("Unrecognized symbol: " + token);
                        state = 0;
                    }
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
                    state = 0;
                } else {
                    state = 8;
                }
            } else if (state == 6) {
                // state 6: int .
                char input = doCheck();

                if (Character.isDigit(input)) {
                    state = 6;
                } else {
                    undoCheck();
                    undoCheck();
                    char nextChar = doCheck();
                    state = 7;
                    if (nextChar == '.') {
                        undoCheck();
                    }
                }
            } else if (state == 7) {
                // state 7: int or float
                finishCheck("CONST");
                state = 0;
            } else if (state == 8) {
                finishCheck("IDN");
                state = 0;
            } else if (state == 9) {
                // state 9: op
                finishCheck("OP");
                state = 0;
            } else if (state == 10) {
                // state 10: char?
                char input = doCheck();

                if (input == '\'') {
                    state = 12;
                } else {
                    state = 10;
                }
            } else if (state == 11) {
                // state 11: string?
                char input = doCheck();

                if (input == '\"') {
                    state = 13;
                } else {
                    state = 11;
                }
            } else if (state == 12) {
                // state 12: char
                finishCheck("CHAR");
                state = 0;
            } else if (state == 13) {
                // state 13: string
                finishCheck("STR");
                state = 0;
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
        preTokensResult.add(token);
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

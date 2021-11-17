import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.*;

/**
 * @author super
 */
public class SyntaxAnalysis {
    private List<String> TERMINAL_SYMBOLS = new ArrayList<>(Arrays.asList(
            "while", "for", "continue", "break", "if", "else", "float", "int", "char", "void", "return",
            "+", "-", "*", "/", "%", "=", ">", "<", "==", "<=", ">=", "!=", "++", "--", "&&", "||", "+=", "-=", "*=", "/=", "%=",
            "(", ")", "{", "}", ";", ",", "[", "]",
            "IDN", "INT", "FLOAT", "CHAR", "STR", "$"
    ));
    private List<String> notTerminalSymbols = new ArrayList<>();
    private Map<String, List<String[]>> syntax;
    private Map<String, List<String>> firstSet;
    private Map<String, List<String>> followSet;

    public SyntaxAnalysis(String filename) {
        syntax = readSyntax(filename);
        firstSet = getFirstSet(syntax);
        followSet = getFollowSet(syntax, firstSet);
    }

    public void printFirst() {
        System.out.println("FIRST:");
        for (String left: notTerminalSymbols) {
            System.out.print(left + ": ");
            List<String> rights = firstSet.get(left);
            for (int i = 0; i < rights.size(); i++) {
                System.out.print(rights.get(i));
                if (i != rights.size() - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    public void printFollow() {
        System.out.println("FOLLOW:");
        for (String left: notTerminalSymbols) {
            System.out.print(left + ": ");
            List<String> rights = followSet.get(left);
            for (int i = 0; i < rights.size(); i++) {
                System.out.print(rights.get(i));
                if (i != rights.size() - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    private Map<String, List<String[]>> readSyntax(String filename) {
        Map<String, List<String[]>> result = new HashMap<>();
        String line;
        Reader reader = null;
        try {
            reader = new FileReader(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (reader == null) {
            return null;
        }
        LineNumberReader lineReader = new LineNumberReader(reader);
        try {
            while (true) {
                line = lineReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] lineList = line.split(" ");
                String left = lineList[0];
                String[] rights = Arrays.copyOfRange(lineList, 2, lineList.length);
                if (!result.containsKey(left)) {
                    notTerminalSymbols.add(left);
                    result.put(left, new ArrayList<>());
                }
                result.get(left).add(rights);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Map<String, List<String>> getFirstSet(Map<String, List<String[]>> syntax) {
        Map<String, List<String>> result = new HashMap<>();
        for (String notTerminalSymbol: notTerminalSymbols) {
            result.put(notTerminalSymbol, new ArrayList<>());
        }
        boolean firstSetChanged = true;
        while (firstSetChanged) {
            firstSetChanged = false;
            for(String currentLeft: notTerminalSymbols) {
                // 对于每个产生式的左边
                for (String[] currentRights: syntax.get(currentLeft)) {
                    // 对于每个产生式的右边，判断第一个符号是不是终结符
                    if (TERMINAL_SYMBOLS.contains(currentRights[0])) {
                        // 是终结符，直接加到first集合
                        if (!result.get(currentLeft).contains(currentRights[0])) {
                            result.get(currentLeft).add(currentRights[0]);
                            firstSetChanged = true;
                        }
                    } else {
                        // 是非终结符，依次判断这个串前面若干个非终结符的first集是否有空，直到遇到终结符
                        for (String currentRight: currentRights) {
                            if (notTerminalSymbols.contains(currentRight)) {
                                // 是非终结符，继续判断其first集合是否有空
                                if (result.get(currentRight).contains("$")) {
                                    // 是非终结符，且first集有空，把first集非空元素加入，继续循环
                                    for (String element: result.get(currentRight)) {
                                        if (!"$".equals(element) && !result.get(currentLeft).contains(element)) {
                                            result.get(currentLeft).add(element);
                                            firstSetChanged = true;
                                        }
                                    }
                                } else {
                                    // 是非终结符，且first集不含空，把first集元素加入，停止循环
                                    for (String element: result.get(currentRight)) {
                                        if (!result.get(currentLeft).contains(element)) {
                                            result.get(currentLeft).add(element);
                                            firstSetChanged = true;
                                        }
                                    }
                                    break;
                                }
                            } else {
                                // 是终结符，停止循环
                                if (!result.get(currentLeft).contains(currentRight)) {
                                    result.get(currentLeft).add(currentRight);
                                    firstSetChanged = true;
                                }
                                // 此处break不应放在上面if里面，否则不会停止
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private Map<String, List<String>> getFollowSet(Map<String, List<String[]>> syntax, Map<String, List<String>> firstSet) {
        Map<String, List<String>> result = new HashMap<>();
        for (String notTerminalSymbol: notTerminalSymbols) {
            result.put(notTerminalSymbol, new ArrayList<>());
        }
        // 开始符号添加#
        result.get(notTerminalSymbols.get(0)).add("#");
        boolean followSetChanged = true;
        while (followSetChanged) {
            followSetChanged = false;
            for (String currentLeft: notTerminalSymbols) {
                // 对于每个产生式的左边
                for (String[] currentRights: syntax.get(currentLeft)) {
                    // 对于每个产生式的右边
                    for (int i = 0; i < currentRights.length; i++) {
                        // 对于每个产生式右边的每一个符号
                        if (notTerminalSymbols.contains(currentRights[i])) {
                            // 是非终结符，判断
                            if (i < currentRights.length - 1) {
                                // 没到最后，是非终结符
                                String nextRight = currentRights[i + 1];
                                if (notTerminalSymbols.contains(nextRight)) {
                                    // 下一个还是非终结符，遍历它的first
                                    for (String nextRightFirstElement: firstSet.get(nextRight)) {
                                        if (!result.get(currentRights[i]).contains(nextRightFirstElement) && !"$".equals(nextRightFirstElement)) {
                                            // 把非空的加到FOLLOW里
                                            result.get(currentRights[i]).add(nextRightFirstElement);
                                            followSetChanged = true;
                                        } else if ((i + 1 == currentRights.length - 1) && "$".equals(nextRightFirstElement)) {
                                            // 下一个是最后一个，而且有空，把FOLLOW(left)加到FOLLOW(right)中
                                            for (String element: result.get(currentLeft)) {
                                                if (!result.get(currentRights[i]).contains(element)) {
                                                    result.get(currentRights[i]).add(element);
                                                    followSetChanged = true;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // 下一个是终结符
                                    if (!result.get(currentRights[i]).contains(nextRight)) {
                                        result.get(currentRights[i]).add(nextRight);
                                        followSetChanged = true;
                                    }
                                }
                            } else {
                                // 到最后了，是非终结符，把FOLLOW(left)加到FOLLOW(right)中
                                for (String element: result.get(currentLeft)) {
                                    if (!result.get(currentRights[i]).contains(element)) {
                                        result.get(currentRights[i]).add(element);
                                        followSetChanged = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}

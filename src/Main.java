/**
 * @author super
 */
public class Main {
    public static void main(String[] argc) {
        LexicalAnalysis lexicalAnalysis = new LexicalAnalysis("./test/test3.c");
        lexicalAnalysis.printResult();
        SyntaxAnalysis syntaxAnalysis = new SyntaxAnalysis("./src/syntax.txt", lexicalAnalysis.getTokensResult());
        System.out.println("# FIRST 集合");
        syntaxAnalysis.printFirst();
        System.out.println("# FOLLOW 集合");
        syntaxAnalysis.printFollow();
        System.out.println("# 预测分析表");
        syntaxAnalysis.printTable();
        System.out.println("# 规约序列");
        syntaxAnalysis.printPredict("S");
    }
}

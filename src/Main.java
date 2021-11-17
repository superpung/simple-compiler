/**
 * @author super
 */
public class Main {
    public static void main(String[] argc) {
        LexicalAnalysis lexicalAnalysis = new LexicalAnalysis("./test/test3.c");
        lexicalAnalysis.printResult();
        SyntaxAnalysis syntaxAnalysis = new SyntaxAnalysis("./src/syntax.txt", lexicalAnalysis.getTokensResult());
//        syntaxAnalysis.printFirst();
//        syntaxAnalysis.printFollow();
        syntaxAnalysis.printTable();
        syntaxAnalysis.printPredict("S");
    }
}

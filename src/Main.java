/**
 * @author super
 */
public class Main {
    public static void main(String[] argc) {
        LexicalAnalysis lexicalAnalysis = new LexicalAnalysis("./test/test5.c");
//        lexicalAnalysis.printResult();
        SyntaxAnalysis syntaxAnalysis = new SyntaxAnalysis("./src/syntax.txt");
//        syntaxAnalysis.printFirst();
        syntaxAnalysis.printFollow();
    }
}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CompilationEngine {
    JackTokenizer jt;
    List<String> xml = new ArrayList<>();
    String classname;

    public CompilationEngine(File in, File out, File outT) {
        this.jt = new JackTokenizer(in, outT);
        compileClass();
        produceXML(out);
    }

    void compileClass() {
        write("<class>");

        keyword();
        classname = jt.id();
        id();
        symbol();
        compileClassVarDec();
        while (!(jt.tokenType() == TokenType.SYMBOL && jt.symbol() == '}'))
            compileSubroutineDec();
        symbol();

        write("</class>");
    }

    private void compileSubroutineDec() {
        write("<subroutineDec>");

        keyword();
        type();
        id();
        symbol();
        compileParameterList();
        symbol();
        compileSubroutineBody();

        write("</subroutineDec>");
    }

    private void compileSubroutineBody() {
        write("<subroutineBody>");

        symbol();
        compileVarDec();
        compileStatements();
        symbol();

        write("</subroutineBody>");
    }

    private void compileStatements() {
        write("<statements>");

        while (!(jt.tokenType() == TokenType.SYMBOL && jt.symbol() == '}')) {
            switch (jt.keyword()) {
                case "let" -> compileLetStatement();
                case "if" -> compileIfStatement();
                case "while" -> compileWhileStatement();
                case "do" -> compileDoStatement();
                case "return" -> compileReturnStatement();
                default -> throw new IllegalArgumentException();
            }
        }

        write("</statements>");
    }

    private void compileReturnStatement() {
        write("<returnStatement>");

        keyword();
        if (!(jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ';'))
            compileExpression();
        symbol();

        write("</returnStatement>");
    }

    private void compileDoStatement() {
        write("<doStatement>");

        keyword();
        id();
        if (jt.symbol() == '.') {
            symbol();
            id();
        }
        symbol();
        compileExpressionList();
        symbol();
        symbol();

        write("</doStatement>");
    }

    private void compileWhileStatement() {
        write("<whileStatement>");

        keyword();
        symbol();
        compileExpression();
        symbol();
        symbol();
        compileStatements();
        symbol();

        write("</whileStatement>");
    }

    private void compileIfStatement() {
        write("<ifStatement>");

        keyword();
        symbol();
        compileExpression();
        symbol();
        symbol();
        compileStatements();
        symbol();
        if (jt.tokenType() == TokenType.KEYWORD && jt.keyword().equals("else")) {
            keyword();
            symbol();
            compileStatements();
            symbol();
        }

        write("</ifStatement>");
    }

    private void compileLetStatement() {
        write("<letStatement>");

        keyword();
        id();
        if (jt.symbol() == '[') {
            symbol();
            compileExpression();
            symbol();
        }
        symbol();
        compileExpression();
        symbol();

        write("</letStatement>");
    }

    private void compileExpression() {
        write("<expression>");

        compileTerm();
        while (jt.tokenType() == TokenType.SYMBOL && isOperator(jt.symbol())) {
            symbol();
            compileTerm();
        }

        write("</expression>");
    }

    private void compileTerm() {
        write("<term>");

        switch (jt.tokenType()) {
            case INTCONST -> intConstant();
            case STRINGCONST -> stringConstant();
            case KEYWORD -> keywordTerm();
            case ID -> idTerm();
            case SYMBOL -> symbolTerm();

        }

        write("</term>");
    }

    private void compileExpressionList() {
        while (!(jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ')')) {
            if (jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ',') symbol();
            compileExpression();
        }
    }


    private void compileVarDec() {
        while (jt.tokenType() == TokenType.KEYWORD && jt.keyword().equals("var")) {
            keyword();
            type();
            varNames();
        }
    }

    private void compileParameterList() {
        write("<parameterList>");

        while (!(jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ')')) {
            if (jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ',') symbol();
            type();
            id();
        }

        write("</parameterList>");
    }

    private void compileClassVarDec() {
        while (jt.tokenType() == TokenType.KEYWORD && (jt.keyword().equals("static") || jt.keyword().equals("field"))) {
            keyword();
            type();
            varNames();
        }
    }

    // term type methods
    private void keywordTerm() {
        if (isKeywordConstant(jt.keyword())) keyword();
    }

    private void idTerm() {
        id();

        if (jt.tokenType() != TokenType.SYMBOL) return;

        switch (jt.symbol()) {
            case '[' -> {
                symbol();
                compileExpression();
                symbol();
            }
            case '(' -> {
                symbol();
                compileExpressionList();
                symbol();
            }
            case '.' -> {
                symbol();
                id();
                symbol();
                compileExpressionList();
                symbol();
            }
        }
    }

    private void symbolTerm() {
        if (jt.symbol() == '(') {
            symbol();
            compileExpression();
            symbol();
        } else if (isUnaryOperator(jt.symbol())) {
            symbol();
            compileTerm();
        }
    }

    // bool methods for terms
    private boolean isOperator(char symbol) {
        if (jt.tokenType() != TokenType.SYMBOL)
            return false;

        return switch (symbol) {
            case '+', '-', '*', '/', '&', '|', '<', '>', '=' -> true;
            default -> false;
        };
    }

    private boolean isUnaryOperator(char symbol) {
        if (jt.tokenType() != TokenType.SYMBOL)
            return false;

        return switch (symbol) {
            case '-', '~' -> true;
            default -> false;
        };
    }

    private boolean isKeywordConstant(String keyword) {
        if (jt.tokenType() != TokenType.KEYWORD)
            return false;

        return switch (keyword) {
            case "true", "false", "null", "this" -> true;
            default -> false;
        };
    }


    // help methods for long terms
    private void varNames() {
        id();
        while (jt.symbol() == ',') {
            symbol();
            id();
        }
        symbol();
    }

    private void type() {
        switch (jt.tokenType()) {
            case KEYWORD -> {
                switch (jt.keyword()) {
                    case "int", "char", "boolean", "void" -> keyword();
                    default -> throw new IllegalArgumentException();
                }
            }
            case ID -> id();
            default -> throw new IllegalArgumentException();
        }
    }

    // atomic methods for constants
    private void symbol() {
        if (jt.tokenType() != TokenType.SYMBOL) throw new IllegalArgumentException();
        write("<symbol>").write(jt.symbol()).write("</symbol>");
        jt.next();
    }

    private void id() {
        if (jt.tokenType() != TokenType.ID) throw new IllegalArgumentException();
        write("<identifier>").write(jt.id()).write("</identifier>");
        jt.next();
    }

    private void keyword() {
        if (jt.tokenType() != TokenType.KEYWORD) throw new IllegalArgumentException();
        write("<keyword>").write(jt.keyword()).write("</keyword>");
        jt.next();
    }

    private void intConstant() {
        if (jt.tokenType() != TokenType.INTCONST) throw new IllegalArgumentException();
        write("<integerConstant>").write(jt.intval()).write("</integerConstant>");
        jt.next();
    }

    private void stringConstant() {
        if (jt.tokenType() != TokenType.STRINGCONST) throw new IllegalArgumentException();
        write("<stringConstant>").write(jt.stringval()).write("</stringConstant>");
        jt.next();
    }

    // write to xml string
    CompilationEngine write(String s) {
        xml.add(s);
        return this;
    }

    CompilationEngine write(char c) {
        xml.add(String.valueOf(c));
        return this;
    }

    CompilationEngine write(int i) {
        xml.add(String.valueOf(i));
        return this;
    }

    private void produceXML(File out) {
        PrintWriter pw = null;
        try {pw = new PrintWriter(out);} catch (FileNotFoundException e) {}

        for (int i = 0; i < xml.size(); i++) {
            String s = xml.get(i);
            if (isTerminal(s))
                pw.println(s + ' ' + jt.represantation(xml.get(++i)) + ' ' + xml.get(++i));
            else
                pw.println(s);
        }

        pw.close();
    }

    private boolean isTerminal(String s) {
        return switch (s) {
            case "<keyword>", "<symbol>", "<identifier>", "<integerConstant>", "<stringConstant>" -> true;
            default -> false;
        };
    }
}

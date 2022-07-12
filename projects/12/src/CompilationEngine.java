import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CompilationEngine {
    JackTokenizer jt;
    SymbolTable st;
    VMWriter vm;

    int whileCount;
    int ifCount;

    int inArrayExpression;
    boolean inAssignee = false;             // in a variable that sth will be assigned to?

    List<String> xml = new ArrayList<>();

    private String classname;
    private String currMethType;
    private String currMethName;

    public CompilationEngine(String file) {
        jt = new JackTokenizer(new File(file + ".jack"), new File(file + "T.xml"));
        vm = new VMWriter(new File(file + ".vm"));
        st = new SymbolTable();

        whileCount = 0;
        ifCount = 0;

        compileClass();
        produceXML(new File(file + ".xml"));

        vm.close();
    }

    void compileClass() {
        write("<class>");

        keyword();
        classname = id();
        symbol();
        compileClassVarDec();
        while (!(jt.tokenType() == TokenType.SYMBOL && jt.symbol() == '}'))
            compileSubroutineDec();
        symbol();

        write("</class>");
    }

    private void compileSubroutineDec() {
        write("<subroutineDec>");

        st.startSubroutine();

        currMethType = keyword();
        type();
        currMethName = id();
        symbol();
        if (currMethType.equals("method")) st.implicitarg();
        compileParameterList();
        symbol();
        compileSubroutineBody();

        write("</subroutineDec>");
    }

    private void compileSubroutineBody() {
        write("<subroutineBody>");

        symbol();
        compileVarDec();

        writeMethodHead();

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

        if (!(jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ';')) compileExpression();
        else vm.writePush("constant", 0);
        vm.writeReturn();

        symbol();

        write("</returnStatement>");
    }

    private void compileDoStatement() {
        write("<doStatement>");

        keyword();
        idTerm();
        symbol();

        vm.writePop("temp", 0);

        write("</doStatement>");
    }

    private void compileWhileStatement() {
        write("<whileStatement>");

        int tempWhile = whileCount++;

        keyword();

        vm.writeLabel("WHILE_CONTROL" + tempWhile);

        symbol();
        compileExpression();
        symbol();

        vm.writeArithmetic("not");
        vm.writeIf("WHILE_END" + tempWhile);

        symbol();
        compileStatements();
        symbol();

        vm.writeGoto("WHILE_CONTROL" + tempWhile);
        vm.writeLabel("WHILE_END" + tempWhile);

        write("</whileStatement>");
    }

    private void compileIfStatement() {
        write("<ifStatement>");

        int tempIf = ifCount++;

        keyword();
        symbol();
        compileExpression();
        symbol();

        vm.writeArithmetic("not");
        vm.writeIf("IF_FALSE" + tempIf);

        symbol();
        compileStatements();
        symbol();

        vm.writeGoto("IF_END" + tempIf);
        vm.writeLabel("IF_FALSE" + tempIf);

        if (jt.tokenType() == TokenType.KEYWORD && jt.keyword().equals("else")) {
            keyword();
            symbol();
            compileStatements();
            symbol();
        }

        vm.writeLabel("IF_END" + tempIf);

        write("</ifStatement>");
    }

    private void compileLetStatement() {
        write("<letStatement>");

        keyword();

        inAssignee = true;
        String var = id();
        if (jt.symbol() == '[') {
            vm.writePush(st.kindOf(var), st.indexOf(var));
            inArrayExpression++;

            symbol();
            compileExpression();

            inArrayExpression--;
            vm.writeArithmetic("add");
            inAssignee = false;

            symbol();
            symbol();
            compileExpression();
            symbol();

            vm.writePop("temp", 0);
            vm.writePop("pointer", 1);
            vm.writePush("temp", 0);
            vm.writePop("that", 0);
        } else {
            inAssignee = false;

            symbol();
            compileExpression();
            symbol();

            vm.writePop(st.kindOf(var), st.indexOf(var));
        }

        write("</letStatement>");
    }

    private void compileExpression() {
        write("<expression>");

        compileTerm();
        while (jt.tokenType() == TokenType.SYMBOL && isOperator(jt.symbol())) {
            char c = symbol();
            compileTerm();
            writeOperator(c);
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

    private int compileExpressionList() {
        int i = 0;

        while (!(jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ')')) {
            if (jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ',') symbol();
            compileExpression();
            i++;
        }

        return i;
    }

    private void compileVarDec() {
        while (jt.tokenType() == TokenType.KEYWORD && jt.keyword().equals("var")) {
            keyword();
            String type = type();
            varNames().forEach(var -> st.define(var, type, Kind.VAR));
        }
    }

    private void compileParameterList() {
        write("<parameterList>");

        while (!(jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ')')) {
            if (jt.tokenType() == TokenType.SYMBOL && jt.symbol() == ',') symbol();
            String type = type();
            st.define(id(), type, Kind.ARG);
        }

        write("</parameterList>");
    }

    private void compileClassVarDec() {
        while (jt.tokenType() == TokenType.KEYWORD && (jt.keyword().equals("static") || jt.keyword().equals("field"))) {
            Kind kind = Kind.of(keyword());
            String type = type();
            varNames().forEach(var -> st.define(var, type, kind));
        }
    }

    // term type methods
    private void keywordTerm() {
        if (!isKeywordConstant(jt.keyword())) return;

        String keyword = keyword();

        writeKeywordTerm(keyword);
    }

    private void idTerm() {
        String id = id();

        if (jt.tokenType() == TokenType.SYMBOL)
            switch (jt.symbol()) {
                case '[' -> {
                    vm.writePush(st.kindOf(id), st.indexOf(id));
                    inArrayExpression++;

                    symbol();
                    compileExpression();
                    symbol();

                    inArrayExpression--;
                    vm.writeArithmetic("add");
                    if (inArrayExpression > 0 || inArrayExpression == 0 && !inAssignee) {
                        vm.writePop("pointer", 1);
                        vm.writePush("that", 0);
                    }
                }
                case '(' -> {
                    vm.writePush("pointer", 0);

                    symbol();
                    int nArgs = compileExpressionList();
                    symbol();

                    vm.writeCall(classname + "." + id, nArgs + 1);
                }
                case '.' -> {
                    symbol();
                    String id2 = id();
                    symbol();

                    if (st.indexOf(id) >= 0) vm.writePush(st.kindOf(id), st.indexOf(id));

                    int nArgs = compileExpressionList();
                    symbol();

                    if (st.indexOf(id) >= 0) vm.writeCall(st.typeOf(id) + "." + id2, nArgs + 1);
                    else vm.writeCall(id + "." + id2, nArgs);
                }
                default -> vm.writePush(st.kindOf(id), st.indexOf(id));
            }
        else
            vm.writePush(st.kindOf(id), st.indexOf(id));
    }

    private void symbolTerm() {
        if (jt.symbol() == '(') {
            symbol();
            compileExpression();
            symbol();
        } else if (isUnaryOperator(jt.symbol())) {
            char c = symbol();
            compileTerm();

            writeUnary(c);
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
    private List<String> varNames() {
        List<String> varNames = new ArrayList<>();

        varNames.add(id());
        while (jt.symbol() == ',') {
            symbol();
            varNames.add(id());
        }
        symbol();

        return varNames;
    }

    private String type() {
        switch (jt.tokenType()) {
            case KEYWORD -> {
                return switch (jt.keyword()) {
                    case "int", "char", "boolean", "void" -> keyword();
                    default -> throw new IllegalArgumentException();
                };
            }
            case ID -> {return id();}
            default -> throw new IllegalArgumentException();
        }
    }

    // atomic methods for constants
    private char symbol() {
        if (jt.tokenType() != TokenType.SYMBOL) throw new IllegalArgumentException();

        char s = jt.symbol();
        write("<symbol>").write(s).write("</symbol>");

        jt.next();
        return s;
    }

    private String id() {
        if (jt.tokenType() != TokenType.ID) throw new IllegalArgumentException();

        String id = jt.id();
        write("<identifier>").write(id).write("</identifier>");

        jt.next();
        return id;
    }

    private String keyword() {
        if (jt.tokenType() != TokenType.KEYWORD) throw new IllegalArgumentException();

        String keyword = jt.keyword();
        write("<keyword>").write(keyword).write("</keyword>");

        jt.next();
        return keyword;
    }

    private int intConstant() {
        if (jt.tokenType() != TokenType.INTCONST) throw new IllegalArgumentException();

        int intval = jt.intval();
        write("<integerConstant>").write(intval).write("</integerConstant>");

        vm.writePush("constant", intval);

        jt.next();
        return intval;
    }

    private String stringConstant() {
        if (jt.tokenType() != TokenType.STRINGCONST) throw new IllegalArgumentException();

        String stringval = jt.stringval();
        write("<stringConstant>").write(stringval).write("</stringConstant>");

        vm.writePush("constant", stringval.length());
        vm.writeCall("String.new", 1);
        for (int i = 0; i < stringval.length(); i++) {
            vm.writePush("constant", stringval.charAt(i));
            vm.writeCall("String.appendChar", 2);
        }

        jt.next();
        return stringval;
    }

    // write VM code
    private void writeMethodHead() {
        vm.writeFunction(classname + "." + currMethName, st.varCount(Kind.VAR));
        switch (currMethType) {
            case "constructor" -> {
                vm.writePush("constant", st.varCount(Kind.FIELD));
                vm.writeCall("Memory.alloc", 1);
            }
            case "method" -> vm.writePush("argument", 0);
            default -> {return;}
        }
        vm.writePop("pointer", 0);
    }

    private void writeOperator(char op) {
        switch (op) {
            case '+' -> vm.writeArithmetic("add");
            case '-' -> vm.writeArithmetic("sub");
            case '*' -> vm.writeCall("Math.multiply", 2);
            case '/' -> vm.writeCall("Math.divide", 2);
            case '&' -> vm.writeArithmetic("and");
            case '|' -> vm.writeArithmetic("or");
            case '<' -> vm.writeArithmetic("lt");
            case '>' -> vm.writeArithmetic("gt");
            case '=' -> vm.writeArithmetic("eq");
        }
    }

    private void writeKeywordTerm(String keyword) {
        switch (keyword) {
            case "null", "false" -> vm.writePush("constant", 0);
            case "true" -> {
                vm.writePush("constant", 0);
                vm.writeArithmetic("not");
            }
            default -> vm.writePush("pointer", 0);
        }
    }

    private void writeUnary(char c) {
        switch (c) {
            case '-' -> vm.writeArithmetic("neg");
            case '~' -> vm.writeArithmetic("not");
        }
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

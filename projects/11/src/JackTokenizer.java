import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class JackTokenizer {
    private ArrayList<String> tokens;
    private int current;
    String currentToken;
    private String symbols;
    private List<String> keywords;

    public JackTokenizer(File input, File output) {
        init();
        try {start(input, output);} catch (IOException ignored) {}
        reset();
    }

    private void start(File input, File output) throws IOException {
        List<String> lines = removeWhitespace(input);
        tokenize(lines);
        produceXML(output);
    }

    boolean hasNext() {
        return current < tokens.size() - 1;
    }

    boolean next() {
        if (current < tokens.size() - 1) {
            currentToken = tokens.get(++current);
            return true;
        }
        return false;
    }

    void reset() {
        current = -1;
        if (hasNext()) {
            current = 0;
            currentToken = tokens.get(current);
        }
    }

    TokenType tokenType() {
        return tokenType(currentToken);
    }

    TokenType tokenType(String token) {
        if (symbols.contains(token)) return TokenType.SYMBOL;
        if (keywords.contains(token)) return TokenType.KEYWORD;
        if (token.startsWith("\"")) return TokenType.STRINGCONST;
        if (isInt(token)) return TokenType.INTCONST;
        else return TokenType.ID;
    }

    private boolean isInt(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    String keyword() {
        if (tokenType() == TokenType.KEYWORD)
            return tokens.get(current);
        throw new IllegalArgumentException();
    }

    char symbol() {
        if (tokenType() == TokenType.SYMBOL)
            return tokens.get(current).charAt(0);
        throw new IllegalArgumentException();
    }

    String id() {
        if (tokenType() == TokenType.ID)
            return tokens.get(current);
        throw new IllegalArgumentException();
    }

    int intval() {
        if (tokenType() == TokenType.INTCONST)
            return Integer.parseInt(tokens.get(current));
        throw new IllegalArgumentException();
    }

    String stringval() {
        if (tokenType() == TokenType.STRINGCONST)
            return tokens.get(current).substring(1, tokens.get(current).length() - 1);
        throw new IllegalArgumentException();
    }

    private void tokenize(List<String> lines) {
        int stringCount = 0;
        lines.forEach(curr -> tokens.addAll(split(curr, symbols + " ", true)));
    }

    private List<String> removeWhitespace(File input) throws IOException {
        List<String> res = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String curr;
        boolean inMultiLine = false;

        while ((curr = reader.readLine()) != null) {
            int inLine = curr.indexOf("//");
            int multiLine = curr.indexOf("/*");
            int multiLineEnd;

            if (inMultiLine) {
                if ((multiLineEnd = curr.indexOf("*/")) >= 0) {
                    curr = curr.substring(multiLineEnd + 2);
                    inMultiLine = false;
                } else continue;
            }

            if ((inLine >= 0 && multiLine < 0) || (inLine - multiLine > 0)) curr = curr.substring(0, inLine);
            else if ((inLine < 0 && multiLine >= 0) || (inLine - multiLine < 0)) {
                if ((multiLineEnd = curr.indexOf("*/")) >= 0)
                    curr = curr.substring(multiLineEnd + 2);
                else {
                    inMultiLine = true;
                    curr = curr.substring(0, multiLine);
                }
            }

            if (!curr.isBlank()) res.add(curr.trim());
        }

        return res;
    }

    private void init() {
        symbols = "{}()[].,;+-*/&|<>=~";
        keywords = List.of("class", "constructor", "function", "method", "field", "static", "var", "int",
                "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while",
                "return");
        tokens = new ArrayList<String>();
    }

    private void produceXML(File output) {
        PrintWriter pw = null;

        try {pw = new PrintWriter(output);} catch (FileNotFoundException ignored) {}

        pw.println("<tokens>");
        for (String token : tokens) {
            String type = String.valueOf(tokenType(token));
            pw.println(String.format("<%s> %s </%s>", type, represantation(token), type));
        }
        pw.println("</tokens>");

        pw.close();
    }

    String represantation(String word) {
        return switch (word) {
            case ">" -> "&gt;";
            case "<" -> "&lt;";
            case "&" -> "&amp;";
            case "\"" -> "&quot;";
            default -> word;
        };
    }

    public static List<String> split(String line, String delim, boolean includeDelim) {
        List<String> tokens = new ArrayList<String>();
        StringTokenizer parser = new StringTokenizer(line, delim, includeDelim);

        while (parser.hasMoreTokens()) {
            StringBuilder token = new StringBuilder(parser.nextToken());
            if (token.toString().startsWith("\"") && (!token.toString().endsWith("\"") || token.length() == 1)) {
                String token2 = parser.nextToken();
                while (!token2.endsWith("\"")) {
                    token.append(token2);
                    token2 = parser.nextToken();
                }
                token.append(token2);
            }
            if (!token.toString().isBlank()) tokens.add(token.toString());
        }

        return tokens;
    }
}

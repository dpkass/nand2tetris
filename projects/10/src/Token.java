public class Token {
    private TokenType type;
    private String id;

    public Token(String id, TokenType type) {
        this.id = id;
        this.type = type;
    }

    public TokenType getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}

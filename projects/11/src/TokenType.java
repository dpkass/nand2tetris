public enum TokenType {
    KEYWORD, SYMBOL, INTCONST, STRINGCONST, ID;

    @Override
    public String toString() {
        return switch (this) {
            case KEYWORD -> "keyword";
            case SYMBOL -> "symbol";
            case INTCONST -> "integerConstant";
            case STRINGCONST -> "stringConstant";
            case ID -> "identifier";
        };
    }
}

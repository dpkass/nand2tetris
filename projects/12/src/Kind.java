public enum Kind {
    STATIC, FIELD, ARG, VAR, NONE;

    public static Kind of(String str) {
        return switch (str) {
            case "static" -> STATIC;
            case "field" -> FIELD;
            case "argument" -> ARG;
            case "var" -> VAR;
            default -> throw new IllegalStateException("Unexpected value");
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case STATIC -> "static";
            case FIELD -> "this";
            case ARG -> "argument";
            case VAR -> "local";
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }
}

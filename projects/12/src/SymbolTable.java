import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private int staticCounter;
    private int fieldCounter;
    private int argCounter;
    private int varCounter;

    private final Map<String, Symbol> classScope;
    private final Map<String, Symbol> subroutineScope;

    SymbolTable() {
        classScope = new HashMap<>();
        subroutineScope = new HashMap<>();
        argCounter = varCounter = staticCounter = fieldCounter = 0;
    }

    void startSubroutine() {
        argCounter = varCounter = 0;
        subroutineScope.clear();
    }

    void define(String name, String type, Kind kind) {
        switch (kind) {
            case STATIC -> classScope.put(name, new Symbol(type, kind, staticCounter++));
            case FIELD -> classScope.put(name, new Symbol(type, kind, fieldCounter++));
            case ARG -> subroutineScope.put(name, new Symbol(type, kind, argCounter++));
            case VAR -> subroutineScope.put(name, new Symbol(type, kind, varCounter++));
            default -> throw new IllegalStateException("Unexpected value: " + kind);
        }
    }

    void implicitarg() {
        argCounter++;
    }

    int varCount(Kind kind) {
        return switch (kind) {
            case STATIC -> staticCounter;
            case FIELD -> fieldCounter;
            case ARG -> argCounter;
            case VAR -> varCounter;
            default -> -1;
        };
    }

    String kindOf(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name).kind().toString();
        else if (classScope.containsKey(name)) return classScope.get(name).kind().toString();
        return null;
    }

    String typeOf(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name).type();
        else if (classScope.containsKey(name)) return classScope.get(name).type();
        return null;
    }

    int indexOf(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name).index();
        else if (classScope.containsKey(name)) return classScope.get(name).index();
        return -1;
    }
}

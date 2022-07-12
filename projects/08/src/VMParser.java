import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

// 
// Decompiled by Procyon v0.5.36
// 

public class VMParser
{
    public static final int PUSH = 0;
    public static final int POP = 1;
    public static final int ARITHMETIC = 2;
    public static final int LABEL = 3;
    public static final int GOTO = 4;
    public static final int IF_GOTO = 5;
    public static final int CALL = 6;
    public static final int FUNCTION = 7;
    public static final int RETURN = 8;
    Set<String> arithmeticCommands;
    private int currLine;
    private String[] curr;
    private ArrayList<String> commands;
    
    public VMParser(final File input) throws FileNotFoundException, Exception {
        this.arithmeticCommands = Set.of("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not");
        this.commands = new ArrayList<String>();
        this.fixCommand(input);
        this.currLine = 0;
        if (!this.commands.isEmpty()) this.curr = this.commands.get(this.currLine).split(" ");
    }
    
    public void fixCommand(final File input) throws FileNotFoundException {
        final Scanner s = new Scanner(input);
        while (s.hasNext()) {
            String command = "";
            final String[] split = s.nextLine().split("\\s+");
            for (final String element : split) {
                if (element.startsWith("//")) break;
                command += element + " ";
            }
            if (!command.isBlank()) this.commands.add(command.trim());
        }
        s.close();
    }
    
    public boolean hasNext() {
        return this.currLine < this.commands.size();
    }
    
    public void next() {
        try {
            this.curr = this.commands.get(++this.currLine).split(" ");
        }
        catch (IndexOutOfBoundsException ex) {}
    }
    
    public int type() {
        if (this.arithmeticCommands.contains(this.curr[0])) return ARITHMETIC;
        final String s = this.curr[0];
        return switch (s) {
            case "push" -> 0;
            case "pop" -> 1;
            case "label" -> 3;
            case "goto" -> 4;
            case "if-goto" -> 5;
            case "call" -> 6;
            case "function" -> 7;
            case "return" -> 8;
            default -> -1;
        };
    }
    
    public String firstArg() {
        if (this.type() == 2)
            return this.curr[0];
        return this.curr[1];
    }
    
    public int secondArg() {
        return Integer.parseInt(this.curr[2]);
    }
}

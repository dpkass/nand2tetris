import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

public class VMParser {

    public static final int PUSH = 0;
    public static final int POP = 1;
    public static final int ARITHMETIC = 2;

    Set<String> arithmeticCommands = Set.of("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not");

    private int currLine;
    private String[] curr;
    private ArrayList<String> commands = new ArrayList<String>();

    public VMParser(File input) throws FileNotFoundException, Exception {
        fixCommand(input);
        currLine = 0;
        if (!commands.isEmpty())
            curr = commands.get(currLine).split(" ");
    }

    public void fixCommand(File input) throws FileNotFoundException {
        Scanner s = new Scanner(input);

        while (s.hasNext()) {
            String command = "";
            String[] line = s.nextLine().split(" ");

            for (String element : line) {
                if (element.length() > 1 && element.substring(0, 2).equals("//")) {
                    break;
                } else {
                    command += element + " ";
                }
            }

            if (!command.isBlank()) {
                commands.add(command.trim());
            }
        }

        s.close();
    }

    public boolean hasNext() {
        return currLine < commands.size() - 1;
    }

    public void next() {
        curr = commands.get(++currLine).split(" ");
    }

    public int type() {
        if (arithmeticCommands.contains(curr[0])) {
            return ARITHMETIC;
        }

        if (curr[0].equals("push")) {
            return PUSH;
        }

        if (curr[0].equals("pop")) {
            return POP;
        }

        return -1;
    }

    public String firstArg() {
        if (type() == ARITHMETIC)
            return curr[0];
        else
            return curr[1];
    }

    public int secondArg() {
        return Integer.parseInt(curr[2]);
    }
}

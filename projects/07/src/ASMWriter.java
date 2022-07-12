import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ASMWriter {
    private String filename;
    private PrintWriter writer;
    private int currLine = 0;

    public ASMWriter(File output, String filename) throws FileNotFoundException {
        writer = new PrintWriter(output);
        this.filename = filename;
    }

    public void arithmetic(String operation) {
        switch (operation) {
            case "add":
            case "sub":
            case "and":
            case "or":
                mainArith(operation);
                break;

            case "eq":
            case "lt":
            case "gt":
                boolArith(operation);
                break;

            case "not":
                notArith();
                break;

            case "neg":
                negArith();
                break;
        }
    }

    private void negArith() {
        write("D=0");
        write("@SP");
        write("A=M-1");
        write("M=D-M");
    }

    private void notArith() {
        write("@SP");
        write("A=M-1");
        write("M=!M");
    }

    private void boolArith(String operation) {
        write("@SP");
        write("AM=M-1");
        write("D=M");
        write("A=A-1");
        write("D=M-D");
        write("@" + (currLine + 7));

        switch (operation) {
            case "eq":
                write("D;JEQ");
                break;

            case "lt":
                write("D;JLT");
                break;

            case "gt":
                write("D;JGT");
                break;
        }

        write("@SP");
        write("A=M-1");
        write("M=0");
        write("@" + (currLine + 5));
        write("0;JMP");
        write("@SP");
        write("A=M-1");
        write("M=-1");
    }

    private void mainArith(String operation) {
        write("@SP");
        write("AM=M-1");
        write("D=M");
        write("A=A-1");

        switch (operation) {
            case "add":
                write("M=D+M");
                break;

            case "sub":
                write("M=M-D");
                break;

            case "and":
                write("M=D&M");
                break;

            case "or":
                write("M=D|M");
                break;
        }
    }

    public void push(String segment, int index) {
        switch (segment) {
            case "pointer":
                if (index == 0)
                    write("@THIS");
                else if (index == 1)
                    write("@THAT");
                write("D=M");
                break;

            case "static":
                write("@" + filename + "." + index);
                write("D=M");
                break;

            case "constant":
                write("@" + index);
                write("D=A");
                break;

            default:
                if (index > 0) {
                    write("@" + index);
                    write("D=A");
                    write(getLabel(segment));
                    write(segment.equals("temp") ? "A=D+A" : "A=D+M");
                    write("D=M");
                } else if (index == 0) {
                    write(getLabel(segment));
                    write("A=M");
                    write("D=M");
                }
        }

        write("@SP");
        write("A=M");
        write("M=D");
        write("@SP");
        write("M=M+1");
    }

    public void pop(String segment, int index) {
        if (index == 0 || segment.equals("pointer") || segment.equals("static")) {
            write("@SP");
            write("AM=M-1");
            write("D=M");
            popLabel(segment, index);
        } else if (index > 0) {
            write("@" + index);
            write("D=A");
            write(getLabel(segment));
            if (segment.equals("temp"))
                write("D=D+A");
            else
                write("D=M+D");
            write("@R13");
            write("M=D");
            write("@SP");
            write("AM=M-1");
            write("D=M");
            write("@R13");
        }

        if (!segment.equals("pointer") && !segment.equals("static"))
            write("A=M");

        write("M=D");
    }

    private void popLabel(String segment, int index) {
        switch (segment) {
            case "pointer":
                if (index == 0)
                    write("@THIS");
                else if (index == 1)
                    write("@THAT");
                break;

            case "static":
                write("@" + filename + "." + index);
                break;

            default:
                write(getLabel(segment));
        }
    }

    private void write(String line) {
        writer.println(line);
        currLine++;
    }

    private String getLabel(String segment) {
        switch (segment) {
            case "local":
                return "@LCL";

            case "argument":
                return "@ARG";

            case "this":
                return "@THIS";

            case "that":
                return "@THAT";

            case "temp":
                return "@R5";
        }
        return null;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void close() {
        writer.close();
    }
}

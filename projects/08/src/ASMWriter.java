import java.io.FileNotFoundException;
import java.io.File;
import java.io.PrintWriter;

// 
// Decompiled by Procyon v0.5.36
// 

public class ASMWriter
{
    private String filename;
    private final PrintWriter writer;
    private int callNumber;
    private int currLine;

    public ASMWriter(final File output, final String filename) throws FileNotFoundException {
        final int n = 0;
        this.callNumber = n;
        this.currLine = n;
        this.writer = new PrintWriter(output);
        this.filename = filename;
        bootstrap();
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private void bootstrap() {
        write("@256");
        write("D=A");
        write("@SP");
        write("M=D");
        call("Sys.init",0);
    }

    public void arithmetic(final String operation) {
        switch (operation) {
            case "add", "sub", "and", "or" -> this.mainArith(operation);
            case "eq", "lt", "gt" -> this.boolArith(operation);
            case "not" -> this.notArith();
            case "neg" -> this.negArith();
        }
    }
    
    private void negArith() {
        this.write("D=0");
        this.write("@SP");
        this.write("A=M-1");
        this.write("M=D-M");
    }
    
    private void notArith() {
        this.write("@SP");
        this.write("A=M-1");
        this.write("M=!M");
    }
    
    private void boolArith(final String operation) {
        this.write("@SP");
        this.write("AM=M-1");
        this.write("D=M");
        this.write("A=A-1");
        this.write("D=M-D");
        this.write("@" + (currLine + 7));
        switch (operation) {
            case "eq" -> this.write("D;JEQ");
            case "lt" -> this.write("D;JLT");
            case "gt" -> this.write("D;JGT");
        }
        this.write("@SP");
        this.write("A=M-1");
        this.write("M=0");
        this.write("@" + (currLine + 5));
        this.write("0;JMP");
        this.write("@SP");
        this.write("A=M-1");
        this.write("M=-1");
    }
    
    private void mainArith(final String operation) {
        this.write("@SP");
        this.write("AM=M-1");
        this.write("D=M");
        this.write("A=A-1");
        switch (operation) {
            case "add" -> this.write("M=D+M");
            case "sub" -> this.write("M=M-D");
            case "and" -> this.write("M=D&M");
            case "or" -> this.write("M=D|M");
        }
    }
    
    public void push(final String segment, final int index) {
        switch (segment) {
            case "pointer" -> {
                if (index == 0) {
                    this.write("@THIS");
                } else if (index == 1) {
                    this.write("@THAT");
                }
                this.write("D=M");
            }
            case "static" -> {
                this.write("@" + filename + "." + index);
                this.write("D=M");
            }
            case "constant" -> {
                this.write("@" + index);
                this.write("D=A");
            }
            default -> {
                if (index > 0) {
                    this.write("@" + index);
                    this.write("D=A");
                    this.write(this.getLabel(segment));
                    this.write(segment.equals("temp") ? "A=D+A" : "A=D+M");
                    this.write("D=M");
                } if (index == 0) {
                    this.write(this.getLabel(segment));
                    this.write("A=M");
                    this.write("D=M");
                }
            }
        }
        this.endPush();
    }
    
    private void endPush() {
        this.write("@SP");
        this.write("A=M");
        this.write("M=D");
        this.write("@SP");
        this.write("M=M+1");
    }
    
    public void pop(final String segment, final int index) {
        if (index == 0 || segment.equals("pointer") || segment.equals("static")) {
            this.write("@SP");
            this.write("AM=M-1");
            this.write("D=M");
            this.popLabel(segment, index);
        } else if (index > 0) {
            this.write("@" + index);
            this.write("D=A");
            this.write(this.getLabel(segment));
            if (segment.equals("temp")) {
                this.write("D=D+A");
            }
            else {
                this.write("D=M+D");
            }
            this.write("@R13");
            this.write("M=D");
            this.write("@SP");
            this.write("AM=M-1");
            this.write("D=M");
            this.write("@R13");
        }
        if (!segment.equals("pointer") && !segment.equals("static") && !segment.equals("temp")) {
            this.write("A=M");
        }
        this.write("M=D");
    }
    
    public void label(final String label) {
        writer.println("(" + label + ")");
    }
    
    public void wGoto(final String label) {
        this.write("@" + label);
        this.write("0;JMP");
    }
    
    public void ifGoto(final String label) {
        this.write("@SP");
        this.write("AM=M-1");
        this.write("D=M");
        this.write("@" + label);
        this.write("D;JNE");
    }
    
    public void call(final String function, final int nArgs) {
        this.write("@return" + callNumber);
        this.write("D=A");
        this.endPush();
        this.write("@LCL");
        this.write("D=M");
        this.endPush();
        this.write("@ARG");
        this.write("D=M");
        this.endPush();
        this.write("@THIS");
        this.write("D=M");
        this.endPush();
        this.write("@THAT");
        this.write("D=M");
        this.endPush();
        this.write("@SP");
        this.write("D=M");
        this.write("@" + nArgs);
        this.write("D=D-A");
        this.write("@5");
        this.write("D=D-A");
        this.write("@ARG");
        this.write("M=D");
        this.write("@SP");
        this.write("D=M");
        this.write("@LCL");
        this.write("M=D");
        this.wGoto(function);
        this.label("return" + callNumber++);
    }
    
    public void function(final String function, final int nLocals) {
        this.label(function);
        for (int i = 0; i < nLocals; ++i)
            this.push("constant", 0);
    }
    
    public void wReturn() {
        this.write("@LCL");
        this.write("D=M");
        this.write("@FRAME");
        this.write("M=D");
        this.write("@5");
        this.write("A=D-A");
        this.write("D=M");
        this.write("@return");
        this.write("M=D");
        this.write("@SP");
        this.write("AM=M-1");
        this.write("D=M");
        this.write("@ARG");
        this.write("A=M");
        this.write("M=D");
        this.write("@ARG");
        this.write("D=M+1");
        this.write("@SP");
        this.write("M=D");
        this.write("@FRAME");
        this.write("A=M-1");
        this.write("D=M");
        this.write("@THAT");
        this.write("M=D");
        this.write("@FRAME");
        this.write("D=M");
        this.write("@2");
        this.write("A=D-A");
        this.write("D=M");
        this.write("@THIS");
        this.write("M=D");
        this.write("@FRAME");
        this.write("D=M");
        this.write("@3");
        this.write("A=D-A");
        this.write("D=M");
        this.write("@ARG");
        this.write("M=D");
        this.write("@FRAME");
        this.write("D=M");
        this.write("@4");
        this.write("A=D-A");
        this.write("D=M");
        this.write("@LCL");
        this.write("M=D");
        this.write("@return");
        this.write("A=M");
        this.write("0;JMP");
    }
    
    private void popLabel(final String segment, final int index) {
        switch (segment) {
            case "pointer" -> {
                if (index == 0) this.write("@THIS");
                else if (index == 1) this.write("@THAT");
            }
            case "static" -> this.write("@" + filename + "." + index);
            default -> this.write(this.getLabel(segment));
        }
    }

    private void write(final String line) {
        this.writer.println(line);
        this.currLine++;
    }
    
    private String getLabel(final String segment) {
        return switch (segment) {
            case "local" -> "@LCL";
            case "argument" ->  "@ARG";
            case "this" -> "@THIS";
            case "that" -> "@THAT";
            case "temp" ->  "@R5";
            default ->  null;
        };
    }
    
    public void close() {
        this.writer.close();
    }
}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class VMWriter {
    private PrintWriter pw;

    public VMWriter(File out)  {
        try {
            pw = new PrintWriter(out);
        } catch(FileNotFoundException ignored) {}
    }

    public void writePush(String segment, int index) {
        pw.println("push " + segment + ' ' + index);
    }

    public void writePop(String segment, int index) {
        pw.println("pop " + segment + ' ' + index);
    }

    public void writeArithmetic(String comand) {
        pw.println(comand);
    }

    public void writeLabel(String label) {
        pw.println("label " + label);
    }

    public void writeGoto(String label) {
        pw.println("goto " + label);
    }

    public void writeIf(String label) {
        pw.println("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) {
        pw.println("call " + name + ' ' + nArgs);
    }

    public void writeFunction(String name, int nLocals) {
        pw.println("function " + name + ' ' + nLocals);
    }

    public void writeReturn() {
        pw.println("return");
    }

    public void close() {
        pw.close();
    }
}

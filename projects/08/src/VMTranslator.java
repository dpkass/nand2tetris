import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//
// Decompiled by Procyon v0.5.36
//

public class VMTranslator
{
    public static void main(final String[] args) throws Exception {
        final File in = new File(args[0]);
        final ASMWriter aw;
        VMParser p;

        if(in.isDirectory()){
            final String outName = in.getName();
            final File out = new File(in, outName + ".asm");
            List<File> files =
                    new ArrayList<>(Arrays.stream(in.listFiles()).filter(f->f.getName().endsWith(".vm")).toList());
            aw = new ASMWriter(out, outName);
            for (File f : files) {
                aw.setFilename(f.getName());
                p = new VMParser(f);
                runFile(aw, p);
            }
        } else {
            final String parent = in.getParent();
            final String outName = parent.substring(parent.indexOf('/') + 1);
            final File out = new File(parent, outName + ".asm");
            aw = new ASMWriter(out, outName);
            p = new VMParser(in);
            runFile(aw, p);
        }
        aw.close();
    }

    private static void runFile(ASMWriter aw, VMParser p) {
        do {
            switch (p.type()) {
                case VMParser.PUSH -> aw.push(p.firstArg(), p.secondArg());
                case VMParser.POP -> aw.pop(p.firstArg(), p.secondArg());
                case VMParser.ARITHMETIC -> aw.arithmetic(p.firstArg());
                case VMParser.LABEL -> aw.label(p.firstArg());
                case VMParser.GOTO -> aw.wGoto(p.firstArg());
                case VMParser.IF_GOTO -> aw.ifGoto(p.firstArg());
                case VMParser.CALL -> aw.call(p.firstArg(), p.secondArg());
                case VMParser.FUNCTION -> aw.function(p.firstArg(), p.secondArg());
                case VMParser.RETURN -> aw.wReturn();
                default -> throw new IllegalStateException("Unexpected value: " + p.type());
            }
            p.next();
        } while (p.hasNext());
    }
}

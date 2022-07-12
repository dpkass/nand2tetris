import java.io.File;

public class VMTranslator {
    public static void main(String[] args) throws Exception {
        File in = new File(args[0]), out;
        String outName = in.getName().substring(0, in.getName().indexOf('.'));
        out = new File(in.getParent(), outName + ".asm");

        ASMWriter aw = new ASMWriter(out, outName);
        VMParser p = new VMParser(in);

        while (true) {
            switch (p.type()) {
                case VMParser.PUSH:
                    aw.push(p.firstArg(), p.secondArg());
                    break;

                case VMParser.POP:
                    aw.pop(p.firstArg(), p.secondArg());
                    break;

                case VMParser.ARITHMETIC:
                    aw.arithmetic(p.firstArg());
                    break;
            }
            if (!p.hasNext())
                break;
            p.next();
        }
        aw.close();
    }
}

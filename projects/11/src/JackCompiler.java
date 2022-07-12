import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JackCompiler {
    public static void main(String[] args) {
        String[] files = getFiles(args[0]);

        for (String file : files)
            new CompilationEngine(file);
    }

    private static String[] getFiles(String arg) {
        File f = new File(arg);

        if (!f.isDirectory())
            return new String[] { f.toString() };

        File[] files = f.listFiles();
        List<String> res = new ArrayList<>();

        for (File file : files) {
            String filename = file.toString();
            int i = filename.lastIndexOf('.');
            if (i > 0 && filename.substring(i + 1).equals("jack")) {
                filename = filename.substring(0, i);
                res.add(filename);
            }
        }

        return res.toArray(String[]::new);
    }
}

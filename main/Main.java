import syntaxtree.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.err.println("Usage: java Main <inputFile1> <inputFile2> ... <inputFileN>");
            System.exit(1);
        }



        FileInputStream fis = null;
        for (int i = 0; i < args.length; i++) {
            try{
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                System.err.println("Program in inputFile \"" + args[i] + "\" parsed successfully.");

                MyVisitor eval = new MyVisitor();
                root.accept(eval, null);
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage() + " inputFile \"" + args[i] + "\"");
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage() + " inputFile \"" + args[i] + "\"");
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage() + " inputFile \"" + args[i] + "\"");
                }
            }
        }
    }
}

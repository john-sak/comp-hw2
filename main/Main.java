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
                symbolTableVisitor STVisitor = new symbolTableVisitor();
                root.accept(STVisitor, null);
                System.out.println("Symbol Table for program in inputFile \"" + args[i] + "\" created succesfully.");
                TCArgs arguTCA = new TCArgs();
                arguTCA.globalST = STVisitor.globalST;
                typeCheckVisitor TCVisitor = new typeCheckVisitor();
                root.accept(TCVisitor, arguTCA);
                System.out.println("Type Check for program in inputFile \"" + args[i] + "\" finished succesfully.");
                OTArgs arguOTA = new OTArgs();
                arguOTA.symbolTable = STVisitor.globalST;
                offsetTableVisitor OTVisitor = new offsetTableVisitor();
                root.accept(OTVisitor, arguOTA);
                OTVisitor.printResult();
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage() + " inputFile \"" + args[i] + "\"");
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage() + " inputFile \"" + args[i] + "\"");
            }
            catch (Exception ex) {
                System.err.println("Error in inputFile \"" + args[i] + "\" (" + ex.getMessage() + ").");
            }
            finally{
                try{
                    if(fis != null) fis.close();
                    System.out.println();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage() + " inputFile \"" + args[i] + "\"");
                }
            }
        }
    }
}

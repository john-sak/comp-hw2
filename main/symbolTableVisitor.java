import syntaxtree.*;
import visitor.*;

class symbolTableVisitor extends GJDepthFirst<String, String> {
    public symbolTable<String, String> ST;
}
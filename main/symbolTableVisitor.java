import syntaxtree.*;
import visitor.*;

import java.util.Map;
import java.util.HashMap;

class symType {
    String name, type;
}

class symbolTableVisitor extends GJDepthFirst<String, String> {
    Map<String, Map<String, symType>> ST = new HashMap<String, Map<String, symType>>();

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
    @Override
    public String visit(MainClass n, String argu) throws Exception {
        String className = n.f1.accept(this, "GLOBAL");
        if (this.ST.containsKey(className)) throw new Exception();
        else {
            Map<String, symType> innerST = new HashMap<String, symType>();
            this.ST.put("GLOAL", innerST);
        }
        return null;
    }
}
import syntaxtree.*;
import visitor.*;

import java.util.Map;
import java.util.HashMap;

class symType {
    public String type, ret = "", args = "";
    public int argNum = -1, size = -1;
    Map<String, symType> inST = null;
    // Map<String, symType> inST = new HashMap<String, symType>();
}

class symbolTableVisitor extends GJDepthFirst<String, String> {
    Map<String, Map<String, symType>> globalST = new HashMap<String, Map<String, symType>>();

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
        String className = n.f1.accept(this, "");
        if (this.globalST.containsKey(className)) throw new Exception();
        else {
            Map<String, symType> localST = new HashMap<String, symType>();
            this.globalST.put(className, localST);
            symType symTypeOut = new symType();
            localST.put("main", symTypeOut);
            symTypeOut.type = "function";
            symTypeOut.ret = "void";
            symTypeOut.argNum = 1;
            symTypeOut.args = "String[]";
            symTypeOut.inST = new HashMap<String, symType>();
            String argName = n.f11.accept(this, "");
            symType symTypeIn = new symType();
            symTypeOut.inST.put(argName, symTypeIn);
            symTypeIn.type = "String[]";
        }
        n.f14.accept(this, className + "->" + "main");
        return null;
    }
}
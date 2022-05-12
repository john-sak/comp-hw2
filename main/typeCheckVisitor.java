import syntaxtree.*;
import visitor.*;

import java.util.Map;
import java.util.HashMap;

class TCArgs {
    public String scope = "";
    Map<String, Map<String, symInfo>> globalST = null;
}

class typeCheckVisitor extends GJDepthFirst<Void, TCArgs> {
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
    public Void visit(MainClass n, TCArgs argu) throws Exception {
        argu.scope = argu.scope + "->main";
        return n.f15.accept(this, argu);
    }
}
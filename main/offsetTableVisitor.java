import syntaxtree.*;
import visitor.*;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;

class OTEntry {
    String scope, identifier, offest;
}

class OTNode {
    String className;
    List<List<OTEntry>> offsets = new ArrayList<List<OTEntry>>();

    OTNode(String name) {
        this.className = name;
        offsets.add(new ArrayList<OTEntry>());
        offsets.add(new ArrayList<OTEntry>());
    }
}

class OTArgs {
    Map<String, classInfo> symbolTable = null;
    List<OTNode> stack = new ArrayList<OTNode>();
}

class offsetTableVisitor extends GJDepthFirst<String, OTArgs> {

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, OTArgs argu) throws Exception {
        if (argu.symbolTable == null) throw new Exception();
        // n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, OTArgs argu) throws Exception {
        String className = n.f1.accept(this, null);
        argu.stack.add(new OTNode(className));
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return null;
    }
    
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, OTArgs argu) throws Exception {
        String className = n.f1.accept(this, null);
        argu.stack.add(new OTNode(className));
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        return null;
    }
}
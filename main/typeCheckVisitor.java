import syntaxtree.*;
import visitor.*;

import java.util.Map;
import java.util.HashMap;

class TCArgs {
    public String scope = "";
    Map<String, classInfo> globalST = null;
}

class typeCheckVisitor extends GJDepthFirst<String, TCArgs> {

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
    public String visit(MainClass n, TCArgs argu) throws Exception {
        TCArgs oldArgu = argu;
        argu.scope = n.f1.accept(this, null) + "->main";
        n.f15.accept(this, argu);
        argu = oldArgu;
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
    public String visit(ClassDeclaration n, TCArgs argu) throws Exception {
        TCArgs oldArgu = argu;
        argu.scope = n.f1.accept(this, null);
        n.f4.accept(this, argu);
        argu = oldArgu;
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
    public String visit(ClassExtendsDeclaration n, TCArgs argu) throws Exception {
        TCArgs oldArgu = argu;
        argu.scope = n.f1.accept(this, null);
        n.f6.accept(this, argu);
        argu = oldArgu;
        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, TCArgs argu) throws Exception {
        TCArgs oldArgu = argu;
        argu.scope += "->" + n.f2.accept(this, null);
        n.f8.accept(this, argu);
        if (n.f1.accept(this, null).compareTo(n.f10.accept(this, argu)) != 0) throw new Exception();
        argu = oldArgu;
        return null;
    }

    /**
     * f0 -> "boolean"
     * f1 -> "["
     * f2 -> "]"
     */
    @Override
    public String visit(BooleanArrayType n, TCArgs argu) throws Exception {
        return "boolean[]";
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    @Override
    public String visit(IntegerArrayType n, TCArgs argu) throws Exception {
        return "int[]";
    }

    /**
     * f0 -> "boolean"
     */
    @Override
    public String visit(BooleanType n, TCArgs argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "int"
     */
    @Override
    public String visit(IntegerType n, TCArgs argu) throws Exception {
        return "int";
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public String visit(AssignmentStatement n, TCArgs argu) throws Exception {
        if (!argu.scope.contains("->")) throw new Exception();
        String[] scopes = argu.scope.split("->");
        if (scopes.length != 2) throw new Exception();
        classInfo classI;
        if ((classI = argu.globalST.get(scopes[0])) == null) throw new Exception();
        methodInfo methodI;
        if ((methodI = classI.methods.get(scopes[1])) == null) throw new Exception();
        String name = n.f0.accept(this, null);
        fieldInfo fieldI;
        if ((fieldI = methodI.localVars.get(name)) != null);
        else {
            for (Map<String, fieldInfo> scope : classI.fields) if ((fieldI = scope.get(name)) != null) break;
            if (fieldI == null) throw new Exception();
        }
        if (fieldI.type.compareTo(n.f2.accept(this, argu)) != 0) throw new Exception();
        return null;
    }
    
    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    @Override
    public String visit(ArrayAssignmentStatement n, TCArgs argu) throws Exception {
        if (n.f2.accept(this, argu).compareTo("int") != 0) throw new Exception();
        if (!argu.scope.contains("->")) throw new Exception();
        String[] scopes = argu.scope.split("->");
        if (scopes.length != 2) throw new Exception();
        classInfo classI;
        if ((classI = argu.globalST.get(scopes[0])) == null) throw new Exception();
        methodInfo methodI;
        if ((methodI = classI.methods.get(scopes[1])) == null) throw new Exception();
        String name = n.f0.accept(this, null);
        fieldInfo fieldI;
        if ((fieldI = methodI.localVars.get(name)) != null);
        else {
            for (Map<String, fieldInfo> scope : classI.fields) if ((fieldI = scope.get(name)) != null) break;
            if (fieldI == null) throw new Exception();
        }
        if (fieldI.type.compareTo(n.f5.accept(this, argu)) != 0) throw new Exception();
        return null;
    }
    
    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, TCArgs argu) throws Exception {
        if (n.f2.accept(this, argu).compareTo("boolean") != 0) throw new Exception();
        n.f4.accept(this, argu);
        n.f6.accept(this, argu);
        return null;
    }
    
    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, TCArgs argu) throws Exception {
        if (n.f2.accept(this, argu).compareTo("boolean") != 0) throw new Exception();
        n.f4.accept(this, argu);
        return null;
    }
    
    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, TCArgs argu) throws Exception {
        if (n.f2.accept(this, argu).compareTo("int") != 0) throw new Exception();
        return null;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    @Override
    public String visit(Identifier n, TCArgs argu) throws Exception {
        return n.f0.toString();
    }
}
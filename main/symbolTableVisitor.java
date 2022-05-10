import syntaxtree.*;
import visitor.*;

import java.util.Map;
import java.util.HashMap;

class symType {
    public String type, ret = null, args = null;
    public int argNum = -1, size = -1;
    public boolean extended = false;
    public Map<String, symType> inST = null;
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
        String className = n.f1.accept(this, null);
        if (this.globalST.containsKey(className)) throw new Exception();
        Map<String, symType> localST = new HashMap<String, symType>();
        this.globalST.put(className, localST);
        symType symTypeOut = new symType();
        localST.put("main", symTypeOut);
        symTypeOut.type = "function";
        symTypeOut.ret = "void";
        symTypeOut.argNum = 1;
        symTypeOut.args = "String[]";
        symTypeOut.inST = new HashMap<String, symType>();
        String argName = n.f11.accept(this, null);
        symType symTypeIn = new symType();
        symTypeOut.inST.put(argName, symTypeIn);
        symTypeIn.type = "String[]";
        n.f14.accept(this, className + "->" + "main");
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
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String className = n.f1.accept(this, null);
        if (this.globalST.containsKey(className)) throw new Exception();
        Map<String, symType> localST = new HashMap<String, symType>();
        this.globalST.put(className, localST);
        n.f3.accept(this, className);
        n.f4.accept(this, className);
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
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String className = n.f1.accept(this, null); // classExtends = n.f3.accept(this, null);
        if (this.globalST.containsKey(className)) throw new Exception();
        Map<String, symType> localST = new HashMap<String, symType>();
        this.globalST.put(className, localST);
        // localST.putAll(this.globalST.get(classExtends));
        // for (Map.Entry<String, symType> entry : localST.entrySet()) entry.getValue().extended = true;
        n.f3.accept(this, className);
        n.f4.accept(this, className);
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {
        String[] scopes = argu.split("->");
        Map<String, symType> localST = this.globalST.get(scopes[0]);
        if (argu.contains("->")) {
            symType symTypeIn = localST.get(scopes[1]);
            if (symTypeIn.type.equals("function")) localST = symTypeIn.inST;
            else throw new Exception();
        }
        String type = n.f0.accept(this, null), id = n.f1.accept(this, null);
        if (localST.containsKey(id)) throw new Exception();
        symType symTypeIn = new symType();
        localST.put(id, symTypeIn);
        symTypeIn.type = type;
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
    public String visit(MethodDeclaration n, String argu) throws Exception {
        if (argu.contains("->")) throw new Exception();
        String type = n.f1.accept(this, null), id = n.f2.accept(this, null), argList = (n.f4.present() ? n.f4.accept(this, null) : null);
        if (!this.globalST.containsKey(argu)) throw new Exception();
        if (this.globalST.get(argu).containsKey(id)) throw new Exception();
        symType symTypeIn = new symType();
        this.globalST.get(argu).put(id, symTypeIn);
        symTypeIn.type = "function";
        symTypeIn.ret = type;
        symTypeIn.inST = new HashMap<String, symType>();
        if (!argList.equals("")) {
            String[] args = argList.split(", ");
            symTypeIn.argNum = args.length;
            symTypeIn.args = "";
            for (String arg : args) {
                String[] parts = arg.split(" ");
                symTypeIn.args += parts[0] + ", ";
                if (symTypeIn.inST.containsKey(parts[1])) throw new Exception();
                symType symType2 = new symType();
                symTypeIn.inST.put(parts[1], symType2);
                symType2.type = parts[0];
            }
            symTypeIn.args = symTypeIn.args.substring(0, symTypeIn.args.length() - 2);
        } else {
            symTypeIn.args = "";
            symTypeIn.argNum = 0;
        }
        n.f7.accept(this, argu + "->" + id);
        return null;
    }

    /**
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        return n.f0.accept(this, null) + n.f1.accept(this, null);
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, String argu) throws Exception {
        return n.f0.accept(this, null) + " " + n.f1.accept(this, null);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return ", " + n.f1.accept(this, null);
    }

    /**
     * f0 -> "boolean"
     * f1 -> "["
     * f2 -> "]"
     */
    @Override
    public String visit(BooleanArrayType n, String argu) throws Exception {
        return "boolean[]";
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    @Override
    public String visit(IntegerArrayType n, String argu) throws Exception {
        return "int[]";
    }

    /**
     * f0 -> "boolean"
     */
    @Override
    public String visit(BooleanType n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "int"
     */
    @Override
    public String visit(IntegerType n, String argu) throws Exception {
        return "int";
    }
}
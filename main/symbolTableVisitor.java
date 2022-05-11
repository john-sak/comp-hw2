import syntaxtree.*;
import visitor.*;

import java.util.Map;
import java.util.HashMap;

class symInfo {
    public String type, ret = null, args = null;
    public int argNum = 0, size = 0;
    // public boolean extended = false;
    public Map<String, symInfo> localST = null;
}

class symbolTableVisitor extends GJDepthFirst<String, String> {
    Map<String, Map<String, symInfo>> globalST = new HashMap<String, Map<String, symInfo>>();

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
        Map<String, symInfo> localST = new HashMap<String, symInfo>();
        this.globalST.put(className, localST);
        symInfo infoOut = new symInfo();
        localST.put("main", infoOut);
        infoOut.type = "function";
        infoOut.ret = "void";
        infoOut.argNum = 1;
        infoOut.args = "String[]";
        infoOut.localST = new HashMap<String, symInfo>();
        String argName = n.f11.accept(this, null);
        symInfo infoIn = new symInfo();
        infoOut.localST.put(argName, infoIn);
        infoIn.type = "String[]";
        n.f14.accept(this, className + "->main");
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
        Map<String, symInfo> localST = new HashMap<String, symInfo>();
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
        Map<String, symInfo> localST = new HashMap<String, symInfo>();
        this.globalST.put(className, localST);
        // localST.putAll(this.globalST.get(classExtends));
        // for (Map.Entry<String, symInfo> entry : localST.entrySet()) entry.getValue().extended = true;
        n.f5.accept(this, className);
        n.f6.accept(this, className);
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
        Map<String, symInfo> localST = this.globalST.get(scopes[0]);
        if (argu.contains("->")) {
            symInfo info = localST.get(scopes[1]);
            if (!info.type.equals("function")) throw new Exception();
            localST = info.localST;
        }
        String type = n.f0.accept(this, null), id = n.f1.accept(this, null);
        if (localST.containsKey(id)) throw new Exception();
        symInfo info = new symInfo();
        localST.put(id, info);
        info.type = type;
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
        String type = n.f1.accept(this, null), id = n.f2.accept(this, null), argList = (n.f4.present() ? n.f4.accept(this, null) : "");
        if (!this.globalST.containsKey(argu)) throw new Exception();
        if (this.globalST.get(argu).containsKey(id)) throw new Exception();
        symInfo info = new symInfo();
        this.globalST.get(argu).put(id, info);
        info.type = "function";
        info.ret = type;
        info.localST = new HashMap<String, symInfo>();
        if (!argList.equals("")) {
            String[] args = argList.split(", ");
            info.argNum = args.length;
            info.args = "";
            for (String arg : args) {
                String[] parts = arg.split(" ");
                info.args += parts[0] + ", ";
                if (info.localST.containsKey(parts[1])) throw new Exception();
                symInfo infoIn = new symInfo();
                info.localST.put(parts[1], infoIn);
                infoIn.type = parts[0];
            }
            info.args = info.args.substring(0, info.args.length() - 2);
        } else {
            info.args = "";
            info.argNum = 0;
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

    /**
     * f0 -> <IDENTIFIER>
     */
    @Override
    public String visit(Identifier n, String argu) throws Exception {
        return n.f0.toString();
    }
}
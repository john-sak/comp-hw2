import syntaxtree.*;
import visitor.*;

import java.util.Map;

class TCArgs {
    public String scope = null;
    Map<String, classInfo> globalST = null;
}

class typeCheckVisitor extends GJDepthFirst<String, TCArgs> {

    public String resolveIdentifier(String ID, TCArgs argu) throws Exception {
        if (!argu.scope.contains("->")) throw new Exception();
        String[] scope = argu.scope.split("->");
        classInfo classI;
        if ((classI = argu.globalST.get(scope[0])) == null) throw new Exception();
        methodInfo methodI;
        if ((methodI = classI.methods.get(scope[1])) == null) throw new Exception();
        fieldInfo fieldI;
        if ((fieldI = methodI.localVars.get(ID)) != null) return fieldI.type;
        // if ((fieldI = classI.fields.get(ID)) != null) return fieldI.type;
        // while ((classI = classI.superclass) != null) if ((fieldI = classI.fields.get(ID)) != null) return fieldI.type;
        while (classI != null) {
            if ((fieldI = classI.fields.get(ID)) != null) return fieldI.type;
            classI = classI.superclass;
        }
        // for (int i = 0; i < classI.superclasses.size(); i++) if (classI.superclasses.get(i).fields.get(0).containsKey(ID)) return classI.superclasses.get(i).fields.get(0).get(ID).type;
        // if (classI.fields.contains(ID)) return classI.fields.get(0).get(ID).type;
        // if (argu.globalST.containsKey(ID)) return ID;
        return null;
    }

    public boolean isValidType(String type, TCArgs argu) {
        return type.compareTo("boolean[]") == 0 || type.compareTo("int[]") == 0 || type.compareTo("boolean") == 0 || type.compareTo("int") == 0 || argu.globalST.containsKey(type);
    }

    public boolean isAcceptable(String expected, String given, TCArgs argu) throws Exception {
        if (!isValidType(given, argu)) return false;
        String[] acceptable = expected.split("-");
        for (String type : acceptable) {
            if (!isValidType(type, argu)) continue;
            if (given.compareTo(type) == 0) return true;
            classInfo classI = argu.globalST.get(given);
            // if ((classI = argu.globalST.get(given)) == null) return false;
            while ((classI = classI.superclass) != null) if (classI.name.compareTo(type) == 0) return true;
            // if (classI.superclasses.contains(argu.globalST.get(expected))) return true;
        }
        return false;
    }

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
        argu.scope = n.f1.accept(this, argu) + "->main";
        n.f14.accept(this, argu);
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
        argu.scope = n.f1.accept(this, argu);
        n.f3.accept(this, argu);
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
        argu.scope = n.f1.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        argu = oldArgu;
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, TCArgs argu) throws Exception {
        // String type = n.f0.accept(this, argu);
        // if (!isValidType(type, argu)) throw new Exception();
        if (!isValidType(n.f0.accept(this, argu), argu)) throw new Exception();
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
        String type = n.f1.accept(this, argu);
        if (!isValidType(type, argu)) throw new Exception();
        TCArgs oldArgu = argu;
        argu.scope += "->" + n.f2.accept(this, argu);
        n.f4.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        if (!isAcceptable(type, n.f10.accept(this, argu), argu)) throw new Exception();
        argu = oldArgu;
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, TCArgs argu) throws Exception {
        if (!isValidType(n.f0.accept(this, argu), argu)) throw new Exception();
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
        if (!isAcceptable(resolveIdentifier(n.f0.accept(this, argu), argu), n.f2.accept(this, argu), argu)) throw new Exception();
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
        String type = resolveIdentifier(n.f0.accept(this, argu), argu);
        if (!isAcceptable("boolean[]-int[]", type, argu)) throw new Exception();
        if (!isAcceptable("int", n.f2.accept(this, argu), argu)) throw new Exception();
        // type = type.substring(0, type.length() - 2);
        if (!isAcceptable(type.substring(0, type.length() - 2), n.f5.accept(this, argu), argu)) throw new Exception();
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
    @Override
    public String visit(IfStatement n, TCArgs argu) throws Exception {
        if (!isAcceptable("boolean", n.f2.accept(this, argu), argu)) throw new Exception();
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
    @Override
    public String visit(WhileStatement n, TCArgs argu) throws Exception {
        if (!isAcceptable("boolean", n.f2.accept(this, argu), argu)) throw new Exception();
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
    @Override
    public String visit(PrintStatement n, TCArgs argu) throws Exception {
        if (!isAcceptable("int", n.f2.accept(this, argu), argu)) throw new Exception();
        return null;
    }
    
    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    @Override
    public String visit(AndExpression n, TCArgs argu) throws Exception {
        if (!isAcceptable("boolean", n.f0.accept(this, argu), argu)) throw new Exception();
        if (!isAcceptable("boolean", n.f2.accept(this, argu), argu)) throw new Exception();
        return "boolean";
    }
    
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(CompareExpression n, TCArgs argu) throws Exception {
        if (!isAcceptable("int", n.f0.accept(this, argu), argu)) throw new Exception();
        if (!isAcceptable("int", n.f2.accept(this, argu), argu)) throw new Exception();
        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n, TCArgs argu) throws Exception {
        if (!isAcceptable("int", n.f0.accept(this, argu), argu)) throw new Exception();
        if (!isAcceptable("int", n.f2.accept(this, argu), argu)) throw new Exception();
        return "int";
    }
    
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(MinusExpression n, TCArgs argu) throws Exception {
        if (!isAcceptable("int", n.f0.accept(this, argu), argu)) throw new Exception();
        if (!isAcceptable("int", n.f2.accept(this, argu), argu)) throw new Exception();
        return "int";
    }
    
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(TimesExpression n, TCArgs argu) throws Exception {
        if (!isAcceptable("int", n.f0.accept(this, argu), argu)) throw new Exception();
        if (!isAcceptable("int", n.f2.accept(this, argu), argu)) throw new Exception();
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public String visit(ArrayLookup n, TCArgs argu) throws Exception {
        String type = n.f0.accept(this, argu);
        if (!isAcceptable("boolean[]-int[]", type, argu)) throw new Exception();
        if (!isAcceptable("int", n.f2.accept(this, argu), argu)) throw new Exception();
        return type.substring(0, type.length() - 2);
    }
    
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public String visit(ArrayLength n, TCArgs argu) throws Exception {
        if (!isAcceptable("boolean[]-int[]", n.f0.accept(this, argu), argu)) throw new Exception();
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public String visit(MessageSend n, TCArgs argu) throws Exception {
        String expr = n.f0.accept(this, argu), scope;
        if (expr.compareTo("this") != 0) {
            scope = expr;
        } else {
            if (!argu.scope.contains("->")) throw new Exception();
            scope = argu.scope.split("->")[0];
        }
        String name = n.f2.accept(this, argu);
        classInfo classI;
        if ((classI = argu.globalST.get(scope)) == null) throw new Exception();
        methodInfo methodI = null;
        while (classI != null) {
            if ((methodI = classI.methods.get(name)) != null) break;
            classI = classI.superclass;
        }
        if (methodI == null) throw new Exception();
        // for (Map<String, methodInfo> node : classI.methods) if ((methodI = node.get(name)) != null) break;
        // if (methodI == null) throw new Exception();
        if (n.f4.present()) {
            String[] parameters = n.f4.accept(this, argu).split(", ");
            if (parameters.length != methodI.argNum) throw new Exception();
            String[] argumetns = methodI.argTypes.split(", ");
            for (int i = 0; i < parameters.length; i++) if (parameters[i].compareTo(argumetns[i]) != 0) throw new Exception();
        } else if (methodI.argNum != 0) throw new Exception();
        return methodI.returnValue;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    @Override
    public String visit(ExpressionList n, TCArgs argu) throws Exception {
        return n.f0.accept(this, argu) + n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionTerm n, TCArgs argu) throws Exception {
        return ", " + n.f1.accept(this, argu);
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    @Override
    public String visit(PrimaryExpression n, TCArgs argu) throws Exception {
        String type = n.f0.accept(this, argu);
        if (!isValidType(type, argu)) type = resolveIdentifier(type, argu);
        if (type == null) throw new Exception();
        return type;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n, TCArgs argu) throws Exception {
        return "int";
    }

    /**
     * f0 -> "true"
     */
    @Override
    public String visit(TrueLiteral n, TCArgs argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    @Override
    public String visit(FalseLiteral n, TCArgs argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    @Override
    public String visit(Identifier n, TCArgs argu) throws Exception {
        return n.f0.toString();
    }

    /**
     * f0 -> "this"
     */
    @Override
    public String visit(ThisExpression n, TCArgs argu) throws Exception {
        return "this";
    }

    /**
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public String visit(BooleanArrayAllocationExpression n, TCArgs argu) throws Exception {
        if (!isAcceptable("int", n.f3.accept(this, argu), argu)) throw new Exception();
        return "boolean[]";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public String visit(IntegerArrayAllocationExpression n, TCArgs argu) throws Exception {
        if (!isAcceptable("int", n.f3.accept(this, argu), argu)) throw new Exception();
        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n, TCArgs argu) throws Exception {
        String type = n.f1.accept(this, argu);
        // if (!argu.globalST.containsKey(type)) throw new Exception();
        if (!isValidType(type, argu)) throw new Exception();
        return type;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    @Override
    public String visit(NotExpression n, TCArgs argu) throws Exception {
        if (!isAcceptable("boolean", n.f1.accept(this, argu), argu)) throw new Exception();
        return "boolean";
    }
}
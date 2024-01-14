import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AST implements Parser{
    private final List<Token> tokens;
    private int i =0;
    private Token preanalisis;

    private final List<TipoToken> primary_exp = Arrays.asList(
            TipoToken.TRUE,
            TipoToken.FALSE,
            TipoToken.NULL,
            TipoToken.NUMBER,
            TipoToken.STRING,
            TipoToken.IDENTIFIER,
            TipoToken.LEFT_PAREN);
    private final List<TipoToken> stmt_list = Arrays.asList(
            TipoToken.FOR,
            TipoToken.IF,
            TipoToken.PRINT,
            TipoToken.RETURN,
            TipoToken.WHILE,
            TipoToken.LEFT_BRACE
    );
    public AST(List<Token> tokens){
        this.tokens = tokens;
        preanalisis = this.tokens.get(i);
    }

    @Override
    public boolean parse() {
        List<Statement> stmts = program();
        return false;
    }

    public List<Statement> program(){
        List<Statement> program = new ArrayList<>();

        if(preanalisis.tipo!=TipoToken.EOF){
            List<Statement> res = declaration(program);
            return res;
        }
        return null;
    }

    //Declaraciones

    private List<Statement> declaration(List<Statement> program){
        switch (preanalisis.tipo){
            case FUN:
                Statement stmt = fun_decl();
                program.add(stmt);
                return declaration(program);
            case VAR:
                stmt = var_decl();
                program.add(stmt);
                return declaration(program);
        }
        if(primary_exp.contains(preanalisis.tipo) || stmt_list.contains(preanalisis.tipo)){
            Statement stmt = statement();
            program.add(stmt);
            return declaration(program);
        }
        return program;
    }

    private Statement fun_decl(){
        match(TipoToken.FUN);
        Statement fun = function();
        return fun;
    }

    private Statement var_decl(){
        match(TipoToken.VAR);
        match(TipoToken.IDENTIFIER);
        Token id = previous();
        Expression expr = var_init();
        match(TipoToken.SEMICOLON);
        return new StmtVar(id,expr);
    }

    private Expression var_init(){
        if(preanalisis.tipo==TipoToken.EQUAL){
            match(TipoToken.EQUAL);
            Expression expr = expression();
            return expr;
        }
        return null;
    }

    //Sentencias
    private Statement statement(){
        if(primary_exp.contains(preanalisis.tipo)){
            return expr_stmt();
        }
        switch (preanalisis.tipo){
            case FOR:
                return for_stmt();
            case IF:
                return if_stmt();
            case PRINT:
                return print_stmt();
            case RETURN:
                return return_stmt();
            case WHILE:
                return while_stmt();
            case LEFT_BRACE:
                return block();
        }
        return null;
    }

    private Statement expr_stmt(){
        Expression expr = expression();
        match(TipoToken.SEMICOLON);
        return new StmtExpression(expr);
    }

    private Statement for_stmt(){
        match(TipoToken.FOR);
        match(TipoToken.LEFT_PAREN);
        Statement stmt1 = for_stmt1();//donde inicia
        Expression stmt2 = for_stmt2();//hasta donde
        Expression stmt3 = for_stmt3();//de cuanto en cuanto
        match(TipoToken.RIGHT_PAREN);
        Statement body = statement();
        //de cuanto en cuanto va
        if(stmt3!=null){
            body = new StmtBlock(Arrays.asList(body, new StmtExpression(stmt3)));
        }
        //por si no se especifica hasta donde
        if(stmt2==null){
            stmt2 = new ExprLiteral(true);
        }
        body = new StmtLoop(stmt2, body);
        //si el inicio no esta vacio
        if(stmt1!=null){
            body = new StmtBlock(Arrays.asList(stmt1, body));
        }
        return body;
    }

    private Statement for_stmt1(){
        if(preanalisis.tipo==TipoToken.VAR){
            return var_decl();
        }else if(primary_exp.contains(preanalisis.tipo)){
            return expr_stmt();
        }
        match(TipoToken.SEMICOLON);
        return null;
    }

    private Expression for_stmt2(){
        if (primary_exp.contains(preanalisis.tipo)){
            Expression expr = expression();
            match(TipoToken.SEMICOLON);
            return expr;
        }
        match(TipoToken.SEMICOLON);
        return null;
    }

    private Expression for_stmt3(){
        if(primary_exp.contains(preanalisis.tipo)){
            Expression expr = expression();
            return expr;
        }
        return null;
    }

    private Statement if_stmt(){
        match(TipoToken.IF);
        match(TipoToken.LEFT_PAREN);
        Expression if_exp = expression();
        match(TipoToken.RIGHT_PAREN);
        Statement stmt_exp = statement();
        Statement else_exp = else_stmt();
        return new StmtIf(if_exp, stmt_exp, else_exp);
    }

    private Statement else_stmt(){
        if(preanalisis.tipo==TipoToken.ELSE){
            match(TipoToken.ELSE);
            Statement stmt = statement();
            return stmt;
        }
        return null;
    }

    private Statement print_stmt(){
        match(TipoToken.PRINT);
        Expression print_exp = expression();
        match(TipoToken.SEMICOLON);
        return new StmtPrint(print_exp);
    }

    private Statement return_stmt(){
        match(TipoToken.RETURN);
        Expression return_exp = return_exp_opc();
        match(TipoToken.SEMICOLON);
        return new StmtReturn(return_exp);
    }

    private Expression return_exp_opc(){
        if(primary_exp.contains(preanalisis.tipo)){
            return expression();
        }
        return null;
    }

    private Statement while_stmt(){
        match(TipoToken.WHILE);
        match(TipoToken.LEFT_PAREN);
        Expression cond = expression();
        match(TipoToken.RIGHT_PAREN);
        Statement body = statement();
        return new StmtLoop(cond, body);
    }
    private Statement block(){
        match(TipoToken.LEFT_BRACE);
        List<Statement> stmts = new ArrayList<>();
        stmts = declaration(stmts);
        match(TipoToken.RIGHT_BRACE);
        return new StmtBlock(stmts);
    }

    //Expresiones
    private Expression expression(){
        Expression expr = assigment();
        return expr;
    }

    private Expression assigment(){
        Expression expr = logic_or();
        expr = assigment_opc(expr);
        return expr;
    }

    private Expression assigment_opc(Expression expr){
        switch (preanalisis.tipo){
            case EQUAL:
                if(expr instanceof ExprVariable){
                    match(TipoToken.EQUAL);
                    Expression expr2 = expression();
                    return new ExprAssign(((ExprVariable) expression()).name, expr2);
                }
        }
        return expr;
    }

    private Expression logic_or(){
        Expression expr = logic_and();
        expr = logic_or2(expr);
        return expr;
    }

    private Expression logic_or2(Expression expr){
        switch (preanalisis.tipo){
            case OR:
                match(TipoToken.OR);
                Token operador = previous();
                Expression expr2 = logic_and();
                ExprLogical expl = new ExprLogical(expr, operador, expr2);
                return logic_and2(expl);
        }
        return expr;
    }

    private Expression logic_and(){
        Expression expr = equality();
        expr = logic_and2(expr);
        return expr;
    }

    private Expression logic_and2(Expression expr){
        switch (preanalisis.tipo){
            case AND:
                match(TipoToken.AND);
                Token operador = previous();
                Expression expr2 = equality();
                ExprLogical expl = new ExprLogical(expr, operador, expr2);
                return logic_and2(expl);
        }
        return expr;
    }
    private Expression equality(){
        Expression expr = comparison();
        expr = equality2(expr);
        return expr;
    }

    private Expression equality2(Expression expr){
        switch (preanalisis.tipo){
            case BANG_EQUAL:
                match(TipoToken.BANG_EQUAL);
                Token operador = previous();
                Expression expr2 = comparison();
                ExprBinary expb = new ExprBinary(expr, operador, expr2);
                return equality2(expb);
            case EQUAL_EQUAL:
                match(TipoToken.EQUAL_EQUAL);
                operador = previous();
                expr2 = comparison();
                expb = new ExprBinary(expr, operador, expr2);
                return equality2(expb);
        }
        return expr;
    }

    private Expression comparison(){
        Expression expr = term();
        expr = comparison2(expr);
        return expr;
    }

    private Expression comparison2(Expression expr){
        switch (preanalisis.tipo){
            case GREATER:
                match(TipoToken.GREATER);
                Token operador = previous();
                Expression expr2 = term();
                ExprBinary expb = new ExprBinary(expr, operador, expr2);
                return comparison2(expb);
            case GREATER_EQUAL:
                match(TipoToken.GREATER_EQUAL);
                operador = previous();
                expr2 = term();
                expb = new ExprBinary(expr, operador, expr2);
                return comparison2(expb);
            case LESS:
                match(TipoToken.LESS);
                operador = previous();
                expr2 = term();
                expb = new ExprBinary(expr, operador, expr2);
                return comparison2(expb);
            case LESS_EQUAL:
                match(TipoToken.LESS_EQUAL);
                operador = previous();
                expr2 = term();
                expb = new ExprBinary(expr, operador, expr2);
                return comparison2(expb);
        }
        return expr;
    }

    private Expression term(){//no es que tenga que ser void, asi lo dejo para decir que continuaramos nostros para arriba
        Expression expr = factor();
        expr = term2(expr);
        return expr;
    }

    private Expression term2(Expression expr){
        switch(preanalisis.tipo){
            case MINUS:
                match(TipoToken.MINUS);
                Token operador = previous();
                Expression expr2 = factor();
                ExprBinary expb = new ExprBinary(expr, operador, expr2);
                return term2(expb);
            case STAR:
                match(TipoToken.STAR);
                operador = previous();
                expr2 = factor();
                expb = new ExprBinary(expr, operador, expr2);
                return factor2(expb);

        }
        return expr;
    }

    private Expression factor(){
        Expression expr = unary();
        expr = factor2(expr);
        return expr;
    }

    private Expression factor2(Expression expr){
        switch (preanalisis.tipo){
            case SLASH:
                match(TipoToken.SLASH);
                Token operador = previous();
                Expression expr2 = unary();
                ExprBinary expb = new ExprBinary(expr, operador, expr2);
                return factor2(expb);
            case STAR:
                match(TipoToken.STAR);
                operador = previous();
                expr2 = unary();
                expb = new ExprBinary(expr, operador, expr2);
                return factor2(expb);
        }
        return expr;
    }

    private Expression unary(){
        switch (preanalisis.tipo){
            case BANG:
                match(TipoToken.BANG);
                Token operador = previous();
                Expression expr = unary();
                return new ExprUnary(operador, expr);
            case MINUS:
                match(TipoToken.MINUS);
                operador = previous();
                expr = unary();
                return new ExprUnary(operador, expr);
            default:
                return call();
        }
    }

    private Expression call(){
        Expression expr = primary();
        expr = call2(expr);
        return expr;
    }

    private Expression call2(Expression expr){
        switch (preanalisis.tipo){
            case LEFT_PAREN:
                match(TipoToken.LEFT_PAREN);
                List<Expression> lstArguments = argumentsOptional();
                match(TipoToken.RIGHT_PAREN);
                ExprCallFunction ecf = new ExprCallFunction(expr, lstArguments);
                return call2(ecf);
        }
        return expr;
    }
    private Expression primary(){
        switch (preanalisis.tipo){
            case TRUE:
                match(TipoToken.TRUE);
                return new ExprLiteral(true);
            case FALSE:
                match(TipoToken.FALSE);
                return new ExprLiteral(false);
            case NULL:
                match(TipoToken.NULL);
                return new ExprLiteral(null);
            case NUMBER:
                match(TipoToken.NUMBER);
                Token numero = previous();
                return new ExprLiteral(numero.literal);
            case STRING:
                match(TipoToken.STRING);
                Token cadena = previous();
                return new ExprLiteral(cadena.literal);
            case IDENTIFIER:
                match(TipoToken.IDENTIFIER);
                Token id = previous();
                return new ExprVariable(id);
            case LEFT_PAREN:
                match(TipoToken.LEFT_PAREN);
                Expression expr = expression();
                // Tiene que ser cachado aquello que retorna
                match(TipoToken.RIGHT_PAREN);
                return new ExprGrouping(expr);
        }
        return null;

    }

    //otras

    private Statement function(){
        match(TipoToken.IDENTIFIER);
        Token id = previous();
        match(TipoToken.LEFT_PAREN);
        List<Token> param = parameters_opc();
        match(TipoToken.RIGHT_PAREN);
        Statement block = block();
        return new StmtFunction(id, param, (StmtBlock) block);
    }

    private List<Token> parameters_opc(){
        List<Token> param = new ArrayList<>();
        if(preanalisis.tipo==TipoToken.IDENTIFIER){
            param = parameters(param);
            return param;
        }
        return null;
    }

    private List<Token> parameters(List<Token> param){
        match(TipoToken.IDENTIFIER);
        Token id = previous();
        param.add(id);
        param = parameters2(param);
        return param;
    }

    private List<Token> parameters2(List<Token> param){
        if(preanalisis.tipo==TipoToken.COMMA){
            match(TipoToken.COMMA);
            match(TipoToken.IDENTIFIER);
            Token id = previous();
            param.add(id);
            return parameters2(param);
        }
        return null;
    }

    private List<Expression> argumentsOptional(){
        if(primary_exp.contains(preanalisis.tipo)){
            Expression expr = expression();
            List<Expression> args = new ArrayList<>();
            args.add(expr);
            arguments(args);
            return args;
        }
        return null;
    }

    private List<Expression> arguments(List<Expression> args){
        if(preanalisis.tipo==TipoToken.COMMA){
            match(TipoToken.COMMA);
            Expression expr = expression();
            args.add(expr);
            arguments(args);
        }
        return null;
    }

    private void match(TipoToken tt) {
        if(preanalisis.tipo==tt){
            i++;
            preanalisis = tokens.get(i);
        }else{
            String message = "Error en la línea. Se esperaba "
                    + preanalisis.tipo +
                    " pero se encontró " + tt;
            Interprete err = new Interprete();
            err.error(0,message);
        }
    }

    private Token previous(){return this.tokens.get(i-1);}
}

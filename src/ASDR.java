import java.util.List;
public class ASDR implements Parser{
    private int i =0;
    private boolean hayErrores =false;
    private Token preanalisis;
    private final List<Token> tokens;

    public ASDR(List<Token> tokens){
        this.tokens = tokens;
        preanalisis = this.tokens.get(i);
    }

    @Override
    public boolean parse() {
        program();

        if(preanalisis.tipo == TipoToken.EOF && !hayErrores){
            System.out.println("Sintaxis correcta");
        }else{
            System.out.println("Se encontraron errores");
        }
        return false;
    }

    //PROGRAM -> DECLARATION
    private void program(){
        declaration();
    }

    //DECLARATION -> FUN_DECL DECLARATION | VAR_DECL DECLARATION | STATEMENT DECLARATION | Ɛ
    private void declaration(){
        if(hayErrores)
            return;
        if(preanalisis.tipo == TipoToken.FUN){//Primero de fun_decl
            fun_decl();
            declaration();
        }else if(preanalisis.tipo == TipoToken.VAR || preanalisis.tipo == TipoToken.IDENTIFIER){//primero de var_decl
            var_decl();
            declaration();
        }else if(primero_statement()){//Primero de statement
            statement();
            declaration();
        }
    }

    //FUN_DECL -> fun FUNCTION
    private void fun_decl(){
        if(hayErrores)
            return;
        if(preanalisis.tipo == TipoToken.FUN){
            match(TipoToken.FUN);
            function();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'fun'");
        }
    }

    //VAR_DECL -> var id VAR_INIT ;
    private void var_decl(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.VAR){
            match(TipoToken.VAR);
            match(TipoToken.IDENTIFIER);
            var_init();
            match(TipoToken.SEMICOLON);
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'var'");
        }
    }

    //VAR_INIT -> = EXPRESSION | Ɛ
    private void var_init(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.EQUAL){
            match(TipoToken.EQUAL);
            expression();
        }
    }

    //-----------------------Sentencias----------------------

    //STATEMENT -> EXPR_STMT | FOR_STMT | IF_STMT | PRINT_STMT | RETURN_STMT | WHILE_STMT | BLOCK
    private void statement(){
        if(hayErrores)
            return;
        if(primero_expr_stmt()){
            expr_stmt();
        }else if(preanalisis.tipo==TipoToken.FOR){//Primero de for_stmt
            for_stmt();
        }else if(preanalisis.tipo==TipoToken.IF){//Primero de if_stmt
            if_stmt();
        }else if(preanalisis.tipo==TipoToken.PRINT){//Primero de print_stmt
            print_stmt();
        }else if(preanalisis.tipo==TipoToken.RETURN){//Primero de return_stmt
            return_stmt();
        }else if(preanalisis.tipo==TipoToken.WHILE){//Primero de while_stmt
            while_stmt();
        }else if(preanalisis.tipo==TipoToken.RIGHT_BRACE){//Primero de block
            block();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'for' | 'if' | 'print' | 'return' | 'while' | '{' | P(EXPR_STMT)");
        }
    }

    //EXPR_STMT -> EXPRESSION ;
    private void expr_stmt(){
        if(hayErrores)
            return;
        if(primero_expr_stmt()){
            expression();
            match(TipoToken.SEMICOLON);
        }else{
            hayErrores=true;
            System.out.println("Se esperaba el P(EXPRESSION)");
        }
    }

    //FOR_STMT -> for ( FOR_STMT_1 FOR_STMT_2 FOR_STMT_3 ) STATEMENT
    private void for_stmt(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.FOR){
            match(TipoToken.FOR);
            match(TipoToken.LEFT_PAREN);
            for_stmt_1();
            for_stmt_2();
            for_stmt_3();
            match(TipoToken.RIGHT_PAREN);
            statement();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'for'");
        }
    }

    //FOR_STMT_1 -> VAR_DECL | EXPR_STMT | ;
    private void for_stmt_1(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.VAR){
            var_decl();
        }else if(primero_expr_stmt()){
            expr_stmt();
        }else if(preanalisis.tipo==TipoToken.SEMICOLON){
            match(TipoToken.SEMICOLON);
        }
    }

    //FOR_STMT_2 -> EXPRESSION | ;
    private void for_stmt_2(){
        if(hayErrores)
            return;
        if(primero_expr_stmt()){
            expression();
        }else if(preanalisis.tipo==TipoToken.SEMICOLON){
            match(TipoToken.SEMICOLON);
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un ';' o P(EXPRESSION)");
        }
    }

    //FOR_STMT_3 -> EXPRESSION | Ɛ
    private void for_stmt_3(){
        if(hayErrores)
            return;
        if(primero_expr_stmt()){
            expression();
        }
    }

    //IF_STMT -> if ( EXPRESSION ) STATEMENT ELSE_STATEMENT
    private void if_stmt(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.IF){
            match(TipoToken.IF);
            match(TipoToken.LEFT_PAREN);
            expression();
            match(TipoToken.RIGHT_PAREN);
            statement();
            else_statement();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'if'");
        }
    }

    //ELSE_STATEMENT -> else STATEMENT | Ɛ
    private void else_statement(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.ELSE){
            match(TipoToken.ELSE);
            statement();
        }
    }

    //PRINT_STMT -> print EXPRESSION ;
    private void print_stmt(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.PRINT){
            match(TipoToken.PRINT);
            expression();
            match(TipoToken.SEMICOLON);
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'print'");
        }
    }

    //RETURN_STMT -> return RETURN_EXP_OPC ;
    private void return_stmt(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.RETURN){
            match(TipoToken.RETURN);
            return_exp_opc();
            match(TipoToken.SEMICOLON);
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'return'");
        }
    }

    //RETURN_STMT_OPC -> EXPRESSION | Ɛ
    private void return_exp_opc(){
        if (hayErrores)
            return;
        if(primero_expr_stmt()){
            expression();
        }
    }

    //WHILE_STMT -> while ( EXPRESSION ) STATEMENT
    private void while_stmt(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.WHILE){
            match(TipoToken.WHILE);
            match(TipoToken.LEFT_PAREN);
            expression();
            match(TipoToken.RIGHT_PAREN);
            statement();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un P(EXPRESSION)");
        }
    }

    //BLOCK -> { DECLARATION }
    private void block(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.LEFT_BRACE){
            match(TipoToken.LEFT_BRACE);
            declaration();
            match(TipoToken.RIGHT_BRACE);
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un '{'");
        }
    }

    //--------------------EXPRESIONES-----------------------
    //EXPRESSION -> ASSIGMENT
    private void expression(){
        if(hayErrores)
            return;
        if(primero_expr_stmt()){
            assigment();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un P(ASSIGMENT)");
        }
    }

    //ASSIGMENT -> LOGIC_OR ASSIGMENT_OPC
    private void assigment(){
        if(hayErrores)
            return;
        logic_or();
        assigment_opc();
    }

    //ASSIGMENT_OPC -> = EXPRESSION | Ɛ
    private void assigment_opc(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.EQUAL){
            match(TipoToken.EQUAL);
            expression();
        }
    }
    //LOGIC_OR -> LOGIC_AND LOGIC_OR_2
    private void logic_or(){
        if (hayErrores)
            return;
        logic_and();
        logic_or_2();
    }

    //LOGIC_OR_2 -> or LOGIC_AND LOGIC_OR_2 | Ɛ
    private void logic_or_2(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.OR){
            match(TipoToken.OR);
            logic_and();
            logic_or_2();
        }
    }

    //LOGIC_AND -> EQUALITY LOGIC_AND_2
    private void logic_and(){
        if(hayErrores)
            return;
        equality();
        logic_and_2();
    }

    //LOGIC_AND_2 -> and EQUALITY LOGIC_AND_2 | Ɛ
    private void logic_and_2(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.AND){
            match(TipoToken.AND);
            equality();
            logic_and_2();
        }
    }

    //EQUALITY -> COMPARISON EQUALITY_2
    private void equality(){
        if(hayErrores)
            return;
        comparison();
        equality_2();
    }

    //EQUALITY_2 -> != COMPARISON EQUALITY_2 | == COMPARISON EQUALITY_2 | Ɛ
    private void equality_2(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.BANG){
            match(TipoToken.BANG);
            match(TipoToken.EQUAL);
            comparison();
            equality_2();
        }else if(preanalisis.tipo==TipoToken.EQUAL_EQUAL){
            match(TipoToken.EQUAL_EQUAL);
            comparison();
            equality_2();
        }
    }

    //COMPARISON -> TERM COMPARISON_2
    private void comparison(){
        if(hayErrores)
            return;
        term();
        comparison_2();
    }

    //COMPARISON_2 -> > TERM COMPARISON_2 | >= TERM COMPARISON_2
    private void comparison_2(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.GREATER){
            match(TipoToken.GREATER);
            term();
            comparison_2();
        }else if(preanalisis.tipo==TipoToken.GREATER_EQUAL){
            match(TipoToken.GREATER_EQUAL);
            term();
            comparison_2();
        }else if(preanalisis.tipo==TipoToken.LESS){
            match(TipoToken.LESS);
            term();
            comparison_2();
        }else if(preanalisis.tipo==TipoToken.LESS_EQUAL){
            match(TipoToken.LESS_EQUAL);
            term();
            comparison_2();
        }
    }

    //TERM -> FACTOR TERM_2
    private void term(){
        if(hayErrores)
            return;
        factor();
        term_2();
    }

    //TERM_2 -> - FACTOR TERM_2 | + FACTOR TERM_2 | Ɛ
    private void term_2(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.MINUS){
            match(TipoToken.MINUS);
            factor();
            term_2();
        }else if(preanalisis.tipo==TipoToken.PLUS){
            match(TipoToken.PLUS);
            factor();
            term_2();
        }
    }

    //FACTOR -> UNARY FACTOR_2
    private void factor(){
        if(hayErrores)
            return;
        unary();
        factor_2();
        /*if(primero_expr_stmt()){
            unary();
            factor_2();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un P(UNARY)");
        }*/
    }

    //FACTOR_2 -> / UNARY FACTOR_2 | * UNARY FACTOR_2 | Ɛ
    private void factor_2(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.SLASH){
            match(TipoToken.SLASH);
            unary();
            factor_2();
        }else if(preanalisis.tipo==TipoToken.STAR){
            match(TipoToken.STAR);
            unary();
            factor_2();
        }
    }

    //UNARY -> ! UNARY | - UNARY | CALL
    private void unary(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.BANG){
            match(TipoToken.BANG);
            unary();
        }else if(preanalisis.tipo==TipoToken.MINUS){
            match(TipoToken.MINUS);
            unary();
        }else if(primero_expr_stmt()){
            call();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un '!' o '-' o P(CALL)");
        }
    }

    //CALL -> PRIMARY CALL_2
    private void call(){
        if(hayErrores)
            return;
        primary();
        call_2();
    }

    //CALL_2 -> ( ARGUMENTS_OPC ) CALL_2 | Ɛ
    private void call_2(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.LEFT_PAREN){
            match(TipoToken.LEFT_PAREN);
            arguments_opc();
            match(TipoToken.RIGHT_PAREN);
            call_2();
        }
    }

    //PRIMARY -> true | false | null | number | string | id | ( EXPRESSION )
    private void primary(){
        if (hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.TRUE){
            match(TipoToken.TRUE);
        }else if(preanalisis.tipo==TipoToken.FALSE){
            match(TipoToken.FALSE);
        }else if(preanalisis.tipo==TipoToken.NULL){
            match(TipoToken.NULL);
        }else if(preanalisis.tipo==TipoToken.NUMBER){
            match(TipoToken.NUMBER);
        }else if(preanalisis.tipo==TipoToken.STRING){
            match(TipoToken.STRING);
        }else if(preanalisis.tipo==TipoToken.IDENTIFIER){
            match(TipoToken.IDENTIFIER);
        }else if(preanalisis.tipo==TipoToken.LEFT_PAREN){
            match(TipoToken.LEFT_PAREN);
            expression();
            match(TipoToken.RIGHT_PAREN);
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'true' o 'false' o 'null' o 'number' o 'string' o 'id' o '('");
        }
    }

    //FUNCTION -> id ( PARAMETERS_OPC ) BLOCK
    private void function(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.IDENTIFIER){
            match(TipoToken.IDENTIFIER);
            match(TipoToken.LEFT_PAREN);
            parameters_opc();
            match(TipoToken.RIGHT_PAREN);
            block();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'id'");
        }
    }

    //FUNCTIONS -> FUN_DECL FUNCTIONS | Ɛ
    private void functions(){
        if (hayErrores)
            return;
        fun_decl();
        functions();
    }

    //PARAMETERS_OPC -> PARAMETERS | Ɛ
    private void parameters_opc(){
        if(hayErrores)
            return;
        parameters();
    }

    //PARAMETERS -> id PARAMETERS_2
    private void parameters(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.IDENTIFIER){
            match(TipoToken.IDENTIFIER);
            parameters_2();
        }else{
            hayErrores=true;
            System.out.println("Se esperaba un 'id'");
        }
    }

    //PARAMETERS_2 -> , id PARAMETERS_2 | Ɛ
    private void parameters_2(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.COMMA){
            match(TipoToken.COMMA);
            match(TipoToken.IDENTIFIER);
            parameters_2();
        }
    }

    //ARGUMENTS_OPC -> EXPRESSION ARGUMENTS
    private void arguments_opc(){
        if(hayErrores)
            return;
        expression();
        arguments();
    }

    //ARGUMENTS -> , EXPRESSION ARGUMENTS
    private void arguments(){
        if(hayErrores)
            return;
        if(preanalisis.tipo==TipoToken.COMMA){
            match(TipoToken.COMMA);
            expression();
            arguments();
        }
    }
    private boolean primero_expr_stmt(){//P(EXPR_STMT) = P(EXPRESSION) = P(ASSIGMENT) = P(LOGIC_OR) = P(LOGIC_AND) = P(EQUALITY)
                                        //= P(COMPARISON) = P(TERM) = P(FACTOR) = P(UNARY) = P(CALL) = P(PRIMARY)
        if(preanalisis.tipo== TipoToken.BANG ||
                preanalisis.tipo==TipoToken.MINUS ||
                preanalisis.tipo==TipoToken.TRUE ||
                preanalisis.tipo==TipoToken.FALSE ||
                preanalisis.tipo==TipoToken.NULL ||
                preanalisis.tipo==TipoToken.NUMBER ||
                preanalisis.tipo==TipoToken.STRING ||
                preanalisis.tipo==TipoToken.IDENTIFIER ||
                preanalisis.tipo==TipoToken.RIGHT_PAREN){
            return true;
        }else{
            return false;
        }
    }
    private boolean primero_statement(){
        if(preanalisis.tipo== TipoToken.BANG ||
            preanalisis.tipo==TipoToken.MINUS ||
            preanalisis.tipo==TipoToken.TRUE ||
            preanalisis.tipo==TipoToken.FALSE ||
                preanalisis.tipo==TipoToken.NULL ||
                preanalisis.tipo==TipoToken.NUMBER ||
                preanalisis.tipo==TipoToken.STRING ||
                preanalisis.tipo==TipoToken.IDENTIFIER ||
                preanalisis.tipo==TipoToken.RIGHT_PAREN ||
                preanalisis.tipo==TipoToken.FOR ||
                preanalisis.tipo==TipoToken.IF ||
                preanalisis.tipo==TipoToken.PRINT ||
                preanalisis.tipo==TipoToken.RETURN ||
                preanalisis.tipo==TipoToken.WHILE ||
                preanalisis.tipo==TipoToken.RIGHT_BRACE){
            return true;
        }else{
            return false;
        }

    }
    private void match(TipoToken tt){
        if(preanalisis.tipo == tt){
            i++;
            preanalisis = tokens.get(i);
        }else{
            hayErrores=true;
            System.out.println("Error encontrado");
        }
    }
}

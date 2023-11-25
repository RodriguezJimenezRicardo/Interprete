import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {

    private static final Map<String, TipoToken> palabrasReservadas;

    static {
        palabrasReservadas = new HashMap<>();
        palabrasReservadas.put("and",    TipoToken.AND);
        palabrasReservadas.put("else",   TipoToken.ELSE);
        palabrasReservadas.put("false",  TipoToken.FALSE);
        palabrasReservadas.put("for",    TipoToken.FOR);
        palabrasReservadas.put("fun",    TipoToken.FUN);
        palabrasReservadas.put("if",     TipoToken.IF);
        palabrasReservadas.put("null",   TipoToken.NULL);
        palabrasReservadas.put("or",     TipoToken.OR);
        palabrasReservadas.put("print",  TipoToken.PRINT);
        palabrasReservadas.put("return", TipoToken.RETURN);
        palabrasReservadas.put("true",   TipoToken.TRUE);
        palabrasReservadas.put("var",    TipoToken.VAR);
        palabrasReservadas.put("while",  TipoToken.WHILE);
    }

    private final String source;

    private final List<Token> tokens = new ArrayList<>();

    public Scanner(String source){
        this.source = source + " ";
    }

    public List<Token> scan() throws Exception {
        String lexema = "";
        int estado = 0,linea=0;
        char c;
        Token t;

        for(int i=0; i<source.length(); i++){
            c = source.charAt(i);

            switch (estado){
                case 0:
                    if(Character.isLetter(c)){
                        estado = 13;
                        lexema += c;
                    }
                    else if(Character.isDigit(c)){
                        estado = 15;
                        lexema += c;

                    }else if(c=='"'){
                        estado=24;
                        lexema+=c;
                    }else if(c=='/'){
                        estado=26;
                        lexema+=c;
                    }else if(c=='>'){
                        estado=1;
                        lexema+=c;
                    }else if(c=='<'){
                        estado=4;
                        lexema+=c;
                    }else if(c=='='){
                        estado=7;
                        lexema+=c;
                    }else if(c=='!'){
                        estado=10;
                        lexema+=c;
                    }else if(c=='('){
                        estado=31;
                        lexema+=c;
                    }else if(c==')'){
                        estado=32;
                        lexema+=c;
                    }else if(c=='{'){
                        estado=33;
                        lexema+=c;
                    }else if(c=='}'){
                        estado=34;
                        lexema+=c;
                    }else if(c==','){
                        estado=35;
                        lexema+=c;
                    }else if(c=='.'){
                        estado=36;
                        lexema+=c;
                    }else if(c=='-'){
                        estado=37;
                        lexema+=c;
                    }else if(c=='+'){
                        estado=38;
                        lexema+=c;
                    }else if(c==';'){
                        estado=39;
                        lexema+=c;
                    }else if(c=='*'){
                        estado=40;
                        lexema+=c;
                    }else if(c=='\n'||c=='\r'){
                        linea++;
                    }else if(c==' '){
                        estado=0;
                    }else{
                        //caracter no valido
                        Interprete.error(linea-1, "El caracter "+c+" no es aceptado en este lenguaje");
                        estado=0;
                    }

                    break;
                case 1:
                    if(c=='='){
                        //token greater equal
                        lexema+=c;

                        t = new Token(TipoToken.GREATER_EQUAL, lexema);
                        tokens.add(t);

                        estado = 0;
                        lexema = "";

                    }else{
                        //token greater
                        t = new Token(TipoToken.GREATER, lexema);
                        tokens.add(t);

                        estado=0;
                        lexema="";
                        i--;
                    }
                    break;
                case 4:
                    if(c=='='){
                        //token less equal
                        lexema+=c;

                        t = new Token(TipoToken.LESS_EQUAL, lexema);
                        tokens.add(t);

                        estado = 0;
                        lexema = "";

                    }else{
                        //token less
                        t = new Token(TipoToken.LESS, lexema);
                        tokens.add(t);

                        estado=0;
                        lexema="";
                        i--;
                    }
                    break;
                case 7:
                    if(c=='='){
                        lexema+=c;

                        t = new Token(TipoToken.EQUAL_EQUAL,lexema);
                        tokens.add(t);

                        estado=0;
                        lexema="";

                    }else{
                        t = new Token(TipoToken.EQUAL,lexema);
                        tokens.add(t);

                        estado=0;
                        lexema="";
                        i--;
                    }
                    break;
                case 10:
                    if(c=='='){
                        lexema+=c;

                        t = new Token(TipoToken.BANG_EQUAL,lexema);
                        tokens.add(t);

                        estado=0;
                        lexema="";
                    }else{
                        t = new Token(TipoToken.BANG,lexema);
                        tokens.add(t);

                        estado=0;
                        lexema="";
                        i--;
                    }
                    break;
                case 13:
                    if(Character.isLetter(c) || Character.isDigit(c)){
                        estado = 13;
                        lexema += c;
                    }
                    else{
                        // Vamos a crear el Token de identificador o palabra reservada
                        TipoToken tt = palabrasReservadas.get(lexema);

                        if(tt == null){
                            t = new Token(TipoToken.IDENTIFIER, lexema);
                            tokens.add(t);
                        }
                        else{
                            t = new Token(tt, lexema);
                            tokens.add(t);
                        }

                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 15:
                    if(Character.isDigit(c)){
                        estado = 15;
                        lexema += c;
                    }
                    else if(c == '.'){
                        estado=16;
                        lexema+=c;
                    }
                    else if(c == 'E'){
                        estado=18;
                        lexema+=c;
                    }
                    else{
                        t = new Token(TipoToken.NUMBER, lexema, Integer.valueOf(lexema));
                        tokens.add(t);

                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 16:
                    if(Character.isDigit(c)){
                        estado=17;
                        lexema+=c;
                    }else{
                        Interprete.error(linea-1,"Numero decimal incompleto");
                        //estado=0;
                    }
                    break;
                case 17:
                    if(Character.isDigit(c)){
                        estado=17;
                        lexema+=c;
                    }else if(c=='E'){
                        estado=18;
                        lexema+=c;
                    }else{//otro
                        //Crear token numero decimal
                        t = new Token(TipoToken.NUMBER, lexema, Float.valueOf(lexema));
                        tokens.add(t);

                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 18:
                    if(c=='+'||c=='-'){
                        estado=19;
                        lexema+=c;
                    }else if(Character.isDigit(c)){
                        estado=20;
                        lexema+=c;
                    }else{
                        //Error
                        Interprete.error(linea-1,"Numero exponencial error");
                        estado=0;
                    }
                    break;
                case 19:
                    if(Character.isDigit(c)){
                        estado=20;
                        lexema+=c;
                    }else{
                        //error
                        Interprete.error(linea-1,"No es numero valido");
                        //estado=0;
                    }
                    break;
                case 20:
                    if(Character.isDigit(c)){
                        estado=20;
                        lexema+=c;
                    }else{
                        //Crear token,

                        t = new Token(TipoToken.NUMBER, lexema,Double.valueOf(lexema));
                        tokens.add(t);

                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 24:
                    if(c=='"'){
                        //Token cadena
                        lexema+=c;

                        t = new Token(TipoToken.STRING, lexema,lexema.substring(1,lexema.length()-1));
                        tokens.add(t);

                        estado=0;
                        lexema ="";

                    }else if(c=='\n'||c=='\r'){
                        //Error
                        Interprete.error(linea-1,"Cadena incompleta");
                        linea++;
                        lexema="";
                        estado=0;
                    }else{
                        //Cualquier otro
                        estado=24;
                        lexema+=c;
                    }
                    break;
                case 26:
                    if(c=='*'){
                        estado=27;

                    }else if(c=='/'){
                        estado=30;

                    }else{
                        //token slash
                        t = new Token(TipoToken.SLASH, lexema);
                        tokens.add(t);

                        estado=0;
                        lexema ="";
                        i--;
                    }
                    break;
                case 27:
                    if(c=='*'){
                        estado=28;
                    }else if(c=='\n'||c=='\r'){
                        linea++;
                        //estado=0;
                    }else{
                        estado=27;
                    }
                    break;
                case 28:
                    if(c=='*'){
                        estado=28;
                    }else if(c=='/'){
                        //No genera token,
                        estado=0;
                        lexema="";
                    }else{
                        estado=27;
                    }
                    break;
                case 30:
                    if(c=='\n'||c=='\r'){
                        //No genera token
                        linea++;
                        estado=0;
                        lexema="";
                    }else{
                        estado=30;
                    }
                    break;
                case 31:
                    t = new Token(TipoToken.LEFT_PAREN,lexema);
                    tokens.add(t);

                    estado=0;
                    lexema="";
                    i--;
                    break;
                case 32:
                    t = new Token(TipoToken.RIGHT_PAREN,lexema);
                    tokens.add(t);

                    estado=0;
                    lexema="";
                    i--;
                    break;
                case 33:
                    t = new Token(TipoToken.LEFT_BRACE,lexema);
                    tokens.add(t);
//java Interprete C:\Users\doabl\OneDrive\Escritorio\Pruebas\Prueba3.txt
                    estado=0;
                    lexema="";
                    i--;
                    break;
                case 34:
                    t=new Token(TipoToken.RIGHT_BRACE,lexema);
                    tokens.add(t);

                    estado=0;
                    lexema="";
                    i--;
                    break;
                case 35:
                    t=new Token(TipoToken.COMMA,lexema);
                    tokens.add(t);

                    estado=0;
                    lexema="";
                    i--;
                    break;
                case 36:
                    t=new Token(TipoToken.DOT,lexema);
                    tokens.add(t);

                    estado=0;
                    lexema="";
                    i--;
                    break;
                case 37:
                    t=new Token(TipoToken.MINUS,lexema);
                    tokens.add(t);

                    estado=0;
                    lexema="";
                    i--;
                    break;
                case 38:
                    t=new Token(TipoToken.PLUS,lexema);
                    tokens.add(t);

                    estado=0;
                    lexema="";
                    i--;
                    break;
                case 39:
                    t=new Token(TipoToken.SEMICOLON,lexema);
                    tokens.add(t);

                    estado=0;
                    lexema="";
                    i--;
                    break;
                case 40:
                    t=new Token(TipoToken.STAR,lexema);
                    tokens.add(t);

                    estado=0;
                    lexema="";
                    i--;
                    break;
            }
        }
        t = new Token(TipoToken.EOF,"eof");
        tokens.add(t);
        return tokens;
    }
}

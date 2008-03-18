/* Lexer for XCSP functionals */
package translator.xcsp2ep.mapper.functionalsParser;

import java_cup.runtime.*;


%%


%class FunctionalsLexer
%unicode
%cup
%line
%column
%public

/* user-declarations  */
%{

  private Symbol getSymbol(int type) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }

  private Symbol getSymbol(int type, Object value) {
    return new Symbol(type, yyline+1, yycolumn+1, value);
  }

  private void print_error_msg(String message) {
     System.out.print("\nLex error:\t"+message+"\n");
  }

%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

Integer = 0 | [1-9][0-9]*
Identifier = [:jletter:][:jletterdigit:]*



%%

/* keywords */
<YYINITIAL> "abs"            { return getSymbol(sym.ABS); }
<YYINITIAL> "add"            { return getSymbol(sym.ADD); }
<YYINITIAL> "and"       	 { return getSymbol(sym.AND); }
<YYINITIAL> "div"       	 { return getSymbol(sym.DIV); }
<YYINITIAL> "eq"             { return getSymbol(sym.EQ); }
<YYINITIAL> "false"          { return getSymbol(sym.FALSE); }
<YYINITIAL> "ge"             { return getSymbol(sym.GE); }
<YYINITIAL> "gt"             { return getSymbol(sym.GT); }
<YYINITIAL> "iff"            { return getSymbol(sym.IFF); }
<YYINITIAL> "if"             { return getSymbol(sym.IF); }
<YYINITIAL> "le"             { return getSymbol(sym.LE); }
<YYINITIAL> "lt"             { return getSymbol(sym.LT); }
<YYINITIAL> "max"            { return getSymbol(sym.MAX); }
<YYINITIAL> "min"            { return getSymbol(sym.MIN); }
<YYINITIAL> "mod"            { return getSymbol(sym.MOD); }
<YYINITIAL> "mul"          	 { return getSymbol(sym.MUL); }
<YYINITIAL> "neg"            { return getSymbol(sym.NEG); }
<YYINITIAL> "ne"             { return getSymbol(sym.NE); }
<YYINITIAL> "not"            { return getSymbol(sym.NOT); }
<YYINITIAL> "or"             { return getSymbol(sym.OR); }
<YYINITIAL> "pow"            { return getSymbol(sym.POW); }
<YYINITIAL> "sub"            { return getSymbol(sym.SUB); }
<YYINITIAL> "true"           { return getSymbol(sym.TRUE); }
<YYINITIAL> "xor"            { return getSymbol(sym.XOR); }




<YYINITIAL> {

  /* separators */

  "("                           { return getSymbol(sym.LPAREN); }
  ")"                           { return getSymbol(sym.RPAREN); }
  ","                           { return getSymbol(sym.COMMA); }
 


 
 /* Literals */ 
  {Integer} 		  	{  return getSymbol(sym.INTEGER, new Integer(yytext())); }	 


  {Identifier}   		{   return getSymbol(sym.IDENTIFIER, yytext()); }	


  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}


/* error fallback */
.|\n                             { print_error_msg("Illegal character \""+yytext()+
                                                              "\" at line "+yyline+", column "+yycolumn); 
                                                              //System.exit(0);
                                                              }



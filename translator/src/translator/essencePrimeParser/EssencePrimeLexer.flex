/* Lexer for ESSENCE' */
package translator.essencePrimeParser;

import java_cup.runtime.*;


%%


%class EssencePrimeLexer
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
Comment     = "$" {InputCharacter}* {LineTerminator}

Integer = 0 | [1-9][0-9]*
Identifier = [:jletter:][:jletterdigit:]*

Header  = "ESSENCE' 1.0"  



%%

/* keywords */
<YYINITIAL> "alldiff"            { return getSymbol(sym.ALLDIFF); }
<YYINITIAL> "allDiff"            { return getSymbol(sym.ALLDIFF); }
<YYINITIAL> "alldifferent"       { return getSymbol(sym.ALLDIFF); }
<YYINITIAL> "allDifferent"            { return getSymbol(sym.ALLDIFF); }
<YYINITIAL> "atleast"            { return getSymbol(sym.ATLEAST); }
<YYINITIAL> "atmost"             { return getSymbol(sym.ATMOST); }
<YYINITIAL> "be"                 { return getSymbol(sym.BE); }
<YYINITIAL> "be domain"          { return getSymbol(sym.BE_DOMAIN); }
<YYINITIAL> "be new type"        { return getSymbol(sym.BE_NEW_TYPE); }
<YYINITIAL> "enum"               { return getSymbol(sym.ENUM); }
<YYINITIAL> "element"            { return getSymbol(sym.ELEMENT); }
<YYINITIAL> "exists"             { return getSymbol(sym.EXISTS); }
<YYINITIAL> "false"              { return getSymbol(sym.FALSE); }
<YYINITIAL> "find"               { return getSymbol(sym.FIND); }
<YYINITIAL> "forall"             { return getSymbol(sym.FORALL); }
<YYINITIAL> "given"          	 { return getSymbol(sym.GIVEN); }
<YYINITIAL> "letting"            { return getSymbol(sym.LETTING); }
<YYINITIAL> "matrix indexed by"  { return getSymbol(sym.MATRIX_INDEXED_BY); }
<YYINITIAL> "maximising"         { return getSymbol(sym.MAXIMISING); }
<YYINITIAL> "max"                { return getSymbol(sym.MAX); }
<YYINITIAL> "minimising"         { return getSymbol(sym.MINIMISING); }
<YYINITIAL> "min"                { return getSymbol(sym.MIN); }
<YYINITIAL> "occurrence"          { return getSymbol(sym.OCCURRENCE); }
<YYINITIAL> "of"                 { return getSymbol(sym.OF); }
<YYINITIAL> "such that"          { return getSymbol(sym.SUCH_THAT); }
<YYINITIAL> "sum"                { return getSymbol(sym.SUM); }
<YYINITIAL> "true"               { return getSymbol(sym.TRUE); }
<YYINITIAL> "where"               { return getSymbol(sym.WHERE); }




<YYINITIAL> {

  /* separators */

  "("                           { return getSymbol(sym.LPAREN); }
  ")"                           { return getSymbol(sym.RPAREN); }
  "{"                            { return getSymbol(sym.LBRACE); }
  "}"                            { return getSymbol(sym.RBRACE); }
  "["                           { return getSymbol(sym.LBRACK); }
  "]"                           { return getSymbol(sym.RBRACK); }
  "."                           { return getSymbol(sym.DOT); }
  ".."                          { return getSymbol(sym.DOTDOT); }
  ","                           { return getSymbol(sym.COMMA); }
  ":"                           { return getSymbol(sym.COLON); }
  "|"                            { return getSymbol(sym.BAR); }



  /* operators */  
  /* relops */	
  "<lex"						  { return getSymbol(sym.LEXLESS); }
  ">lex"						  { return getSymbol(sym.LEXGREATER); }
  "<=lex"						  { return getSymbol(sym.LEXLEQ); }
  ">=lex"						  { return getSymbol(sym.LEXGEQ); }
  "="                            { return getSymbol(sym.EQ); }
  "!="                           { return getSymbol(sym.NEQ); }
  "<"                            { return getSymbol(sym.LESS); }
  "<="                            { return getSymbol(sym.LEQ); }
  ">"                            { return getSymbol(sym.GREATER); }
  ">="                            { return getSymbol(sym.GEQ); }

  /* mulops */
  "+"                            { return getSymbol(sym.PLUS); }
  "-"                            { return getSymbol(sym.MINUS); }
  "*"                            { return getSymbol(sym.MULT); }
  "/"                            { return getSymbol(sym.DIVISION); }
  "^"                            { return getSymbol(sym.POWER); }

  	

  /* boolean ops */
  "/\\"                            { return getSymbol(sym.AND); }
  "\\/"                            { return getSymbol(sym.OR); }
  "!"                             { return getSymbol(sym.NOT); }
  "=>"                            { return getSymbol(sym.IF); }
  "<=>"                           { return getSymbol(sym.IFF); }


  {Header}			  { return getSymbol(sym.HEADER); }

  /* Literals */ 
  {Integer} 		  	{  return getSymbol(sym.INTEGER, new Integer(yytext())); }	 


  {Identifier}   		{   return getSymbol(sym.IDENTIFIER, yytext()); }	


  /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}


/* error fallback */
.|\n                             { print_error_msg("Illegal character \""+yytext()+
                                                              "\" at line "+yyline+", column "+yycolumn); 
                                                              System.exit(0);}



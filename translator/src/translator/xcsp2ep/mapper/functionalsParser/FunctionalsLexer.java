/* The following code was generated by JFlex 1.4.1 on 03/12/08 14:32 */

/* Lexer for XCSP functionals */
package translator.xcsp2ep.mapper.functionalsParser;

import java_cup.runtime.*;



/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.1
 * on 03/12/08 14:32 from the specification file
 * <tt>FunctionalsLexer.flex</tt>
 */
public class FunctionalsLexer implements java_cup.runtime.Scanner {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\7\1\3\1\2\1\0\1\3\1\1\16\7\4\0\1\3\3\0"+
    "\1\6\3\0\1\34\1\35\2\0\1\36\1\37\2\0\1\4\11\5"+
    "\7\0\32\6\4\0\1\6\1\0\1\10\1\11\1\6\1\13\1\17"+
    "\1\21\1\23\1\6\1\15\2\6\1\22\1\25\1\14\1\27\1\32"+
    "\1\20\1\31\1\12\1\24\1\30\1\16\1\33\1\26\2\6\4\0"+
    "\41\7\2\0\4\6\4\0\1\6\2\0\1\7\7\0\1\6\4\0"+
    "\1\6\5\0\27\6\1\0\37\6\1\0\u013f\6\31\0\162\6\4\0"+
    "\14\6\16\0\5\6\11\0\1\6\21\0\130\7\5\0\23\7\12\0"+
    "\1\6\13\0\1\6\1\0\3\6\1\0\1\6\1\0\24\6\1\0"+
    "\54\6\1\0\46\6\1\0\5\6\4\0\202\6\1\0\4\7\3\0"+
    "\105\6\1\0\46\6\2\0\2\6\6\0\20\6\41\0\46\6\2\0"+
    "\1\6\7\0\47\6\11\0\21\7\1\0\27\7\1\0\3\7\1\0"+
    "\1\7\1\0\2\7\1\0\1\7\13\0\33\6\5\0\3\6\15\0"+
    "\4\7\14\0\6\7\13\0\32\6\5\0\13\6\16\7\7\0\12\7"+
    "\4\0\2\6\1\7\143\6\1\0\1\6\10\7\1\0\6\7\2\6"+
    "\2\7\1\0\4\7\2\6\12\7\3\6\2\0\1\6\17\0\1\7"+
    "\1\6\1\7\36\6\33\7\2\0\3\6\60\0\46\6\13\7\1\6"+
    "\u014f\0\3\7\66\6\2\0\1\7\1\6\20\7\2\0\1\6\4\7"+
    "\3\0\12\6\2\7\2\0\12\7\21\0\3\7\1\0\10\6\2\0"+
    "\2\6\2\0\26\6\1\0\7\6\1\0\1\6\3\0\4\6\2\0"+
    "\1\7\1\6\7\7\2\0\2\7\2\0\3\7\11\0\1\7\4\0"+
    "\2\6\1\0\3\6\2\7\2\0\12\7\4\6\15\0\3\7\1\0"+
    "\6\6\4\0\2\6\2\0\26\6\1\0\7\6\1\0\2\6\1\0"+
    "\2\6\1\0\2\6\2\0\1\7\1\0\5\7\4\0\2\7\2\0"+
    "\3\7\13\0\4\6\1\0\1\6\7\0\14\7\3\6\14\0\3\7"+
    "\1\0\11\6\1\0\3\6\1\0\26\6\1\0\7\6\1\0\2\6"+
    "\1\0\5\6\2\0\1\7\1\6\10\7\1\0\3\7\1\0\3\7"+
    "\2\0\1\6\17\0\2\6\2\7\2\0\12\7\1\0\1\6\17\0"+
    "\3\7\1\0\10\6\2\0\2\6\2\0\26\6\1\0\7\6\1\0"+
    "\2\6\1\0\5\6\2\0\1\7\1\6\6\7\3\0\2\7\2\0"+
    "\3\7\10\0\2\7\4\0\2\6\1\0\3\6\4\0\12\7\1\0"+
    "\1\6\20\0\1\7\1\6\1\0\6\6\3\0\3\6\1\0\4\6"+
    "\3\0\2\6\1\0\1\6\1\0\2\6\3\0\2\6\3\0\3\6"+
    "\3\0\10\6\1\0\3\6\4\0\5\7\3\0\3\7\1\0\4\7"+
    "\11\0\1\7\17\0\11\7\11\0\1\6\7\0\3\7\1\0\10\6"+
    "\1\0\3\6\1\0\27\6\1\0\12\6\1\0\5\6\4\0\7\7"+
    "\1\0\3\7\1\0\4\7\7\0\2\7\11\0\2\6\4\0\12\7"+
    "\22\0\2\7\1\0\10\6\1\0\3\6\1\0\27\6\1\0\12\6"+
    "\1\0\5\6\2\0\1\7\1\6\7\7\1\0\3\7\1\0\4\7"+
    "\7\0\2\7\7\0\1\6\1\0\2\6\4\0\12\7\22\0\2\7"+
    "\1\0\10\6\1\0\3\6\1\0\27\6\1\0\20\6\4\0\6\7"+
    "\2\0\3\7\1\0\4\7\11\0\1\7\10\0\2\6\4\0\12\7"+
    "\22\0\2\7\1\0\22\6\3\0\30\6\1\0\11\6\1\0\1\6"+
    "\2\0\7\6\3\0\1\7\4\0\6\7\1\0\1\7\1\0\10\7"+
    "\22\0\2\7\15\0\60\6\1\7\2\6\7\7\4\0\10\6\10\7"+
    "\1\0\12\7\47\0\2\6\1\0\1\6\2\0\2\6\1\0\1\6"+
    "\2\0\1\6\6\0\4\6\1\0\7\6\1\0\3\6\1\0\1\6"+
    "\1\0\1\6\2\0\2\6\1\0\4\6\1\7\2\6\6\7\1\0"+
    "\2\7\1\6\2\0\5\6\1\0\1\6\1\0\6\7\2\0\12\7"+
    "\2\0\2\6\42\0\1\6\27\0\2\7\6\0\12\7\13\0\1\7"+
    "\1\0\1\7\1\0\1\7\4\0\2\7\10\6\1\0\42\6\6\0"+
    "\24\7\1\0\2\7\4\6\4\0\10\7\1\0\44\7\11\0\1\7"+
    "\71\0\42\6\1\0\5\6\1\0\2\6\1\0\7\7\3\0\4\7"+
    "\6\0\12\7\6\0\6\6\4\7\106\0\46\6\12\0\51\6\7\0"+
    "\132\6\5\0\104\6\5\0\122\6\6\0\7\6\1\0\77\6\1\0"+
    "\1\6\1\0\4\6\2\0\7\6\1\0\1\6\1\0\4\6\2\0"+
    "\47\6\1\0\1\6\1\0\4\6\2\0\37\6\1\0\1\6\1\0"+
    "\4\6\2\0\7\6\1\0\1\6\1\0\4\6\2\0\7\6\1\0"+
    "\7\6\1\0\27\6\1\0\37\6\1\0\1\6\1\0\4\6\2\0"+
    "\7\6\1\0\47\6\1\0\23\6\16\0\11\7\56\0\125\6\14\0"+
    "\u026c\6\2\0\10\6\12\0\32\6\5\0\113\6\3\0\3\6\17\0"+
    "\15\6\1\0\4\6\3\7\13\0\22\6\3\7\13\0\22\6\2\7"+
    "\14\0\15\6\1\0\3\6\1\0\2\7\14\0\64\6\40\7\3\0"+
    "\1\6\3\0\2\6\1\7\2\0\12\7\41\0\3\7\2\0\12\7"+
    "\6\0\130\6\10\0\51\6\1\7\126\0\35\6\3\0\14\7\4\0"+
    "\14\7\12\0\12\7\36\6\2\0\5\6\u038b\0\154\6\224\0\234\6"+
    "\4\0\132\6\6\0\26\6\2\0\6\6\2\0\46\6\2\0\6\6"+
    "\2\0\10\6\1\0\1\6\1\0\1\6\1\0\1\6\1\0\37\6"+
    "\2\0\65\6\1\0\7\6\1\0\1\6\3\0\3\6\1\0\7\6"+
    "\3\0\4\6\2\0\6\6\4\0\15\6\5\0\3\6\1\0\7\6"+
    "\17\0\4\7\32\0\5\7\20\0\2\6\23\0\1\6\13\0\4\7"+
    "\6\0\6\7\1\0\1\6\15\0\1\6\40\0\22\6\36\0\15\7"+
    "\4\0\1\7\3\0\6\7\27\0\1\6\4\0\1\6\2\0\12\6"+
    "\1\0\1\6\3\0\5\6\6\0\1\6\1\0\1\6\1\0\1\6"+
    "\1\0\4\6\1\0\3\6\1\0\7\6\3\0\3\6\5\0\5\6"+
    "\26\0\44\6\u0e81\0\3\6\31\0\11\6\6\7\1\0\5\6\2\0"+
    "\5\6\4\0\126\6\2\0\2\7\2\0\3\6\1\0\137\6\5\0"+
    "\50\6\4\0\136\6\21\0\30\6\70\0\20\6\u0200\0\u19b6\6\112\0"+
    "\u51a6\6\132\0\u048d\6\u0773\0\u2ba4\6\u215c\0\u012e\6\2\0\73\6\225\0"+
    "\7\6\14\0\5\6\5\0\1\6\1\7\12\6\1\0\15\6\1\0"+
    "\5\6\1\0\1\6\1\0\2\6\1\0\2\6\1\0\154\6\41\0"+
    "\u016b\6\22\0\100\6\2\0\66\6\50\0\15\6\3\0\20\7\20\0"+
    "\4\7\17\0\2\6\30\0\3\6\31\0\1\6\6\0\5\6\1\0"+
    "\207\6\2\0\1\7\4\0\1\6\13\0\12\7\7\0\32\6\4\0"+
    "\1\6\1\0\32\6\12\0\132\6\3\0\6\6\2\0\6\6\2\0"+
    "\6\6\2\0\3\6\3\0\2\6\3\0\2\6\22\0\3\7\4\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\2\2\2\3\17\4\1\5\1\6\1\7"+
    "\1\10\5\4\1\11\1\4\1\12\1\13\1\4\1\14"+
    "\1\15\1\16\1\17\6\4\1\20\1\4\1\21\1\22"+
    "\1\23\1\24\1\25\1\26\1\27\1\30\2\4\1\31"+
    "\1\32\1\33\1\34\1\35\1\36\1\4\1\37\1\40";

  private static int [] zzUnpackAction() {
    int [] result = new int[66];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\40\0\100\0\40\0\40\0\140\0\200\0\240"+
    "\0\300\0\340\0\u0100\0\u0120\0\u0140\0\u0160\0\u0180\0\u01a0"+
    "\0\u01c0\0\u01e0\0\u0200\0\u0220\0\u0240\0\40\0\40\0\40"+
    "\0\40\0\u0260\0\u0280\0\u02a0\0\u02c0\0\u02e0\0\u0300\0\u0320"+
    "\0\u0340\0\200\0\u0360\0\200\0\200\0\200\0\200\0\u0380"+
    "\0\u03a0\0\u03c0\0\u03e0\0\u0400\0\u0420\0\200\0\u0440\0\200"+
    "\0\200\0\200\0\200\0\200\0\200\0\200\0\200\0\u0460"+
    "\0\u0480\0\200\0\200\0\200\0\200\0\200\0\200\0\u04a0"+
    "\0\200\0\200";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[66];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\1\3\2\4\1\5\1\6\1\7\1\2\1\10"+
    "\1\7\1\11\1\12\1\13\1\14\1\7\1\15\1\7"+
    "\1\16\1\17\1\20\1\21\1\22\1\23\1\24\2\7"+
    "\1\25\1\7\1\26\1\27\1\30\1\31\42\0\1\4"+
    "\41\0\2\6\36\0\30\7\10\0\5\7\1\32\1\7"+
    "\1\33\1\34\17\7\10\0\24\7\1\35\3\7\10\0"+
    "\11\7\1\36\16\7\10\0\13\7\1\37\7\7\1\40"+
    "\4\7\10\0\15\7\1\41\12\7\10\0\14\7\1\42"+
    "\13\7\10\0\4\7\1\43\23\7\10\0\13\7\1\44"+
    "\4\7\1\45\7\7\10\0\13\7\1\46\4\7\1\47"+
    "\7\7\10\0\25\7\1\50\2\7\10\0\4\7\1\51"+
    "\4\7\1\52\11\7\1\53\1\54\3\7\10\0\23\7"+
    "\1\55\4\7\10\0\25\7\1\56\2\7\10\0\23\7"+
    "\1\57\4\7\10\0\6\7\1\60\21\7\10\0\7\7"+
    "\1\61\20\7\10\0\7\7\1\62\20\7\10\0\5\7"+
    "\1\63\22\7\10\0\12\7\1\64\15\7\10\0\17\7"+
    "\1\65\10\7\10\0\20\7\1\66\7\7\10\0\15\7"+
    "\1\67\12\7\10\0\16\7\1\70\11\7\10\0\24\7"+
    "\1\71\3\7\10\0\22\7\1\72\5\7\10\0\10\7"+
    "\1\73\17\7\10\0\7\7\1\74\20\7\10\0\16\7"+
    "\1\75\11\7\10\0\25\7\1\76\2\7\10\0\27\7"+
    "\1\77\10\0\6\7\1\100\21\7\10\0\13\7\1\101"+
    "\14\7\10\0\13\7\1\102\14\7\4\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[1216];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\1\1\2\11\20\1\4\11\51\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[66];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */

  private Symbol getSymbol(int type) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }

  private Symbol getSymbol(int type, Object value) {
    return new Symbol(type, yyline+1, yycolumn+1, value);
  }

  private void print_error_msg(String message) {
     System.out.print("\nLex error in XCSP functionals parser:\t"+message+"\n");
  }



  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public FunctionalsLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public FunctionalsLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 1720) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzPushbackPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead < 0) {
      return true;
    }
    else {
      zzEndRead+= numRead;
      return false;
    }
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = zzPushbackPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() throws java.io.IOException {
    if (!zzEOFDone) {
      zzEOFDone = true;
      yyclose();
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public java_cup.runtime.Symbol next_token() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      boolean zzR = false;
      for (zzCurrentPosL = zzStartRead; zzCurrentPosL < zzMarkedPosL;
                                                             zzCurrentPosL++) {
        switch (zzBufferL[zzCurrentPosL]) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn++;
        }
      }

      if (zzR) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof) 
            zzPeek = false;
          else 
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = zzLexicalState;


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 14: 
          { return getSymbol(sym.GE);
          }
        case 33: break;
        case 4: 
          { return getSymbol(sym.IDENTIFIER, yytext());
          }
        case 34: break;
        case 18: 
          { return getSymbol(sym.ADD);
          }
        case 35: break;
        case 9: 
          { return getSymbol(sym.NE);
          }
        case 36: break;
        case 13: 
          { return getSymbol(sym.LT);
          }
        case 37: break;
        case 17: 
          { return getSymbol(sym.ABS);
          }
        case 38: break;
        case 25: 
          { return getSymbol(sym.MAX);
          }
        case 39: break;
        case 27: 
          { return getSymbol(sym.MOD);
          }
        case 40: break;
        case 15: 
          { return getSymbol(sym.GT);
          }
        case 41: break;
        case 31: 
          { return getSymbol(sym.TRUE);
          }
        case 42: break;
        case 6: 
          { return getSymbol(sym.RPAREN);
          }
        case 43: break;
        case 11: 
          { return getSymbol(sym.EQ);
          }
        case 44: break;
        case 24: 
          { return getSymbol(sym.IFF);
          }
        case 45: break;
        case 16: 
          { return getSymbol(sym.OR);
          }
        case 46: break;
        case 28: 
          { return getSymbol(sym.MUL);
          }
        case 47: break;
        case 30: 
          { return getSymbol(sym.POW);
          }
        case 48: break;
        case 19: 
          { return getSymbol(sym.AND);
          }
        case 49: break;
        case 23: 
          { return getSymbol(sym.NOT);
          }
        case 50: break;
        case 20: 
          { return getSymbol(sym.SUB);
          }
        case 51: break;
        case 21: 
          { return getSymbol(sym.DIV);
          }
        case 52: break;
        case 29: 
          { return getSymbol(sym.XOR);
          }
        case 53: break;
        case 26: 
          { return getSymbol(sym.MIN);
          }
        case 54: break;
        case 8: 
          { return getSymbol(sym.UMINUS);
          }
        case 55: break;
        case 12: 
          { return getSymbol(sym.LE);
          }
        case 56: break;
        case 3: 
          { return getSymbol(sym.INTEGER, new Integer(yytext()));
          }
        case 57: break;
        case 5: 
          { return getSymbol(sym.LPAREN);
          }
        case 58: break;
        case 10: 
          { return getSymbol(sym.IF);
          }
        case 59: break;
        case 22: 
          { return getSymbol(sym.NEG);
          }
        case 60: break;
        case 1: 
          { print_error_msg("Illegal character \""+yytext()+
                                                              "\" at line "+yyline+", column "+yycolumn); 
                                                              //System.exit(0);
          }
        case 61: break;
        case 7: 
          { return getSymbol(sym.COMMA);
          }
        case 62: break;
        case 32: 
          { return getSymbol(sym.FALSE);
          }
        case 63: break;
        case 2: 
          { /* ignore */
          }
        case 64: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            zzDoEOF();
              { return new java_cup.runtime.Symbol(sym.EOF); }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}

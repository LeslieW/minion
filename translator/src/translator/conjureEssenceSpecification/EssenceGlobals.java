package translator.conjureEssenceSpecification;

/**  global constants in order to trace down the syntax tree
     idea: maybe introduce a value for all nonterminals/terminals
           that should appear in Essence'?
           
     The highest occupied number is now 144 
*/

public interface EssenceGlobals {

    // AtomicExpression.java
	// please keep the same ordering
	// of the following restriction modes.
	// offsets from the current value are ok.
    public final int BOOLEAN = 0;
    public final int NUMBER = 1;
    public final int IDENTIFIER = 2;
    public final int AUX_VARIABLE = 3;
    public final int ATOMIC_SET_DOMAIN = 133;
    public final int ATOMIC_MULTISET_DOMAIN = 134;

    // Be.java
    public final int BE = 5;
    public final int BE_DOMAIN = 6;

    // Constant.java
    public final int CONSTANT_DOMAIN = 7;
    public final int CONSTANT = 8;
    public final int CONSTANT_NEW_TYPE = 9;
    public final int CONSTANT_ARRAY = 144;

    // BindingExpression.java
    public final int BINDING_IDENTIFIER_DOMAIN_EXPR = 10;
    public final int BINDING_UNIT_MEMBER_EXPR = 11;
    public final int BINDING_IDLIST_SUBSET_EXPR = 12;


    // Declaration.java
    public final int GIVEN = 13;
    public final int WHERE = 14;
    public final int LETTING = 15;
    public final int FIND = 16;
    public final int PARAM = 143;
    
    // Domain.java
    public final int BRACKETED_DOMAIN = 17;
    public final int BOOLEAN_DOMAIN = 18;
    public final int INTEGER_RANGE = 19;
    public final int IDENTIFIER_RANGE = 20;
    public final int SET_DOMAIN = 21;
    public final int MULTISET_DOMAIN = 22;
    public final int MATRIX_DOMAIN = 23;
    public final int FUNCTION_DOMAIN = 24;
    public final int REL_DOMAIN = 25;
    public final int PARTITION_DOMAIN = 26;
    public final int RPARTITION_DOMAIN = 27;
    public final int INFINITE_DOMAIN = 4; // only for parameters
    
    // FunctionDescriptors.java
    public final int KIND_FUNCTION = 28;
    public final int CLASS_FUNCTION = 29;
    public final int KIND_CLASS_FUNCTION = 30;


    //  FunctionDomain.java
    public final int FUNCTIOND = 31;
    public final int FUNCTIOND_KIND = 32;
    public final int FUNCTIOND_CLASS = 33;
    public final int FUNCTIOND_KIND_CLASS = 34;

    //    GroupOpExpression.java
    public final int GROUP_SET = 35;
    public final int GROUP_MSET = 36;
    public final int GROUP_TUPLE = 37;
    public final int GROUP_MATRIX = 38;
    public final int GROUP_MATRIX_DOMAIN = 39;


    // FunctionExpression.java
    // watch out with the order here!
    // at the moment: alldiff < element
    public final int ALLDIFF = 40;
    public final int MIN = 41;
    public final int MAX = 42;
    public final int DOM = 43;
    public final int RAN = 44;
    public final int INV = 45;
    public final int IMAGE = 46;
    public final int ATLEAST = 47;
    public final int ATMOST = 48;
    public final int OCCURRENCE = 126;
    public final int ELEMENT = 127;
    

    //    IdentifierDomain.java
    public final int IDENTIFIER_DOMAIN = 49;
    public final int IDENTIFIER_DOMAIN_RANGE = 50;

    //    IdentifierList.java
    public final int IDENTIFIER_LIST = 51;
    public final int IDENTIFIER_LIST_DOMAIN = 52;


    //    IntDomain.java 
    public final int INT_DOMAIN = 53;
    public final int INT_DOMAIN_RANGE = 54;

    //    KindFunction.java 
    public final int KIND_FUNCTION_PARTIAL = 55;
    public final int KIND_FUNCTION_TOTAL = 56;

    // MatrixDomain.java
    // i don't understand why we need a restriction mode here
    public final int MATRIXD_DOMAIN = 57;  

    // MemberOp.java
    public final int MEMBER_OF = 58;
    public final int NOT_IN = 59;

    // MultiSetDomain.java
    public final int MULTISETD = 60;
    public final int MULTISETD_SIZESET = 61;

    //     NonAtomicExpression.java
    public final int NONATOMIC_EXPR_BRACKET = 62;
    public final int NONATOMIC_EXPR_PAREN = 63;
    
    // Objective.java
    public final int SOLVE = 64;
    public final int MINIMISING = 65;
    public final int MAXIMISING = 66;

    // Parameter.java
    public final int PARAMETER_DOM_IDENTIFIERS = 67;
    public final int PARAMETER_ENUMERATION = 68;

    // Expression.java
    // please keep the order of the values 
    // of the following globals
    public final int BRACKET_EXPR = 69;
    public final int ATOMIC_EXPR = 70;
    public final int NONATOMIC_EXPR =71;
    public final int GROUPOP_EXPR = 72;
    public final int UNITOP_EXPR = 73;
    public final int BINARYOP_EXPR = 74;
    public final int FUNCTIONOP_EXPR = 75;
    public final int QUANTIFIER_EXPR = 76;
    public final int LEX_EXPR = 132;
    public final int LINEAR_EXPR = 135;
    public final int TABLE_CONSTRAINT = 136;
    
    // ClassFunction.java
    public final int INJECTIVE = 77;
    public final int SURJECTIVE = 78;
    public final int BIJECTIVE = 79;

    // BindUnit.java
    public final int BIND_IDENTIFIER_LIST = 80;
    public final int BIND_TUPLE_LIST = 81;

    // PartitionDomain.java
    public final int PARTITIOND = 82;
    public final int PARTITIOND_SIZESET = 83;

    // Quantifier.java
    // please keep the order!!
    public final int SUM = 84;
    public final int FORALL = 85;
    public final int EXISTS = 86;
    

    // RangeAtom.java
    public final int RANGE_EXPR = 87;
    public final int RANGE_DOTS_EXPR = 88;
    public final int RANGE_EXPR_DOTS = 89;
    public final int RANGE_EXPR_DOTS_EXPR = 90;
    
    // RelDomain.java
    public final int RELD = 91;
    public final int RELD_SIZESET = 92;

    // SetDomain.java
    public final int SETD = 93;
    public final int SETD_SIZESET = 94;

    // SizeSet.java
    public final int SIZESET = 95;
    public final int SIZESET_MAX = 96;

    // SubSetOp.java
    public final int PROPER_SUBSET = 97;
    public final int SUBSET = 98;

    // Type.java
    public final int TYPE_EXPR = 99;
    public final int TYPE_ENUM = 100;
    
    // UnitOpExpression.java
    public final int NEGATION = 101;
    public final int NOT = 102;
    public final int ABS = 103;
   

    // Annotations.java
    public final int SYMMETRIE = 104;
    public final int CHANNEL = 105;
    public final int QANNOTATION = 106;

    // BiOp.java
    // please do not alter the order 
    // of the operators' restriction-modes
    public final int EQ = 107;
    public final int NEQ = 108;
    public final int LESS = 109;
    public final int LEQ = 110;
    public final int GREATER = 111;
    public final int GEQ = 112;
    public final int PLUS = 113;
    public final int MINUS = 114;
    public final int DIVIDE = 115;
    public final int MULT = 116;
    public final int POWER = 117;
    public final int IFF = 118;
    public final int IF = 119;
    public final int OR = 120;
    public final int AND = 121;
 
    
 
    public final int INTERSEC = 122;
    public final int UNION = 123;
    public final int SUBSET_OP = 124;
    public final int MEMBER_OP = 125;

    // LexOperator.java
    public final int LEX_LESS = 128;
    public final int LEX_LEQ = 129;
    public final int LEX_GREATER = 130;
    public final int LEX_GEQ = 131;
    
    
    // matrix/array indices
    public final int EXPRESSION_INDEX = 137;
    public final int FULL_BOUNDED_INDEX = 138;
    public final int LOWER_BOUNDED_INDEX = 139;
    public final int UPPER_BOUNDED_INDEX = 140;
    public final int UPPER_LOWER_BOUNDED_INDEX = 141;
    public final int SPARSE_INDEX = 142;
    
}

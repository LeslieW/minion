// MinionInputReader.cpp
//
// Subversion Identity $Id: MinionInputReader.cpp 156 2006-05-03 19:18:12Z gentian $
//
// Plan here is to generate an instance of a problem (or whatever you have)
// and return that.

/// TODO: We need somewhere better to put these things.
bool print_solution = true;

#include "minion.h"
#include "CSPSpec.h"
using namespace ProbSpec;

#include "BuildConstraint.h"

#include "MinionInputReader.h"

ConstraintDef constraint_list[] =
{
  { "element",  CT_ELEMENT, 2, { read_list, read_var }, STATIC_CT },
  { "watchelement", CT_WATCHED_ELEMENT, 2, { read_list, read_var }, DYNAMIC_CT },
  { "gacelement", CT_GACELEMENT, 2, { read_list, read_var }, STATIC_CT },
{ "alldiff", CT_ALLDIFF, 1, { read_list }, STATIC_CT },
{ "diseq",   CT_DISEQ,   2, { read_var, read_var }, STATIC_CT },
{ "eq",      CT_EQ,      2, { read_var, read_var }, STATIC_CT },
{ "ineq",    CT_INEQ,    3, { read_var, read_var, read_constant }, STATIC_CT },
{ "lexleq",  CT_LEXLEQ,  2, { read_list, read_list }, STATIC_CT },
{ "lexless", CT_LEXLESS, 2, { read_list, read_list }, STATIC_CT },
{ "max",     CT_MAX,     2, { read_list, read_var }, STATIC_CT },
{ "min",     CT_MIN,     2, { read_list, read_var }, STATIC_CT },
{ "occurrence", CT_OCCURRENCE, 3, { read_list, read_constant, read_constant }, STATIC_CT },
{ "product", CT_PRODUCT2, 2, {read_2_vars, read_var }, STATIC_CT },
{ "weightedsumleq", CT_WEIGHTLEQSUM, 3, { read_constant_list, read_list, read_var }, STATIC_CT },
{ "weightedsumgeq", CT_WEIGHTGEQSUM, 3, { read_constant_list, read_list, read_var }, STATIC_CT },
{ "sumgeq", CT_GEQSUM, 2, {read_list, read_var}, STATIC_CT },
{ "sumleq", CT_LEQSUM, 2, {read_list, read_var}, STATIC_CT },
{ "watchsumgeq", CT_WATCHED_GEQSUM, 2, {read_list, read_var}, DYNAMIC_CT },
{ "watchsumleq", CT_WATCHED_LEQSUM, 2, {read_list, read_var}, DYNAMIC_CT },
{ "table", CT_WATCHED_TABLE, 2, {read_list, read_tuples}, DYNAMIC_CT }
};

const int num_of_constraints = sizeof(constraint_list) / sizeof(ConstraintDef);

ConstraintDef& get_constraint(ConstraintType t)
{
  for(int i = 0; i < num_of_constraints; ++i)
  {
    if(constraint_list[i].type == t)
	  return constraint_list[i];
  }
  
  D_ASSERT(0 && "Constraint not found");
  exit(-1);
}

template<typename T>
vector<T> make_vec(const T& t)
{
  vector<T> vec;
  vec.push_back(t);
  return vec;
}

/// Check if the next character from @infile is @sym.
void check_sym(ifstream& infile, char sym)
{
  char idChar;
  infile >> idChar ;
  if(idChar != sym)
  {
    throw new parse_exception(string("Expected '") + sym + "'. Recieved '" + idChar + "'.");
  }
}

int read_num(ifstream& infile)
{
  int i;
  infile >> i;
  if(infile.fail())
    throw new parse_exception("Expected number!");
  return i;
}

bool parser_verbose = false;

void parser_info(string s)
{
  if(parser_verbose)
    cout << s << endl;
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// flatten
// type: m (2d matrix), t (3d matrix)
// Flattening is row-wise (2d), plane-wise row-wise (3d).
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
vector<Var> MinionInputReader::flatten(char type, int index) {
  unsigned int rowIndex, colIndex, planeIndex ;
  vector<Var> flattened ;
  // flatten row-wise
  if (type == 'm') {
    vector< vector<Var> > matrix = Matrices.at(index) ;
    for (rowIndex = 0; rowIndex < matrix.size() ; rowIndex++) {
      vector<Var> row = matrix.at(rowIndex) ;
      for (colIndex = 0; colIndex < row.size(); colIndex++)
        flattened.push_back(row.at(colIndex)) ;
    }
  }
  // flatten plane-wise then row-wise
  else {
    vector< vector <vector <Var> > > tensor = Tensors.at(index) ;
    for (planeIndex = 0; planeIndex < tensor.size(); planeIndex++) {
      vector< vector <Var> > plane = tensor.at(planeIndex) ;
      for (rowIndex = 0; rowIndex < plane.size(); rowIndex++) {
        vector<Var> row = plane.at(rowIndex) ;
        for (colIndex = 0; colIndex < row.size(); colIndex++)
          flattened.push_back(row.at(colIndex)) ;
      }
    }
  }
  return flattened ;
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// getColOfMatrix
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
vector<Var> MinionInputReader::getColOfMatrix(
											  vector<vector<Var> >& matrix, int colNo) {
  vector<Var> result ;
  for (unsigned int rowIndex = 0; rowIndex < matrix.size(); rowIndex++) {
	result.push_back(matrix.at(rowIndex).at(colNo)) ;
  }
  return result ;
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// getRowThroughTensor
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
vector<Var> MinionInputReader::getRowThroughTensor(
												   vector< vector< vector<Var> > >& tensor, int rowNo, int colNo) {
  vector<Var> result ;
  for (unsigned int planeIndex = 0; planeIndex < tensor.size() ; planeIndex ++) {
    vector< vector<Var> >& plane = tensor.at(planeIndex) ;
    vector<Var>& row = plane.at(rowNo) ;
    result.push_back(row.at(colNo)) ;
  }
  return result ;
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// read
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
void MinionInputReader::read(char* fn) {
  // general purpose buffer
  char* buf = new char[10000];
  ifstream infile(fn) ;
  if (!infile) {
    cerr << "Can't open " << fn << endl ;
    D_ASSERT(0); exit(3) ;
  }
  
  string test_name;
  infile >> test_name;
  if(test_name != "MINION")
  {
    cerr << "Error! All Minion input files must begin 'MINION <input version num>";
	D_ASSERT(0); exit(4);
  }
  
  int ver_num = read_num(infile);
  
  if(parser_verbose)
    cout << "Input file has version num " << ver_num << endl;
  
  // Just swallow the rest of this line, in particular the return. Extra stuff could be added on the line
  // later without breaking this version of the parser..
  infile.getline(buf,10000);
  
  if(ver_num != 1)
  {
    cout << "This version of Minion only reads file with input format 1" << endl;
	cout << "Sorry." << endl;
	D_ASSERT(0); exit(5);
  }
  
  while(infile.peek() == '#')
  {
	infile.getline(buf,10000);
	parser_info(string("Read comment line:") + buf);
  }
  
  // After this, we don't want the buffer any more.
  delete[] buf;
  
  try
  {
	readVars(infile) ;
	readVarOrder(infile) ;
	readValOrder(infile) ;
	readMatrices(infile) ;
	readObjective(infile) ;
	readPrint(infile);
	
	while(readConstraint(infile, false)) ;
  }
  // This is for when we want to catch in the debugger. It will
  // leave any exceptions to continue upwards.
#ifdef NO_CATCH
  catch(int ){}
#else
  catch(parse_exception* s)
  {
    cerr << "Error in input." << endl;
	cerr << s->what() << endl;
	// This nasty line will tell us the current position in the file even if a parse fail has occurred.
	int error_pos =  infile.rdbuf()->pubseekoff(0, ios_base::cur, ios_base::in);
	
	//cerr << "It was at character number:" << infile.tellg() << endl;
	int line_num = 0;
	int end_prev_line = 0;
	buf = new char[1000000];
    infile.close();
	// Open a new stream, because we don't know what kind of a mess the old one might be in.
	ifstream error_file(fn);
	while(error_pos > error_file.tellg())
	{ 
	  end_prev_line = error_file.tellg();
	  error_file.getline(buf,1000000);
	  line_num++;
	}
	cerr << "Error on line:" << line_num << ". Gave up parsing before here:" << endl;
	cerr << string(buf) << endl;
	for(int i = 0; i < (error_pos - end_prev_line); ++i)
	  cerr << "-";
	cerr << "^" << endl;
	cerr << "Sorry it didn't work out." << endl;
	// This is so we can catch things in the debugger.
	D_ASSERT(0); exit(6);
  }
#endif
}

/// Cleans rubbish off start of string.
void clean_string(string& s)
{
  while(!s.empty() && isspace(s[0]))
    s.erase(s.begin());
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readConstraint
// Recognise constraint by its name, read past name and leading '('
// Return false if eof or unknown ct. Else true.
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
bool MinionInputReader::readConstraint(ifstream& infile, bool reified) {
  char char_id[1000];
  infile.getline(char_id, 1000, '(');
  string id(char_id);
  clean_string(id);
  
  if(id == "element")
	readConstraintElement(infile, get_constraint(CT_ELEMENT)) ;
  else if(id == "watchelement")
    readConstraintElement(infile, get_constraint(CT_WATCHED_ELEMENT));
  else if(id == "gacelement")
    readConstraintElement(infile, get_constraint(CT_GACELEMENT));  
  else if(id == "reify")
  { 
    if(reified == true)
	  throw new parse_exception("Can't reify a reified constraint!");
    readConstraint(infile, true);
	
	check_sym(infile, ',');
    Var reifyVar = readIdentifier(infile);
	check_sym(infile, ')');
	instance.last_constraint_reify(reifyVar);
  }
  else if(id == "reifyimply")
  {
    if(reified == true)
	  throw new parse_exception("Can't reify a reified constraint!");
	readConstraint(infile, true);
	
	check_sym(infile, ',');
	Var reifyVar = readIdentifier(infile);
	check_sym(infile, ')');
	instance.last_constraint_reifyimply(reifyVar);
  }
  else if(id == "table")
    readConstraintTable(infile, get_constraint(CT_WATCHED_TABLE));
  else 
  {
	for(int i = 0; i < num_of_constraints; ++i)
	{
	  if(constraint_list[i].name == id)
	  {
		readGeneralConstraint(infile, constraint_list[i]);
		return true;
	  }
	}
	
    if (infile.eof()) 
    {
      parser_info("Done.") ;
      return false;
    }
    else
    { throw new parse_exception(string("Unknown Constraint:") + id); }
  }
  return true ;
}


void MinionInputReader::readGeneralConstraint(ifstream& infile, const ConstraintDef& def)
{
  vector<vector<Var> > varsblob;
  for(int i = 0; i < def.number_of_params; ++i)
  {
    switch(def.read_types[i])
	{
	  case read_list:
	    varsblob.push_back(readVectorExpression(infile));
		break;
	  case read_var:
	    varsblob.push_back(make_vec(readIdentifier(infile)));
		break;
	  case read_2_vars:
	  {
	    vector<Var> vars;
	    vars.push_back(readIdentifier(infile));
	    check_sym(infile,',');
	    vars.push_back(readIdentifier(infile));
            varsblob.push_back(vars);
	  }
		break;
	  case read_constant:
	    varsblob.push_back(make_vec(readIdentifier(infile)));
		if(varsblob.back().back().type != VAR_CONSTANT)
		  throw parse_exception("Expected Constant");
		break;
	  case read_constant_list:
	  {
		vector<Var> vectorOfConst ;
		vectorOfConst = readVectorExpression(infile) ;
		for(unsigned int loop = 0; loop < vectorOfConst.size(); ++loop)
		{
		  if(vectorOfConst[loop].type != VAR_CONSTANT)
			throw new parse_exception("Vector must only be constants.");
		}
		varsblob.push_back(vectorOfConst);
	  }
		break;  
	  default:
	    cerr << "Internal Error." << endl;
		exit(0);
	}
	if(i != def.number_of_params - 1)
	  check_sym(infile, ',');
  }
  check_sym(infile, ')');
  
  instance.add_constraint(ConstraintBlob(def, varsblob));
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readConstraintElement
// element(vectorofvars, indexvar, var)
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
void MinionInputReader::readConstraintElement(ifstream& infile, const ConstraintDef& ctype) {
  parser_info("reading an element ct. " ) ;
  vector<vector<Var> > vars;
  // vectorofvars
  vars.push_back(readVectorExpression(infile));
  check_sym(infile, ',');
  // indexvar
  vars.push_back(make_vec(readIdentifier(infile)));
  check_sym(infile, ',');
  // The final var is shoved on the end of the vector of vars as it should
  // be of a similar type.
  // final var
  vars[0].push_back(readIdentifier(infile));
  check_sym(infile, ')');
  instance.add_constraint(ConstraintBlob(ctype, vars));
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readConstraintTable
// table(<vectorOfVars>, {<tuple> [, <tuple>]})
// Tuples represented as a vector of int arrays.
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
void MinionInputReader::readConstraintTable(ifstream& infile, const ConstraintDef& def) {
  parser_info( "reading a table ct (unreifiable)" ) ;
  char delim ;
  int count, elem ;
  vector<Var> vectorOfVars = readVectorExpression(infile) ;
  int tupleSize = vectorOfVars.size() ;
  vector<vector<int> > tuples ;
  check_sym(infile,',');
  check_sym(infile,'{');
  while (delim != '}') {
    check_sym(infile,'<');
    vector<int> tuple(tupleSize);
    elem = read_num(infile) ;
    tuple[0] = elem ;
    for (count = 1; count < tupleSize; count++) {
      check_sym(infile, ',');
      elem = read_num(infile) ;
      tuple[count] = elem ;
    }
    check_sym(infile, '>');
    tuples.push_back(tuple) ;
    infile >> delim ;                                          // , or }
	if(delim != ',' && delim!= '}')
	  throw new parse_exception("Expected ',' or '}'");
  }
  check_sym(infile,')');
  ConstraintBlob tableCon(def, vectorOfVars);
  tableCon.tuples = tuples;
  instance.add_constraint(tableCon);
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readIdentifier
// Expects "<idChar><index>", where <idChar> is 'x', 'v', 'm', 't'.
// Assumes caller knows what idChar should be.
// Returns an object of type Var.
// NB peek() does not ignore whitespace, >> does. Hence use of putBack()
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
Var MinionInputReader::readIdentifier(ifstream& infile) {
  char idChar ;
  infile >> idChar ;
  if ((('0' <= idChar) && ('9' >= idChar)) || idChar == '-') {
    infile.putback(idChar) ;
    int i = read_num(infile);
	return Var(VAR_CONSTANT, i);
  }
  int index = -1 ;
  
  if(idChar != 'x' && idChar != 'n')
  {
    string s("Found 'X', expected 'x' or 'n' at start of a variable");
	s[7] = idChar;
    throw new parse_exception(s);
  }
  if(idChar == 'x')
  {
    index = read_num(infile);
    return instance.vars.get_var(idChar, index);
  }
  check_sym(infile, 'x');
  index = read_num(infile);
  Var var = instance.vars.get_var(idChar, index);
  if(var.type != VAR_BOOL)
    throw new parse_exception("Can only 'not' a Boolean variable!");
  var.type = VAR_NOTBOOL;
  return var;
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readLiteralMatrix
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
vector< vector<Var> > MinionInputReader::readLiteralMatrix(ifstream& infile) {
  char delim ;
  check_sym(infile, '[');
  delim = '[';
  vector< vector<Var> > newMatrix ;
  while(delim != ']') {
    newMatrix.push_back(readLiteralVector(infile)) ;
    infile >> delim ;                                        // , or ]
  }
  return newMatrix ;
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readLiteralVector
// of vars or consts. Checks 1st elem of vect (empty vects not expected)
//  to see which.
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
vector<Var> MinionInputReader::readLiteralVector(ifstream& infile) {
  char delim=',';
  vector<Var> newVector ;
  check_sym(infile, '[');
  while (delim != ']') {
	newVector.push_back(readIdentifier(infile)) ;
	infile >> delim ;
	   if(delim != ',' && delim != ']')
	   {
		 // replace X with the character we got.
		 string s = "Expected ',' or ']'. Got 'X'.";
		 s[s.size() - 3] = delim;
		 throw new parse_exception(s);
	   }
  }
  return newVector ;
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readMatrices
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
void MinionInputReader::readMatrices(ifstream& infile) {
  char delim ;
  int count1 ;
  // Read Vectors%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  int noOfMatrixType = read_num(infile);
  if(parser_verbose)
    cout << "Number of 1d vectors: " << noOfMatrixType << endl ;
  for (count1 = 0; count1 < noOfMatrixType; count1++)
    Vectors.push_back(readLiteralVector(infile)) ;
  // Read 2dMatrices%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  noOfMatrixType = read_num(infile);
  if(parser_verbose)
    cout << "Number of 2d matrices: " << noOfMatrixType << endl ;
  for (count1 = 0; count1 < noOfMatrixType; count1++)
    Matrices.push_back(readLiteralMatrix(infile)) ;
  // Read 3dMatrices%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  noOfMatrixType = read_num(infile);
  if(parser_verbose)
    cout << "Number of 3d tensors: " << noOfMatrixType << endl ;
  for (count1 = 0; count1 < noOfMatrixType; count1++) {
    vector< vector< vector <Var> > > newTensor ;
    infile >> delim ;                                               // [
    while (delim != ']') {
      newTensor.push_back(readLiteralMatrix(infile)) ;
      infile >> delim ;                                        // , or ]
    }
    Tensors.push_back(newTensor) ;
  }
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readObjective
// 'objective' 'none' | 'minimising' <var> | 'maximising' <var>
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
void MinionInputReader::readObjective(ifstream& infile) {
  string s;
  infile >> s;
  if(s != "objective")
    throw new parse_exception(string("Expected 'objective', recieved '")+s+"'");
  infile >> s;
  if(s == "none")
  {
    parser_info( "objective none" );
	return;
  }
  
  if(s != "minimising" && s != "maximising")
  {
    throw new parse_exception(string("Expected 'none', 'maximising'") +
							  string("or 'minimising'. Got ;") + s + "'");
  }
  
  bool minimising = (s == "minimising");
  Var var = readIdentifier(infile) ;
  if(parser_verbose)
    cout << ((minimising) ? "minimising" : "maximising") << string(var) << endl ;
  instance.set_optimise(minimising, var);
}

void MinionInputReader::readPrint(ifstream& infile) {
  string s;
  infile >> s;
  if(s != "print")
    throw new parse_exception(string("Expected 'print', recieved '")+s+"'");
  
  char letter;
  infile >> letter;
  if(letter == 'n')
  {
    infile >> s;
	if(s != "one")
	  throw new parse_exception(string("I don't understand '")+s+"'");
	parser_info( "print none" );
	return;
  }
  else if(letter == 'm')
  {
    int matrix_num = read_num(infile);
	if (print_solution)
	{
	  instance.print_matrix = Matrices[matrix_num];
	  if(parser_verbose)
	    cout << "print m" << matrix_num << endl;
	  return;
	}
	else
	{ 
	  if(parser_verbose)
		cout << "print m" << matrix_num << 
		  "overridden by -noprintsols option" << endl;
	  return;
	}
  }
  
  throw new parse_exception(string("I don't understand this print statement"));
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readValOrder
// '[' <valOrderIdentifier> [, <valOrderIdentifier>]* ']'
// <valOrderIdentifier> := 'a' | 'd' --- for ascending/descending
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
void MinionInputReader::readValOrder(ifstream& infile) {
  parser_info( "Reading val order" ) ;
  char delim, valOrderIdentifier ;
  infile >> delim ;                                                 // [
  vector<char> valOrder ;
  while (delim != ']') {
    infile >> valOrderIdentifier ;
	if(valOrderIdentifier != 'a' && valOrderIdentifier != 'd')
	  throw new parse_exception("Expected 'a' or 'd'");
	valOrder.push_back(valOrderIdentifier == 'a');
    infile >> delim ;                                          // , or ]
  }
  instance.val_order = valOrder;
  ostringstream s;
  s << "Read val order. Length: " << valOrder.size();
  parser_info(s.str());
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readVarOrder
// '[' <var> [, <var>]* ']'
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
void MinionInputReader::readVarOrder(ifstream& infile) {
  parser_info( "Reading var order" ) ;
  char delim = ',' ;
  check_sym(infile, '[');
  vector<Var> varOrder;
  while (delim != ']') {
    if(delim != ',')
	{
	  string s("Expected ',' or ']'. Got 'X'.");
	  s[s.size() - 3] = delim;
	  throw new parse_exception(s);
	}
    varOrder.push_back(readIdentifier(infile)) ;
    infile >> delim ;                                          // , or ]
  }
  instance.var_order = varOrder;
  
  ostringstream s;
  s << "Read var order. Length: " << varOrder.size();
  parser_info(s.str());
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
void MinionInputReader::readVars(ifstream& infile) {
  int lb, ub, count ;
  int total_var_count = 0;
  char delim ;
  ProbSpec::VarContainer var_obj;
  // Read 01Vars%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  int noOfVarType = read_num(infile);
  total_var_count += noOfVarType;
  if(parser_verbose)
    cout << "Number of 01 Vars: " << noOfVarType << endl ;
  var_obj.bools = noOfVarType;
  
  
  // **** Construct this many 01Vars
  // Read Bounds Vars%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  noOfVarType = read_num(infile);
  total_var_count += noOfVarType;
  if(parser_verbose)
    cout << "Number of Bounds Vars: " << noOfVarType << endl ;
  while (noOfVarType > 0) {
    lb = read_num(infile);
	ub = read_num(infile);
	count = read_num(infile);
	if(parser_verbose)
      cout << count << " of " << lb << ", " << ub << endl ;
    var_obj.bound.push_back(make_pair(count, ProbSpec::Bounds(lb, ub)));
    noOfVarType -= count ;
  }
  
  // Read Sparse Bounds Vars%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  noOfVarType = read_num(infile);
  total_var_count += noOfVarType;
  if(parser_verbose)
    cout << "Number of Sparse Bounds Vars: " << noOfVarType << endl ;
  int domainElem ;
  while (noOfVarType > 0) {
    vector<int> domainElements ;
    infile >> delim ;                                               // {
    while (delim != '}') {
      domainElem = read_num(infile);
      domainElements.push_back(domainElem) ;
      infile >> delim ;                                        // , or }
    }
    count = read_num(infile);
	if(parser_verbose)
      cout << count << " of these " << endl ;
    // **** Construct this many discrete vars.
    var_obj.sparse_bound.push_back(make_pair(count, domainElements));
    noOfVarType -= count ;
  }
  
  // Read Discrete Bounds Vars%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  noOfVarType = read_num(infile);
  total_var_count += noOfVarType;
  if(parser_verbose)
    cout << "Number of Discrete Vars: " << noOfVarType << endl ;
  while (noOfVarType > 0) {
    lb = read_num(infile);
	ub = read_num(infile);
	count = read_num(infile);
	if(parser_verbose)
      cout << count << " of " << lb << ", " << ub << endl ;
    var_obj.discrete.push_back(make_pair(count, ProbSpec::Bounds(lb, ub)));
    // **** Construct this many discrete bounds vars.
    noOfVarType -= count ;
  }
  // Read Discrete Vars%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  noOfVarType = read_num(infile);
  total_var_count += noOfVarType;
  if(parser_verbose)
    cout << "Number of Sparse Discrete Vars: " << noOfVarType << endl ;
  while (noOfVarType > 0) {
    vector<int> domainElements ;
    infile >> delim ;                                               // {
    while (delim != '}') {
      domainElem = read_num(infile);
      domainElements.push_back(domainElem) ;
      infile >> delim ;                                        // , or }
    }
    count = read_num(infile);
    if(parser_verbose)
      cout << count << " of these " << endl ;
    // **** Construct this many discrete vars.
    var_obj.sparse_discrete.push_back(make_pair(count, domainElements));
    noOfVarType -= count ;
  }
  
  var_obj.total_var_count = total_var_count;
  instance.vars = var_obj;
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// readVectorExpression
// literal vector (of vars or consts), vi, mi(flattened), ti(flattened),
// row(mi, r), col(mi, c), col(ti, p, c), rowx(ti, p, r), rowz(ti, r, c)
// NB Expects caller knows whether vars or consts expected for lit vect.
// NB peek does not ignore wspace, >> does. Hence use of putback
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
vector<Var> MinionInputReader::readVectorExpression(ifstream& infile) {
  char idChar, delim ;
  int row, col, plane ;
  int input_val;
  infile >> idChar ;
  switch (idChar) {
    case '[':
      parser_info( "Reading Literal Vector of vars or consts" ) ;
      infile.putback(idChar) ;
      return readLiteralVector(infile) ;      
    case 'v':                                        // vector identifier
      parser_info( "Reading vector identifier" ) ;
      //infile.putback(idChar) ;
	  input_val = read_num(infile);
	  return Vectors.at(input_val) ;
    case 'm':                                       // matrix identifier
      parser_info( "Reading matrix identifier (will flatten)" ) ;
      //infile.putback(idChar) ;
	  input_val = read_num(infile);
	  return flatten('m', input_val) ;
    case 't':                                        // matrix identifier
      parser_info( "Reading tensor identifier (will flatten)" ) ;
      //infile.putback(idChar) ;
	  input_val = read_num(infile);
	  return flatten('t', input_val) ;
    case 'r':                                       // row of a mx/tensor
	  check_sym(infile,'o');
	  check_sym(infile,'w');
      infile >> idChar ;            // o w [( x or z]
      switch(idChar) {
		case '(':                                        // row of a matrix
		{parser_info( "Reading row of a matrix" ) ;
		  check_sym(infile,'m');
		  input_val = read_num(infile);
		  vector< vector<Var> > matrix = Matrices.at(input_val) ;
		  infile >> delim;
		  row = read_num(infile);
		  infile >> delim ;
		  return matrix.at(row) ;}
		case 'x':                             // row of a plane of a tensor
		{parser_info( "Reading row of a plane of a tensor" ) ;
		  check_sym(infile,'(');
		  check_sym(infile,'t');
		  input_val = read_num(infile);
		  vector< vector< vector<Var> > >& tensor = Tensors.at(input_val) ;
		  check_sym(infile, ',');
		  input_val = read_num(infile);
		  vector< vector <Var> >& tensorPlane = tensor.at(input_val) ;
		  check_sym(infile, ',');
		  input_val = read_num(infile);
		  check_sym(infile, ')');
		  return tensorPlane.at(input_val);
		}
		case 'z':                         // Row through planes of a tensor
		{parser_info( "Reading row through planes of a tensor" ) ;
		  check_sym(infile, '(');
		  input_val = read_num(infile);
		  vector< vector< vector<Var> > >& tensor = Tensors.at(input_val) ;
		  check_sym(infile, ',');
		  row = read_num(infile);
		  check_sym(infile, ',');
		  col = read_num(infile);
		  check_sym(infile, ')');
		  return getRowThroughTensor(tensor, row, col) ;}
		default:
		  throw new parse_exception("Malformed Row Expression");
		  break ;
	  }
		break ;
      //col(mi, c), col(ti, p, c)
    case 'c':                                        // col of a mx/tensor
	  check_sym(infile, 'o');
	  check_sym(infile, 'l');
	  check_sym(infile, '(');
      if(infile.peek() == 'm') {
		parser_info( "Reading col of matrix" ) ;
		check_sym(infile, 'm');
		input_val = read_num(infile);
		vector< vector<Var> >& matrix = Matrices.at(input_val) ;
		check_sym(infile, ',');
		col = read_num(infile);
		check_sym(infile, ')');
		return getColOfMatrix(matrix, col) ;
      }
		else {
		  parser_info( "Reading col of tensor" ) ;
		  check_sym(infile, 't');
		  input_val = read_num(infile);
		  vector< vector< vector<Var> > >& tensor = Tensors.at(input_val);
		  check_sym(infile, ',');
		  plane = read_num(infile);
		  check_sym(infile, ',');
		  col = read_num(infile);
		  check_sym(infile, ')');
		  return getColOfMatrix(tensor.at(plane), col) ;
		}
      default:
		throw new parse_exception("Malformed Vector Expression") ;
		break ;
  }
  D_ASSERT(0); exit(7);
}


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//Entrance:
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
int main(int argc, char* argv[]) {
  start_time = clock();
  
  if (argc == 1)
  {
    cout << "Usage: minion {options}* nameofprob.minion" << endl
	<< endl 
	<< "Options: [-findallsols]              Find all solutions" << endl 
	<< "         [-quiet] [-verbose]         Don't/do print parser progress" << endl
	<< "         [-printsols] [-noprintsols] Do/don't print solutions" << endl
	<< "         [-test]                     Run in test mode" << endl
	<< endl
	<< "Notes: In problems with an optimisation function, -findallsols is ignored" << endl
	<< "       Test mode should be run with no other options" << endl;
	
    cout << "This version of Minion was built with internal checking " <<
#ifdef NO_DEBUG
	  "off" <<
#else
	  "on" << 
#endif
	  endl << "    and verbose debug info "
#ifdef NO_PRINT
	  "off" << endl;
#else
	"on" << endl;
#endif
	cout << "This version of Minion was built to default to " << (parser_verbose ? "-verbose" : "-quiet") << endl;
	exit(0);
  }	
  
  for(int i = 1; i < argc - 1; ++i)
  {
    string command(argv[i]);
	if(command == string("-findallsols"))
	{ Controller::find_all_solutions(); }
	else if(command == string("-quiet"))
	{ parser_verbose = false; }
	else if(command == string("-verbose"))
	{ parser_verbose = true; }
	else if(command == string("-printsols"))
	{ print_solution = true; }
	else if(command == string("-noprintsols"))
	{ print_solution = false; }
	else if(command == string("-timelimit"))
	{
	  ++i;
	  time_limit = atoi(argv[i]);
	  if(time_limit == 0)
	  {
	    cout << "Did not understand timelimit:" << argv[i] << endl;
		exit(-1);
	  }
	}
	else if(command == string("-test"))
	{ 
	  Controller::test_mode = true; 
	  if(argc != 3)
	  {
	    cout << "Don't give any other command line flags with -test" << endl;
		D_ASSERT(0); exit(8);
	  }
	}
	else
	{ 
	  cout << "I don't understand '" << command << "'. Sorry." << endl;
	  D_ASSERT(0); exit(9);
	}
  }
  MinionInputReader reader;
  
  cout << "# " << VERSION << endl << "# " 
	// << REVISION << endl  // Sadly only gives revision number of minion.h
	// << "#  Run at: UTC " << asctime(gmtime(&timenow))
	<< "#    http://sourceforge.net/projects/minion" << endl;
  
  reader.read(argv[argc - 1]) ;
  
  // Used by test mode.
  bool test_checkonesol = false, test_nosols = false;
  
  if(Controller::test_mode)
  {  // Now we read the solution!
    if(reader.instance.print_matrix.size() != 1)
	{
	  cout << "Must print a matrix with one row in test mode" << endl;
	  D_ASSERT(0); exit(11);
	}
    string s;
	ifstream infile(argv[argc - 1]);
	
	// Grab the first line, which we don't want.
	char* buf = new char[10000];
	infile.getline(buf,10000);
	delete[] buf;
	
	infile >> s;
	if(s != "#TEST")
	{
	  cout << "Test files must begin with '#TEST' after the version number" << endl;
	  cout << "Instead got '" << s << "'" << endl;
	  D_ASSERT(0); exit(12);
	}
	
	infile >> s;
	if(s == "CHECKONESOL")
	  test_checkonesol = true;
	else if(s == "NOSOLS")
	  test_nosols = true;
	else
	{ 
	  cout << "I don't understand" << s << endl;
	  D_ASSERT(0); exit(13);
	}
	
    if(test_checkonesol)
	{
	  for(unsigned i = 0; i < reader.instance.print_matrix[0].size(); ++i)
	  { 
		int val;
		infile >> val;
		Controller::test_solution.push_back(val);
	  }
	  cout << Controller::test_solution.size() << endl;
	}
  }
  
  // Set up variables
  BuildCon::build_variables(reader.instance.vars);
  
  // Set up variable and value ordering
  vector<char> val_order = BuildCon::build_val_and_var_order(reader.instance);
  
  // Set up optimisation
  if(reader.instance.is_optimisation_problem)
  {
    if(reader.instance.optimise_minimising)
      Controller::optimise_minimise_var(BuildCon::get_AnyVarRef_from_Var(reader.instance.optimise_variable));
	else
	  Controller::optimise_maximise_var(BuildCon::get_AnyVarRef_from_Var(reader.instance.optimise_variable));
  }
  
  // Set up printing
  Controller::print_matrix.resize(reader.instance.print_matrix.size());
  for(unsigned i = 0; i < reader.instance.print_matrix.size(); ++i)
  {
    for(unsigned j = 0; j < reader.instance.print_matrix[i].size(); ++j)
	  Controller::print_matrix[i].push_back(BuildCon::get_AnyVarRef_from_Var(reader.instance.print_matrix[i][j]));
  }
  
  // Impose Constraints
  for(unsigned i = 0; i < reader.instance.constraints.size(); ++i)
  {
#ifdef DYNAMICTRIGGERS
    if(reader.instance.constraints[i].is_dynamic())
	  Controller::add_constraint(BuildCon::build_dynamic_constraint(reader.instance.constraints[i]));
	else
#endif
      Controller::add_constraint(BuildCon::build_constraint(reader.instance.constraints[i]));
  }
  
  setup_time = clock();
  cout << "Setup Time: " << (setup_time - start_time) / (1.0 * CLOCKS_PER_SEC) << endl;
  
  Controller::solve1(val_order);

  int setup2_time = clock();
  cout << "Setup2 Time: " << (setup2_time - setup_time) / (1.0 * CLOCKS_PER_SEC) << endl;
  
  Controller::solve_loop(var_order, val_order);
  
  // Solve!
  //Controller::solve(val_order);
  
  if(Controller::test_mode)
  {
    if(test_checkonesol)
	  if(Controller::solutions == 0)
	  {
	    cerr << "Error! Should be a solution!" << endl;
		D_ASSERT(0); exit(14);
	  }
		if(test_nosols)
		  if(Controller::solutions != 0)
		  {
			cerr << "Error! Should be no solutions!" << endl;
			D_ASSERT(0); exit(15);
		  }
  }
	clock_t end_time = clock();
	
	cout << "Solve Time: " << (end_time - setup2_time) / (1.0 * CLOCKS_PER_SEC) << endl;
	cout << "Total Time: " << (end_time - start_time) / (1.0 * CLOCKS_PER_SEC) << endl;
	cout << "Total Nodes: " << nodes << endl;
	cout << "Problem solvable?: " 
	  << (Controller::solutions == 0 ? "no" : "yes") << endl;
	cout << "Solutions Found: " << Controller::solutions << endl;
	return 0;
}

/* Minion
* Copyright (C) 2006
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

#ifdef TRIES
#include "tries.h"
#endif

/*
 struct literal
 {
   int var;
   int val;
   literal(int _var, int _val) : var(_var), val(_val)
   {}
 };*/


template<typename VarArray>
struct GACTableConstraint : public DynamicConstraint
{
  typedef typename VarArray::value_type VarRef;
  VarArray vars;
  
#ifdef TRIES
  TupleTrieArray<VarArray> tupleTrieArray;
  //Following is setup globally in constraint to be passed by reference & recycled
  int* recyclableTuple;

  /// For each literal, the number of the tuple that supports it.
  //   renamed off from current_support in case both run in parallel
  vector<int> trie_current_support;
#endif
  /// Original smallest value from each domain.
  vector<int> dom_smallest;
  /// Original size of each domain.
  vector<int> dom_size;
  
  /// Total number of literals in the variables at the start of search.
  
  int literal_num;
  
  /// Used by get_literal.
  vector<vector<int> > _map_vars_to_literal;
  
  /// Used to get a variable/value pair from a literal
  vector<pair<int,int> > _map_literal_to_vars;
  
  /// For each literal, a list of the tuples it is present in.  
  vector<vector<vector<int> > > literal_specific_tuples;
  
  /// For each literal, the number of the tuple that supports it.
  //vector<int> current_support;
#ifdef WATCHEDLITERALS
  MemOffset _current_support;
#else
  BackTrackOffset _current_support;
#endif
  int* current_support()
  { return (int*)(_current_support.get_ptr()); }
  
  /// Returns the tuple currently supporting a given literal.
  vector<int>& supporting_tuple(int i)
  { return literal_specific_tuples[i][current_support()[i]]; }
  
  /// Maps a variable/value pair to a literal.
  int get_literal(int var_num, int dom_num)
  { return _map_vars_to_literal[var_num][dom_num - dom_smallest[var_num]]; }
  
  pair<int,int> get_varval_from_literal(int literal)
  { return _map_literal_to_vars[literal]; }
  
  
  /// Sets up the variable/value pair to literal mapping, used by @get_literal.
  int setup_get_literal()
  {
	_map_vars_to_literal.resize(dom_size.size());
	// For each variable / value pair, get a literal
	int literal_count = 0;
	for(unsigned i = 0; i < dom_size.size(); ++i)
	{
	  _map_vars_to_literal[i].resize(dom_size[i]);
	  for(int j = 0; j < dom_size[i]; ++j)
	  {
		_map_vars_to_literal[i][j] = literal_count;
		_map_literal_to_vars.push_back(make_pair(i, j + dom_smallest[i]));
		D_ASSERT(get_literal(i, j + dom_smallest[i]) == literal_count);
		D_ASSERT(get_varval_from_literal(literal_count).first == i);
		D_ASSERT(get_varval_from_literal(literal_count).second == j + dom_smallest[i]); 
		++literal_count;
	  }
	}
	return literal_count;
  }
  
  /// Check if all allowed values in a given tuple are still in the domains of the variables.
  bool check_tuple(const vector<int>& v)
  {
	for(unsigned i = 0; i < v.size(); ++i)
	{
	  if(!vars[i].inDomain(v[i]))
		return false;
	}
	return true;
  }
  
  
  GACTableConstraint(const VarArray& _vars, const vector<vector<int> >& tuples) :
	vars(_vars)
#ifdef TRIES
	,tupleTrieArray(vars, tuples)
#endif
  { 
	  int arity = tuples[0].size();	  
	  D_ASSERT(_vars.size() == arity);
	  
	  // Set up the table of tuples.
	  for(unsigned i = 0; i < arity; ++i)
	  {
		int min_val = tuples[0][i];
		int max_val = tuples[0][i];
		for(unsigned j = 1; j < tuples.size(); ++j)
		{
		  min_val = mymin(min_val, tuples[j][i]);
		  max_val = mymax(max_val, tuples[j][i]);
		}
		dom_smallest.push_back(min_val);
		dom_size.push_back(max_val - min_val + 1);
	  }
	  
	  literal_num = setup_get_literal();
	  
	  // For each literal, store the set of tuples which it allows.
	  for(unsigned i = 0; i < dom_size.size(); ++i)
	  {
		for(int j = dom_smallest[i]; j < dom_smallest[i] + dom_size[i]; ++j)
		{
		  vector<vector<int> > specific_tuples;
		  for(unsigned k = 0; k < tuples.size(); ++k)
		  {
			if(tuples[k][i] == j)
			  specific_tuples.push_back(tuples[k]);
		  }
		  literal_specific_tuples.push_back(specific_tuples);
		  D_ASSERT(literal_specific_tuples.size() - 1 == get_literal(i,j));
		}
	  }
	  //current_support.resize(literal_num); 
      _current_support.request_bytes(literal_num * sizeof(int));
#ifdef TRIES

	  // For each literal, store the set of tuples which it allows.
  
      trie_current_support.resize(literal_num); 
	  // initialise supportting tuple for recycle
	  recyclableTuple = new int[arity] ;
	  // Following is incantation copied from non-tries case above
	  
#endif
 
  }
  
  int dynamic_trigger_count()
  { return literal_num * ( vars.size() - 1) ; }
  
  
  bool find_new_support(int literal)
  {
#ifdef TRIES
	 pair<int,int> varval = get_varval_from_literal(literal);
         int new_support = 
           tupleTrieArray.getTrie(varval.first).nextSupportingTuple(
                                                  trie_current_support[literal]);
         if (new_support < 0)
         { return false; }
         else
         { 
           trie_current_support[literal] = new_support;
           return true;
         };
#endif
	int support = current_support()[literal];
	vector<vector<int> >& tuples = literal_specific_tuples[literal];
	int support_size = tuples.size();
	for(int i = support; i < support_size; ++i)
	{
	  if(check_tuple(tuples[i]))
	  {
		current_support()[literal] = i;
		return true;
	  }
	}
	
#ifdef WATCHEDLITERALS
	for(int i = 0; i < support; ++i)
	{
	  if(check_tuple(tuples[i]))
	  {
		current_support()[literal] = i;
		return true;
	  }
	}
#endif
	return false;
  }
  
  virtual void propogate(DynamicTrigger* propogated_trig)
  {
	D_INFO(1, DI_TABLECON, "Propogation Triggered: " + to_string(propogated_trig));
	DynamicTrigger* dt = dynamic_trigger_start();
	int trigger_pos = propogated_trig - dt;
	int propogated_literal = trigger_pos / (vars.size() - 1);
	
	bool is_new_support = find_new_support(propogated_literal);
	pair<int,int> varval = get_varval_from_literal(propogated_literal);
	if(is_new_support)
	{
	  D_INFO(1, DI_TABLECON, "Found new support!");
	  setup_watches(varval.first, varval.second);
                // better to just pass in varval.first and propogated_literal
                // setup_watches does not need value and recomputes lit
	}
	else
	{
	  D_INFO(1, DI_TABLECON, "Failed to find new support");
	  vars[varval.first].removeFromDomain(varval.second);
	}
  }  
  
  void setup_watches(int var, int val)
  {
	int lit = get_literal(var, val);
	vector<int>& support = supporting_tuple(lit);

#ifdef TRIES
	  tupleTrieArray.getTrie(var).reconstructTuple(recyclableTuple,trie_current_support[lit]);
        // HERE IS THE place to check if support = recyclableTuple for debugging
#endif 

	DynamicTrigger* dt = dynamic_trigger_start();
	
	int vars_size = vars.size();
	dt += lit * (vars_size - 1);
	for(int v = 0; v < vars_size; ++v)
	{
	  if(v != var)
	  {
		vars[v].addDynamicTrigger(dt, DomainRemoval, support[v]);
		++dt;
	  }
	}
  }
  
  virtual void full_propogate()
  { 
#ifdef TRIES
	  // For each literal, store the first support 
          for(int varIndex = 0; varIndex < vars.size(); ++varIndex) 
          { 
            for(int i = 0; i < dom_size[varIndex]; ++i) 
            { 
              trie_current_support[varIndex] = 
                tupleTrieArray.getTrie(varIndex).identifySupportingTuple(i + dom_smallest[varIndex]);
			  tupleTrieArray.getTrie(varIndex).
			  reconstructTuple(recyclableTuple, i + dom_smallest[varIndex]);
			  cout << varIndex << "," << i << ":";
			  for(int z = 0; z < vars.size(); ++z)
			    cout << recyclableTuple[z] << " ";
			  cout << endl;
            } 
          }

    // NOTHING DONE IN FULL_PROPOGATE TO CONVERT TO TRIES
    cout << "Nothing has been done in full propagate to make it work in tries" << endl;
    // Specifically, lines to delete unsupported values need work 
    // Note that in initialisation of Constraint, current_supports are initialised
    // should possibly be here
    
#endif
	for(unsigned i = 0; i < vars.size(); ++i)
	{
	  int dom_min = dom_smallest[i];
	  int dom_max = dom_smallest[i] + dom_size[i];
	  D_INFO(2, DI_TABLECON, "Var " + to_string(i) + " pruned to [" + 
			 to_string(dom_min) + "," + to_string(dom_max - 1) + "]");
	  vars[i].setMin(dom_min);
	  vars[i].setMax(dom_max - 1);
	  
	  for(int x = vars[i].getMin(); x <= vars[i].getMax(); ++x)
	  {
		int literal = get_literal(i, x);
		if(literal_specific_tuples[literal].empty())
		{
		  vars[i].removeFromDomain(x);
		  D_INFO(2, DI_TABLECON, "No tuple supports " + to_string(x) + " in var " + to_string(i));
		}
		else
		{
		  current_support()[get_literal(i, x)] = 0;
		  bool is_new_support = find_new_support(get_literal(i, x));
		  
		  if(!is_new_support)
		  {
			D_INFO(2, DI_TABLECON, "No valid support for " + to_string(x) + " in var " + to_string(i));
			vars[i].removeFromDomain(x);
		  }
		  else
		  { setup_watches(i, x); }
		}
	  }
	}
  }
};


template<typename VarArray>
DynamicConstraint*
GACTableCon(const VarArray& vars, const vector<vector<int> >& tuples)
{ return new GACTableConstraint<VarArray>(vars, tuples); }

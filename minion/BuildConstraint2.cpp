/* Minion Constraint Solver
   http://minion.sourceforge.net
   
   For Licence Information see file LICENSE.txt 

   $Id$
*/

/*
 *  BuildConstraint2.cpp
 *  cutecsp
 *
 *  Created by Chris Jefferson on 17/03/2006.
 *  Copyright 2006 __MyCompanyName__. All rights reserved.
 *
 */

#include "minion.h"
#include "CSPSpec.h"


namespace BuildCon
{  

vector<AnyVarRef>
get_AnyVarRef_from_Var(StateObj* stateObj, const vector<Var>& vec)
{
  vector<AnyVarRef> ret_vec;
  ret_vec.reserve(vec.size());
  
  for(int i = 0; i < vec.size(); ++i)
    ret_vec.push_back(get_AnyVarRef_from_Var(stateObj, vec[i]));
  
  return ret_vec;
}

/// Helper function used in a few places.
AnyVarRef
get_AnyVarRef_from_Var(StateObj* stateObj, Var v)
{
  switch(v.type)
		{
		  case VAR_BOOL:
			return AnyVarRef(getVars(stateObj).getBooleanContainer().get_var_num(v.pos));
		  case VAR_NOTBOOL:
		    return AnyVarRef(VarNotRef(getVars(stateObj).getBooleanContainer().get_var_num(v.pos)));
		  case VAR_BOUND:
			return AnyVarRef(getVars(stateObj).getBoundvarContainer().get_var_num(v.pos));
		  case VAR_SPARSEBOUND:
			return AnyVarRef(getVars(stateObj).getSparseBoundvarContainer().get_var_num(v.pos));
		  case VAR_DISCRETE:
			return AnyVarRef(getVars(stateObj).getBigRangevarContainer().get_var_num(v.pos));
		  case VAR_SPARSEDISCRETE:	
			INPUT_ERROR("Sparse Discrete not supported at present");
		  case VAR_CONSTANT:
			return AnyVarRef(ConstantVar(stateObj, v.pos));
		  default:
		    INPUT_ERROR("Unknown Error.");
		}
}

    /// Build the variable and value ordering used.
	/// The var order is placed, the val order is returned.
	pair<vector<AnyVarRef>, vector<int> > build_val_and_var_order(StateObj* stateObj, ProbSpec::CSPInstance& instance)
  {
	  vector<int> final_val_order;
	  vector<AnyVarRef> final_var_order;
	  if(instance.var_order.size() < instance.val_order.size())
      INPUT_ERROR("Variable order cannot be shorter than value order.");
    
    if(instance.var_order.size() > instance.val_order.size())
    {
      getOptions(stateObj).print("# Var order size = " + to_string(instance.var_order.size()));  
      getOptions(stateObj).print( ", Val order size = " + to_string(instance.val_order.size()));
      getOptions(stateObj).printLine( ", so padding val order.");
    
      instance.val_order.insert(instance.val_order.end(), 
        instance.var_order.size() - instance.val_order.size(), instance.val_order.back());
    }
      
    D_ASSERT(instance.val_order.size() == instance.var_order.size());
	    
	
	  for(unsigned int i = 0 ;i < instance.var_order.size(); ++i)
	  {
		final_val_order.push_back(instance.val_order[i]);
		final_var_order.push_back(get_AnyVarRef_from_Var(stateObj, instance.var_order[i]));
	  }
	  return make_pair(final_var_order, final_val_order);
  }	

  /// Create all the variables used in the CSP.
  void build_variables(StateObj* stateObj, const ProbSpec::VarContainer& vars)
  {
    getVars(stateObj).getBooleanContainer().setVarCount(vars.BOOLs);
	getVars(stateObj).getBoundvarContainer().addVariables(vars.bound);
    getVars(stateObj).getSparseBoundvarContainer().addVariables(vars.sparse_bound);
    getVars(stateObj).getBigRangevarContainer().addVariables(vars.discrete);
	
	for(unsigned int i = 0; i < vars.sparse_discrete.size(); ++i)
	{ INPUT_ERROR("Sparse discrete disabled at present due to bugs. Sorry."); }
  }
	
}




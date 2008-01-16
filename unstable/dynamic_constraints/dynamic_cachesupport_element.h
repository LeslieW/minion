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

// The triggers in this constraint are set up as follows:
// If the length of the vector is L.

// The first 2 * Dom(Result) literals are, for some j
//   literal 2 * i : attached to assignment i to V[j]
//	 literal 2 * i + 1 : attached to the assignment j in IndexVar 

// After this there are 2 * Dom(Index) literals are, for some j
// literal 2 * i : attached to j in V[i]
// literal 2 * i + 1 : attached to j in Result



template<typename VarArray, typename Index, typename Result>
struct ElementConstraintDynamic : public DynamicConstraint
{
  typedef typename VarArray::value_type VarRef;
  
  VarArray var_array;
  Index indexvar;
  Result resultvar;
  
  int initial_result_dom_min;
  int initial_result_dom_max;
  
/****
 * Following added for debugging undoable int array
 
  BackTrackOffset _test;
  
  int test_count;
  int& test(int i)
  { return static_cast<int*>(_test.get_ptr())[i]; }

  bool check() 
  {
    for(int i=0; i < test_count ; ++i)
    { if (test(i) != backtrack_support(i)) return false;
    }
    return true;
  }
*/

  UndoableIntArray _current_support;
  
  int& backtrack_support(int i)
  { return _current_support.array(i); }
  
  ElementConstraintDynamic(const VarArray& _var_array, const Index& _index, const Result& _result) :
	var_array(_var_array), indexvar(_index), resultvar(_result)
  { 
	  initial_result_dom_min = resultvar.getInitialMin();
	  initial_result_dom_max = resultvar.getInitialMax();
  }
  
  int dynamic_trigger_count()
  {
	int count = var_array.size() * 2 + 
	(initial_result_dom_max - initial_result_dom_min + 1) * 2 
	+ 1 
	+ 1; 
        _current_support.initialise(
                    count/2-1, 
                    2*(initial_result_dom_max - initial_result_dom_min+1)*(var_array.size()+1));
        // _test.request_bytes(sizeof(int)*(count/2-1));
        // test_count = count/2 - 1;
	return count;
  }
  
  void find_new_support_for_result(int j)
  {
	int realj = j + initial_result_dom_min;
	
    if(!resultvar.inDomain(realj))
	  return;
	
	int array_size = var_array.size();
    
    // support is value of index
    int support = max(backtrack_support(j + array_size), indexvar.getMin());  // old support probably just removed
    int maxsupport = indexvar.getMax();
    
	
    DynamicTrigger* dt = dynamic_trigger_start();
    while(support <= maxsupport && 
		  !(indexvar.inDomain_noBoundCheck(support) && var_array[support].inDomain(realj)))
      ++support;
    if(support > maxsupport)
    { 
        D_INFO(2, DI_DYELEMENT, "No support for " + to_string(realj) + " in result");
        resultvar.removeFromDomain(realj); 
        return;
    }

    var_array[support].addDynamicTrigger(dt + 2*j, DomainRemoval, realj);
    indexvar.addDynamicTrigger(dt + 2*j + 1, DomainRemoval, support);
    _current_support.set(j+var_array.size(),support);
    // test(j+var_array.size()) = support;
  }
  
  void find_new_support_for_index(int i)
  {
    if(!indexvar.inDomain(i))
	  return;
	
	int resultvarmin = resultvar.getMin();
	int resultvarmax = resultvar.getMax();
	DynamicTrigger* dt = dynamic_trigger_start() + 
	                     (initial_result_dom_max - initial_result_dom_min + 1) * 2;
						 
	if(resultvarmin == resultvarmax)
	{
	  if(!var_array[i].inDomain(resultvarmin))
	    indexvar.removeFromDomain(i);
	  else
	  {
	    var_array[i].addDynamicTrigger(dt + 2*i, DomainRemoval, resultvarmin);
	    resultvar.addDynamicTrigger(dt + 2*i + 1, DomainRemoval, resultvarmin);
	    _current_support.set(i,resultvarmin);
            // test(i) = resultvarmin;
	  }
	  return;
	}
	

    // support is value of result
    int support = max(backtrack_support(i), resultvarmin); 
    int maxsupport = resultvarmax;
	
    //int support = initial_result_dom_min;
	while(support <= maxsupport &&
		  !(resultvar.inDomain_noBoundCheck(support) && var_array[i].inDomain(support)))
	  ++support;
	  
	if(support > maxsupport)
	{ 
	    D_INFO(2, DI_DYELEMENT, "No support for " + to_string(i) + " in index");
	    indexvar.removeFromDomain(i); 
		return;
	}
	
	var_array[i].addDynamicTrigger(dt + 2*i, DomainRemoval, support);
	resultvar.addDynamicTrigger(dt + 2*i + 1, DomainRemoval, support);
	_current_support.set(i,support);
        // test(i) = support;
  }
  
  
  void deal_with_assigned_index()
  {
    D_ASSERT(indexvar.isAssigned());
    int indexval = indexvar.getAssignedValue();
    VarRef var = var_array[indexval];
	
    int lower = resultvar.getMin(); 
    if( lower > var.getMin() ) 
    {
      var.setMin(lower);
      ++lower;                      // do not need to check lower bound, we know it's in resultvar
    }
	
    int upper = resultvar.getMax(); 
    if( upper < var.getMax() ) 
    {
      var.setMax(upper);
      --upper;                      // do not need to check upper bound, we know it's in resultvar
    }
    
    for(int i = lower; i <= upper; ++i)
    {
      if(!(resultvar.inDomain(i)))
        var.removeFromDomain(i); 
    }
  }
  
  virtual void full_propogate()
  {
	D_INFO(2, DI_DYELEMENT, "Setup Triggers");
	int array_size = var_array.size(); 
	int result_dom_size = initial_result_dom_max - initial_result_dom_min + 1;
	
	// Setup SupportLostForIndexValue(i,j)
	// Here we are supporting values in the index variable
	// So for each variable in the index variable, we want to ensure
	
	// Couple of quick sanity-propagations.
	// We define UNDEF = false ;)
	indexvar.setMin(0);
	indexvar.setMax(array_size - 1);
	for(int i = 0; i < array_size; ++i)
	{
	  _current_support.set(i,initial_result_dom_min-1);        // will be incremented if support sought
          // test(i)=initial_result_dom_min-1;
	  if(indexvar.inDomain(i))
	    find_new_support_for_index(i);
	}
	
	for(int i = 0; i < result_dom_size; ++i)
	{
	  _current_support.set(i+array_size,-1);        // will be incremented if support sought
          // test(i+array_size) = -1;
	  if(resultvar.inDomain(i + initial_result_dom_min))
	    find_new_support_for_result(i);
	}
	
	DynamicTrigger* dt = dynamic_trigger_start();
	
	dt += var_array.size() * 2 +
	  (initial_result_dom_max - initial_result_dom_min + 1) * 2;
	
	// for(int i = initial_result_dom_min; i <= initial_result_dom_max; ++i)
	// {
	// resultvar.addDynamicTrigger(dt, DomainRemoval, i);
	// ++dt;
	// }
	resultvar.addDynamicTrigger(dt, DomainChanged);
	++dt;
	
	indexvar.addDynamicTrigger(dt, Assigned);
	
  }
  
  
  virtual void propogate(DynamicTrigger* trig)
  {
    D_INFO(2, DI_DYELEMENT, "Start Propagation");
	DynamicTrigger* dt = dynamic_trigger_start();
	unsigned pos = trig - dt;
	unsigned array_size = var_array.size();
	int result_support_triggers = (initial_result_dom_max - initial_result_dom_min + 1) * 2;
	int index_support_triggers =  array_size * 2;
	// int when_index_assigned_triggers = (initial_result_dom_max - initial_result_dom_min + 1);
	if(pos < result_support_triggers)
	{// It was a value in the result var which lost support
	  D_INFO(2, DI_DYELEMENT, "Find new support for result var assigned " + to_string(pos/2));
          _current_support.undo();
          // D_ASSERT(check());
	  find_new_support_for_result(pos / 2);
	  return;
	}
	pos -= result_support_triggers;
	
	if(pos < index_support_triggers)
	{// A value in the index var lost support
	  D_INFO(2, DI_DYELEMENT, "Find new support for index var assigned " + to_string(pos/2));
          _current_support.undo();
          // D_ASSERT(check());
	  find_new_support_for_index( pos / 2 );
	  return;
	}
	pos -= index_support_triggers;
	
	// if(pos < when_index_assigned_triggers)
	if (pos == 0)
	{ // A value was removed from result var
	  if(indexvar.isAssigned())
	  {
		deal_with_assigned_index();
	    //D_ASSERT(!resultvar.inDomain(pos + initial_result_dom_min));
		// D_INFO(2, DI_DYELEMENT, "indexvar assigned, so must remove " + to_string(pos + initial_result_dom_min) + " from var " + to_string(indexvar.getAssignedValue()));
	    //var_array[indexvar.getAssignedValue()].removeFromDomain(pos + initial_result_dom_min);
	  }
	  return;
	}
	
	D_ASSERT(pos == 1);
    // index has become assigned.
	
	D_INFO(2, DI_DYELEMENT, "Index var assigned " + to_string(indexvar.getAssignedValue()));
	
	deal_with_assigned_index();
  }
};

template<typename VarArray, typename VarRef1, typename VarRef2>
DynamicConstraint*
ElementConDynamic(const VarArray& vararray, const VarRef1& v1, const VarRef2& v2)
{ 
  return new ElementConstraintDynamic<VarArray, VarRef1, VarRef2>(vararray, v1, v2); 
}


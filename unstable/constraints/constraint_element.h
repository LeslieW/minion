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


template<typename VarArray, typename IndexRef>
struct ElementConstraint : public Constraint
{
  typedef typename VarArray::value_type VarRef;
  VarArray var_array;
  IndexRef index_ref;
  VarRef result_var;
  ElementConstraint(const VarArray& _var_array, const IndexRef& _index_ref, const VarRef& _result_var) :
    var_array(_var_array), index_ref(_index_ref), result_var(_result_var)
  { }
  
  virtual triggerCollection setup_internal()
  {
    D_INFO(2,DI_SUMCON,"Setting up Constraint");
    triggerCollection t;
    int array_size = var_array.size();
    for(int i = 0; i < array_size; ++i)
	  t.push_back(make_trigger(var_array[i], Trigger(this, i), Assigned));
	
	t.push_back(make_trigger(index_ref, Trigger(this, -1), Assigned));
	t.push_back(make_trigger(result_var, Trigger(this, -2), Assigned));
    return t;
  }
  
  //  virtual Constraint* reverse_constraint()
  
  
  virtual void propogate(int prop_val, DomainDelta)
  {
    if(index_ref.isAssigned())
    {
      int index = index_ref.getAssignedValue();
	  if(index < 0 || index >= var_array.size())
	  {
	    Controller::fail();
		return;
	  }
	  int val_min = max(result_var.getMin(), var_array[index].getMin());
	  int val_max = min(result_var.getMax(), var_array[index].getMax());
	  result_var.setMin(val_min);
	  var_array[index].setMin(val_min);
	  result_var.setMax(val_max);
	  var_array[index].setMax(val_max);
    }
    else
    {
      if(prop_val>=0)
      {
		int assigned_val = var_array[prop_val].getAssignedValue();
		if(!result_var.inDomain(assigned_val))
		  index_ref.removeFromDomain(prop_val);
      }
      else
      {
		D_ASSERT(prop_val == -2);
		int assigned_val = result_var.getAssignedValue();
		int array_size = var_array.size();
		for(int i = 0; i < array_size; ++i)
		{
		  if(!var_array[i].inDomain(assigned_val))
			index_ref.removeFromDomain(i);
		}
      }
    }
  }
  
  /*  virtual bool check_unsat(int i, DomainDelta)
  { 
    if(index_ref.isAssigned())
	  return (x.getMin() > y.getMax() + offset.val()); 
  }*/
  
  virtual void full_propogate()
  {
    if(index_ref.isAssigned())
    {
      int index = index_ref.getAssignedValue();
      int val_min = max(result_var.getMin(), var_array[index].getMin());
      int val_max = min(result_var.getMax(), var_array[index].getMax());
      result_var.setMin(val_min);
      var_array[index].setMin(val_min);
      result_var.setMax(val_max);
      var_array[index].setMax(val_max);
    }  
    //
    // need to do the other cases too, surely
    //
	
    int array_size = var_array.size();
	
    for(int i=0;i<array_size;i++)
	{
	  if(var_array[i].isAssigned())
	  {
		int assigned_val = var_array[i].getAssignedValue();
		if(!result_var.inDomain(assigned_val)) 
		  index_ref.removeFromDomain(i);
	  }
	}
	
    if(result_var.isAssigned())
    {
      int assigned_val = result_var.getAssignedValue();
      for(int i = 0; i < array_size; ++i)
      {
        if(!var_array[i].inDomain(assigned_val))
          index_ref.removeFromDomain(i); 
      }
    }
  }
  
  virtual bool check_assignment(vector<int> v)
  {
    int length = v.size();
    if(v[length-2] < 0 || v[length-2] > length - 3)
	  return false;
    return v[v[length-2]] == v[length-1];
  }
  
  virtual vector<AnyVarRef> get_vars()
  { 
    vector<AnyVarRef> array(var_array.size() + 2);
	for(unsigned int i=0;i<var_array.size(); ++i)
	  array[i] = AnyVarRef(var_array[i]);
    array[var_array.size()] = AnyVarRef(index_ref);
    array[var_array.size() + 1] = AnyVarRef(result_var);
    return array;
  }
};

template<typename VarArray, typename VarRef1, typename VarRef2>
Constraint*
ElementCon(const VarArray& vararray, const VarRef1& v1, const VarRef2& v2)
{ 
  return new ElementConstraint<VarArray, VarRef1>(vararray, v1, v2); 
}





template<typename VarArray, typename MinVarRef>
struct MinConstraint : public Constraint
{
  //typedef BoolLessSumConstraint<VarArray, VarSum,1-VarToCount> NegConstraintType;
  typedef typename VarArray::value_type ArrayVarRef;
  
  VarArray var_array;
  MinVarRef min_var;
  
  MinConstraint(const VarArray& _var_array, const MinVarRef& _min_var) :
	var_array(_var_array), min_var(_min_var)
  { }
  
  virtual triggerCollection setup_internal()
  {
	D_INFO(2,DI_SUMCON,"Setting up Constraint");
	triggerCollection t;
	
	for(unsigned int i=0; i < var_array.size(); ++i)
	{
	  t.push_back(make_trigger(var_array[i], Trigger(this, i), LowerBound));
	  t.push_back(make_trigger(var_array[i], Trigger(this, -i), UpperBound));
	}
	t.push_back(make_trigger(min_var, Trigger(this, var_array.size() ),LowerBound));
	t.push_back(make_trigger(min_var, Trigger(this, -var_array.size() ),UpperBound));
	
	return t;
  }
  
  //  virtual Constraint* reverse_constraint()
  
  virtual void propogate(int prop_val, DomainDelta)
  {
	if(prop_val > 0)
	{// Lower Bound Changed
	  if(prop_val == static_cast<int>(var_array.size()))  
	  {
		int new_min = min_var.getMin();
		typename VarArray::iterator end = var_array.end();
		for(typename VarArray::iterator it = var_array.begin(); it < var_array.end(); ++it)
		  (*it).setMin(new_min);
	  }
	  else
	  {
		int min = big_constant;
		for(typename VarArray::iterator it = var_array.begin(); it < var_array.end(); ++it)
		{
		  int new_min = (*it).getMin();
		  if(min > new_min)
			min = new_min;
		}
		min_var.setMin(min);
	  }
	}
	else
	{// Upper Bound Changed
	  prop_val = -prop_val;
	  if(prop_val == static_cast<int>(var_array.size()))
	  {
		typename VarArray::iterator it = var_array.begin();
		int minvar_max = min_var.getMax();
		while(it != var_array.end() && (*it).getMin() > minvar_max)
		  ++it;
		if(it == var_array.end())
		{
		  Controller::fail();
		  return;
		}
		
		// Possibly this variable is the one that can be the minimum
		typename VarArray::iterator it_copy(it);
		++it;
		while(it != var_array.end() && (*it).getMin() > minvar_max)
		  ++it;
		if(it != var_array.end())
		{ // No, another variable can be the minimum
		  return;
		}
		
		it_copy->setMax(minvar_max);
	  }
	  else
	  {
		min_var.setMax(var_array[prop_val].getMax());
	  }
	}
	
  }
  
  //  virtual bool check_unsat(int i, DomainDelta)
  //  {
  
  
  virtual void full_propogate()
  {
	int array_size = var_array.size();
	for(int i = 1;i <= array_size; ++i)
	{
	  propogate(i,0);
	  propogate(-i,0);
	}
  }
  
  virtual bool check_assignment(vector<int> v)
  {
	D_ASSERT(v.size() == var_array.size() + 1);
	int min_val = big_constant;
	int array_size = v.size();
	for(int i=0;i < array_size - 1;i++)
	  min_val = min(min_val, v[i]);
	return min_val == v.back();
  }

  virtual vector<AnyVarRef> get_vars()
  {
    vector<AnyVarRef> vars(var_array.size() + 1);
	for(unsigned i = 0; i < var_array.size(); ++i)
	  vars[i] = AnyVarRef(var_array[i]);
	vars[var_array.size()] = AnyVarRef(min_var);
	return vars;
  }
};

template<typename VarArray, typename VarRef>
Constraint*
MinCon(const VarArray& _var_array, const VarRef& _var_ref)
{ 
  return (new MinConstraint<VarArray,VarRef>(_var_array, _var_ref)); 
}

template<typename VarArray, typename VarRef>
Constraint*
MaxCon(const VarArray& _var_array, const VarRef& _var_ref)
{ 
  return (new MinConstraint<typename NegType<VarArray>::type, typename NegType<VarRef>::type>(VarNegRef(_var_array),
																					          VarNegRef(_var_ref))); 
}





struct StaticBranch
{
  template<typename VarType>
  int operator()(vector<VarType>& var_order, int pos)
  {
	unsigned v_size = var_order.size();
	while(pos < v_size && var_order[pos].isAssigned())
	  ++pos;
	return pos;
  }
};

struct SlowStaticBranch
{
  template<typename VarType>
  int operator()(vector<VarType>& var_order, int pos)
  {
	unsigned v_size = var_order.size();
	pos = 0;
	while(pos < v_size && var_order[pos].isAssigned())
	  ++pos;
	return pos;
  }
};

#ifdef WDEG
//see Boosting Systematic Search by Weighting Constraints by Boussemart et al
struct WdegBranch
{
  template<typename VarType>
  int operator()(vector<VarType>& var_order, int pos)
  {
    int best = var_order.size(); //the variable with the best score so far (init to none)
    int best_score = -1; //... and its score (all true scores are positive)
    size_t var_order_size = var_order.size();
    for(size_t i = 0; i < var_order_size; i++) { //we will find the score for each var
      //cout << "i=" << i << endl;
      //cout << "best=" << best << endl;
      //cout << "best_score=" << best_score << endl;	
      VarType& v = var_order[i];
      if(v.isAssigned()) {
	//cout << "assigned -- stop" << endl;
	continue;
      }
      int base_wdeg = v.getBaseWdeg();
      //cout << "basewdeg=" << base_wdeg << endl;
      if(base_wdeg <= best_score) {
	//cout << "too low before deductions" << endl;
	continue; //stop if base score is too low before deductions
      }
      vector<AbstractConstraint*>* constrs = v.getConstraints();
      size_t constrs_size = constrs->size();
      for(size_t j = 0; j < constrs_size; j++) { //find constrs to be deducted from var wdeg
	AbstractConstraint* c = (*constrs)[j];
	//cout << "con wdeg=" << c->getWdeg() << endl;
	vector<AnyVarRef>* c_vars = c->get_vars_singleton();
	size_t c_vars_size = c_vars->size();
	int uninst = 0;
	for(size_t k = 0; k < c_vars_size; k++) 
	  if(!(*c_vars)[k].isAssigned())
	    if(++uninst > 1) { //when multiple unassigned we needn't deduct
	      //cout << "don't deduct" << endl;
	      break;
	    }
	if(uninst <= 1) {
	  D_ASSERT(uninst == 1);
	  //cout << "deduct" << endl;
	  base_wdeg -= c->getWdeg();
	  if(base_wdeg <= best_score) {
	    //cout << "too low during deductions" << endl;
	    break;
	  }
	}
      }
      //cout << "basewdeg=" << base_wdeg << endl;
      if(best_score < base_wdeg)
      {
	//cout << "replacing top score" << endl;
	best_score = base_wdeg;
	best = i;
      }
    }
    return best;
  }
};
#endif

struct SDFBranch
{
  template<typename VarType>
  int operator()(vector<VarType>& var_order, int pos)
  {
	int length = var_order.size();
	int smallest_dom = length;
	DomainInt dom_size = DomainInt_Max;
	
	
	for(int i = 0; i < length; ++i)
	{
	  DomainInt maxval = var_order[i].getMax();
	  DomainInt minval = var_order[i].getMin();
	  
	  if((maxval != minval) && ((maxval - minval) < dom_size) )
	  {
		dom_size = maxval - minval;
		smallest_dom = i;
		if( maxval - minval == 1)
		{ // Binary domain, must be smallest
		  return i;
		}
	  }
	}
	return smallest_dom;
  }
};

struct SRFBranch
{
  template<typename VarType>
  int operator()(vector<VarType>& var_order, int pos)
  {
	int length = var_order.size();
	int smallest_dom = length;
	
  float ratio = 2;
	
	
	for(int i = 0; i < length; ++i)
	{
	  DomainInt maxval = var_order[i].getMax();
	  DomainInt minval = var_order[i].getMin();
	  
    DomainInt original_minval = var_order[i].getInitialMin();
    DomainInt original_maxval = var_order[i].getInitialMax();
    
    float new_ratio = (checked_cast<float>(maxval - minval) * 1.0) / checked_cast<float>(original_maxval - original_minval);
	  if((maxval != minval) && (new_ratio < ratio) )
	  {
		  ratio = new_ratio;
		  smallest_dom = i;
	  }
	}
	return smallest_dom;
  }
};


struct LDFBranch
{
  template<typename VarType>
  int operator()(vector<VarType>& var_order, int pos)
  {
	int length = var_order.size();
	
	pos = 0;
	while(pos < length && var_order[pos].isAssigned())
	  ++pos;
	if(pos == length)
	  return length;
	
	int largest_dom = pos;
	DomainInt dom_size = var_order[pos].getMax() - var_order[pos].getMin();
	
	++pos;
	
	for(; pos < length; ++pos)
	{
	  DomainInt maxval = var_order[pos].getMax();
	  DomainInt minval = var_order[pos].getMin();
	  
	  if(maxval - minval > dom_size)
	  {
		dom_size = maxval - minval;
		largest_dom = pos;
	  }
	}
	return largest_dom;
  }
};

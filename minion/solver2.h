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
// This header is designed to be included after all other headers



VARDEF_ASSIGN(clock_t time_limit, 0);
VARDEF(clock_t start_time);
VARDEF(int nodes);


namespace Controller
{
  VARDEF_ASSIGN(bool _find_one_sol,true);
  VARDEF_ASSIGN(AnyVarRef* optimise_var, NULL);
  VARDEF(int current_optimise_position);
  VARDEF_ASSIGN(bool optimise, false);
  VARDEF(vector<TriggerRange> propogate_trigger_list);
  VARDEF(vector<Constraint*> constraints);
#ifdef DYNAMICTRIGGERS
  VARDEF(vector<DynamicConstraint*> dynamic_constraints);
#endif
  VARDEF(int solutions);
  VARDEF(vector<vector<AnyVarRef> > print_matrix);
  VARDEF_ASSIGN(bool test_mode, false);
  VARDEF(vector<int> test_solution);
  
#ifdef DYNAMICTRIGGERS
  VARDEF(vector<DynamicTrigger*> dynamic_trigger_list);
#endif  
  
  inline void find_all_solutions()
  {
    _find_one_sol = false;
  }
  
  /// Pushes the state of the whole world.
  inline void world_push()
  {
    D_INFO(0,DI_SOLVER,"World Push");
    backtrackable_memory.world_push();
  }
  
  /// Pops the state of the whole world.
  inline void world_pop()
  {
    D_INFO(0,DI_SOLVER,"World Pop");
    backtrackable_memory.world_pop();
  }
  
  
  
  
  
  template<typename VarRef>
    void optimise_maximise_var(VarRef var)
  {
      _find_one_sol = false;
      optimise_var = new AnyVarRef(var);
      optimise = true;
  }
  
  template<typename VarRef>
    void optimise_minimise_var(VarRef var)
  {
      _find_one_sol = false;
      optimise_var = new AnyVarRef(VarNeg<VarRef>(var));
      optimise = true;
  }
  
  inline void push_triggers(TriggerRange new_triggers)
  { 
    D_INFO(1, DI_QUEUE, string("Adding ") + to_string(new_triggers.end - new_triggers.start)
						+ string(" new triggers. Trigger list size is ") + 
						to_string(propogate_trigger_list.size()) + ".");
	propogate_trigger_list.push_back(new_triggers); 
  }
  
#ifdef DYNAMICTRIGGERS
  inline void push_dynamic_triggers(DynamicTrigger* new_dynamic_trig_range)
  { 
    D_ASSERT(new_dynamic_trig_range->sanity_check_list());
    dynamic_trigger_list.push_back(new_dynamic_trig_range);   
  }
#endif
  

// next_queue_ptr is defined in constraint_dynamic.
// It is used if pointers are moved around.

  inline void propogate_queue()
  {
    D_INFO(2, DI_QUEUE, "Starting Propogation");
	bool* fail_ptr = &Controller::failed;
	
#ifdef DYNAMICTRIGGERS
      if (dynamic_triggers_used) 
      {
	while(!propogate_trigger_list.empty() || !dynamic_trigger_list.empty())
	{
	  while(!dynamic_trigger_list.empty())
	  {
		DynamicTrigger* t = dynamic_trigger_list.back();
		D_INFO(1, DI_QUEUE, string("Checking queue ") + to_string(t));
		dynamic_trigger_list.pop_back();
		DynamicTrigger* it = t->next;

		while(it != t)
		{
		  if(*fail_ptr) 
		  {
			propogate_trigger_list.clear();
			dynamic_trigger_list.clear();
			return; 
		  }
		  D_INFO(1, DI_QUEUE, string("Checking ") + to_string(it));
		  next_queue_ptr = it->next;
		  D_INFO(1, DI_QUEUE, string("Will do ") + to_string(next_queue_ptr) + " next");
		  it->propogate();
		  it = next_queue_ptr;
		}
	  }
	  
        /* Don't like code duplication here but a slight efficiency gain */

	  while(!propogate_trigger_list.empty())
	  {
		TriggerRange t = propogate_trigger_list.back();
		short data_val = t.data;
		propogate_trigger_list.pop_back();
		
		for(Trigger* it = t.start; it != t.end ; it++)
		{
		  if(*fail_ptr) 
		  {
			propogate_trigger_list.clear();
			dynamic_trigger_list.clear();
			return; 
		  }
		  //cerr << string(rangevar_container) << endl;
		  it->propogate(data_val);
		}
	  }
        }
      }
      else
#endif
	  while(!propogate_trigger_list.empty())
	  {
		TriggerRange t = propogate_trigger_list.back();
		short data_val = t.data;
		propogate_trigger_list.pop_back();
		
		for(Trigger* it = t.start; it != t.end ; it++)
		{
		  if(*fail_ptr) 
		  {
			propogate_trigger_list.clear();
			return; 
		  }
		  //cerr << string(rangevar_container) << endl;
		  it->propogate(data_val);
		}
	  }
  }
  
  
  inline void add_constraint(Constraint* c)
  { constraints.push_back(c); }
  
#ifdef DYNAMICTRIGGERS
  inline void add_constraint(DynamicConstraint* c)
  { dynamic_constraints.push_back(c); }
#endif
  
  inline void setup_constraints()
  {
    size_t size = constraints.size();
    for(size_t i = 0 ; i < size;i++)
    {
      constraints[i]->setup();
      constraints[i]->full_propogate();
	  propogate_queue();
    }
  }
  
  /// Lists all structures that must be locked before search.
  // @todo This could be done more neatly... 
  inline void lock()
  {
    D_INFO(2, DI_SOLVER, "Starting Locking process");
    rangevar_container.lock();
	big_rangevar_container.lock();
    sparse_boundvar_container.lock();
    boolean_container.lock(); 
    boundvar_container.lock();
#ifdef DYNAMICTRIGGERS
	int dynamic_size = dynamic_constraints.size();
	for(int i = 0; i < dynamic_size; ++i)
	  dynamic_constraints[i]->setup();
#endif
    backtrackable_memory.lock();
    memory_block.lock();
    atexit(Controller::finish);
    setup_constraints();
    TriggerSpace::finaliseTriggerLists();
    backtrackable_memory.final_lock();
    memory_block.final_lock();    
#ifdef DYNAMICTRIGGERS
	for(int i = 0; i < dynamic_size; ++i)
	{
	  dynamic_constraints[i]->full_propogate();
	  propogate_queue();
	}
#endif
  }
  
  
  
  template<typename VarRef, typename ValArray>
    inline void solve_loop(vector<VarRef>& v, vector<ValArray>& choose_min_val)
  {
      D_INFO(0, DI_SOLVER, "Non-Boolean Search");
      vector<pair<int,int> > assignment_list;
      size_t v_size = v.size();
      size_t pos = 0;
      while(pos < v_size && v[pos].isAssigned())
		++pos;
      
      while(true)
      {
		nodes++;
		if((nodes & 1023) == 0)
		{
		  if(time_limit != 0)
		  {
		    if( (clock() - start_time) >= (time_limit * CLOCKS_PER_SEC) )
			{
			  cout << "Time out." << endl;
			  return;
			}
		  }
		}

		//display();
		int assign_val;
		if(pos == v_size)
		{  
		  ++solutions;
		  if(!print_matrix.empty())
		  {
			for(unsigned i = 0; i < print_matrix.size(); ++i)
			{
			  for(unsigned j = 0; j < print_matrix[i].size(); ++j)
			  {
				if(!print_matrix[i][j].isAssigned())
				  cout << "[" << print_matrix[i][j].getMin() << "," << print_matrix[i][j].getMax() << "]";
				else
				  cout << print_matrix[i][j].getAssignedValue() << " ";
			  }
			  cout << endl;
			}
			cout << endl;
		  }
		  if(_find_one_sol)
		  {
		    if(Controller::test_mode)
			{
			  vector<int> sol;
			  bool match = true;
			  for(unsigned i = 0; i < print_matrix[0].size(); ++i)
			  {
			    if(!print_matrix[0][i].isAssigned())
	            { 
				  cout << "Test variable " << i << "not assigned!" << endl;
				  sol.push_back(-1);
				  match = false;
				}
				else
				{ sol.push_back(print_matrix[0][i].getAssignedValue()); }
			  }
			  
			  if(sol != test_solution)
			    match = false;
			  
			  if(match == false)
			  {
				cerr << "Test failed!" << endl;
				cerr << "Generated sol:";
				for(unsigned i = 0; i < sol.size(); ++i)
				  cerr << sol[i] << " ";
				cerr << endl;
				cerr << "From test case:";
				for(unsigned i = 0; i < test_solution.size(); ++i)
				  cerr << test_solution[i] << " ";
				cerr << endl;
				D_ASSERT(0); exit(19);
			  }
			}
			return;
		  }
		  if(optimise)
		  {
		   	cout << "Solution found with Value: " 
			<< optimise_var->getAssignedValue() << endl;
			
			current_optimise_position = optimise_var->getAssignedValue() + 1;
			/*
			 if(!print_matrix.empty())
			 {
			   for(unsigned i = 0; i < print_matrix.size(); ++i)
			   {
				 for(unsigned j = 0; j < print_matrix[i].size(); ++j)
				 {
				   if(!print_matrix[i][j].isAssigned())
					 cout << "? ";
				   else
					 cout << print_matrix[i][j].getAssignedValue() << " ";
				 }
				 cout << endl;
			   }
			   cout << endl;
			 }
			 */
		   	cout << "New optimisation Value: " << current_optimise_position << endl;
			
		  }
		  // TODO : Make this more easily changable.
		  #ifndef NO_PRINT_SOLUTIONS
		  cout << "Solution Number: " << solutions << endl;
		  cout << "Time: " << (clock() - setup_time) / (1.0 * CLOCKS_PER_SEC) << endl;
		  cout << "Nodes: " << nodes << endl << endl;
		  #endif
		  
		  if(assignment_list.empty())
			return;
		  pair<int,int> p = assignment_list.back();
		  pos = p.first;
		  assign_val = p.second;
		  assignment_list.pop_back();
		  failed = true;
		  D_ASSERT(pos != v_size);
		  if(optimise)
			optimise_var->setMin(current_optimise_position);
		  // We do this to force the backtrack code to trigger
		  
		}
		else
		{
		  D_ASSERT(!v[pos].isAssigned());
		  if(choose_min_val[pos])
			assign_val = v[pos].getMin();
		  else
			assign_val = v[pos].getMax();
		  world_push();
		  v[pos].uncheckedAssign(assign_val);
		  
		  propogate_queue();
		}
		
		if(failed)
		{
		  failed = false;
		  world_pop();
		  if(choose_min_val[pos])
			v[pos].setMin(assign_val + 1);
		  else
			v[pos].setMax(assign_val - 1);
		  if(optimise)
			optimise_var->setMin(current_optimise_position);
		  propogate_queue();
		  if(failed)
		  {
			while(failed)
			{
			  failed = false;
			  if(assignment_list.empty())
				return;
			  world_pop();
			  pair<int,int> p = assignment_list.back();
			  pos = p.first;
			  assign_val = p.second;
			  assignment_list.pop_back();
			  if(choose_min_val[pos])
				v[pos].setMin(assign_val + 1);
			  else
				v[pos].setMax(assign_val - 1);
			  if(optimise)
				optimise_var->setMin(current_optimise_position);
			  propogate_queue();
			}
		  }
		}
		else
		{
		  assignment_list.push_back(make_pair(pos, assign_val));
		  ++pos;
		}
		
		while(pos < v_size && v[pos].isAssigned())
		  ++pos;
      }
  }
  
  
  inline void solve()
  {
	lock();
	vector<char> var_choices(var_order.size(), 1);
	
	solutions = 0;  
	nodes = 0;
	if(optimise)
	  current_optimise_position = optimise_var->getMin(); 
	solve_loop(var_order,var_choices);
  }


  /// This function allows seperate timing of search
  /// and full setup.
  template<typename ValArray>
  inline bool solve1(ValArray& val)
  {
  	  D_ASSERT(var_order.size() == val.size());
	  solutions = 0;  
	  nodes = 0;
	  lock();
	  /// Failed initially propagating constraints!
	  if(Controller::failed)
	    return false;
	  if(optimise)
	    current_optimise_position = optimise_var->getMin();  
	  return true; 
  }
  
  template<typename ValArray>
	inline void solve(ValArray& val)
  {
	  D_ASSERT(var_order.size() == val.size());
	  solutions = 0;  
	  nodes = 0;
	  lock();
	  /// Failed initially propagating constraints!
	  if(Controller::failed)
	    return;
	  if(optimise)
		current_optimise_position = optimise_var->getMin(); 
	  solve_loop(var_order, val);
  }
}

/*
 inline void solve_loop(vector<BoolVarRef>& v, unsigned int pos)
 {
   D_INFO(0,DI_SOLVER,"Boolean Branching");
   int v_size = v.size();
   nodes++;
   //if(nodes >= 1000000) D_ASSERT(0); exit(1);
   
   // Find first unassigned variable
   while(pos < v_size && v[pos].isAssigned())
	 ++pos;
   // Is this a solution?
   if(pos == v_size)
   {
	 if(optimise)
	   current_optimise_position = optimise_var->getMin() + 1;
#ifdef FIND_ONE_SOL
	 cout <<   nodes << endl;
	 exit(0);
#endif
	 ++solutions;
	 return;
   }
   
   // Try assigning 0
   world_push();
   if(optimise)
	 optimise_var->setMin(current_optimise_position);
   v[pos].uncheckedAssign(0);
   propogate_queue();
   if(!failed)
	 solve_loop(v, pos+1);
   failed=false;
   world_pop();
   
   // Now assign 1
   v[pos].uncheckedAssign(1);
   if(optimise)
	 optimise_var->setMin(current_optimise_position);
   propogate_queue();
   if(!failed)
	 solve_loop(v, pos+1);
   failed=false;
   
   return;
 }
 */


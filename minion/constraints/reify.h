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

template<typename BoolVar>
struct reify : public Constraint
{
  Constraint* poscon;
  Constraint* negcon;
  BoolVar rar_var;
  
  reify(Constraint* _poscon, BoolVar v) : poscon(_poscon), rar_var(v)
  { negcon = poscon->reverse_constraint();}
  
  virtual Constraint* reverse_constraint()
  {
    cerr << "You can't reverse a reified Constraint!";
    D_ASSERT(0); exit(28);
  }
  
  virtual bool check_assignment(vector<int> v)
  {
    int back_val = v.back();
    v.pop_back();
    if(back_val)
      return poscon->check_assignment(v);
    else
      return negcon->check_assignment(v);
  }
  
  virtual vector<AnyVarRef> get_vars()
  { 
    cerr << "Can't table constriant a rarified constraint at the moment.. sorry"; 
    D_ASSERT(0); exit(29);
  }
  
  virtual triggerCollection setup_internal()
  {
    D_INFO(2,DI_REIFY,"Setting up rarification");
    triggerCollection postrig = poscon->setup_internal();
    triggerCollection negtrig = negcon->setup_internal();
    triggerCollection triggers;
    for(unsigned int i=0;i<postrig.size();i++)
    {
      postrig[i]->trigger.info = postrig[i]->trigger.info * 2;
      postrig[i]->trigger.constraint = this;
      triggers.push_back(postrig[i]);
    }
    
    for(unsigned int i=0;i<negtrig.size();i++)
    {
      negtrig[i]->trigger.info = negtrig[i]->trigger.info * 2 + 1;
      negtrig[i]->trigger.constraint = this;
      triggers.push_back(negtrig[i]);
    }
    
    triggers.push_back(make_trigger(rar_var, Trigger(this, -99999), LowerBound));
    triggers.push_back(make_trigger(rar_var, Trigger(this, -99998), UpperBound));
    return triggers;
  }
  
  virtual void propogate(int i, DomainDelta domain)
  {
    D_INFO(1,DI_REIFY,"Propogation Start");
    if(i == -99998 || i == -99999)
    {
      if(i==-99999)
      {
		D_INFO(1,DI_REIFY,"Full Pos Propogation");
		poscon->full_propogate();
      }
      else
      {
		D_INFO(1,DI_REIFY,"Full Neg Propogation");
		negcon->full_propogate();
      }
      return;
    }
    
    if(rar_var.isAssigned())
    {
      if(rar_var.getAssignedValue())
      { if(i%2 == 0) poscon->propogate(i/2, domain); }
      else
      { if(i%2 == 1) negcon->propogate((i-1)/2, domain); }
    }
    else
    {
      if(i%2 == 0)
      { 
		if(poscon->check_unsat(i/2, domain)) 
		{ 
		  D_INFO(1,DI_REIFY,"Constraint False");
		  //D_ASSERT(0);
		  rar_var.uncheckedAssign(false);
		}
      }
      else
      { 
		if(negcon->check_unsat((i-1)/2,domain)) 
		{
		  D_INFO(1,DI_REIFY,"Constraint True");
		  rar_var.uncheckedAssign(true);
		}
      }
    }
  }
  
  virtual void full_propogate()
  {
    if(poscon->full_check_unsat())
	{
	  D_INFO(1,DI_REIFY,"Pos full_check_unsat true!");
      rar_var.propogateAssign(false);
	}
	
    if(negcon->full_check_unsat())
	{
	  D_INFO(1,DI_REIFY,"False full_check_unsat true!");
      rar_var.propogateAssign(true);
	}
    
    if(rar_var.isAssigned())
    {
      if(rar_var.getAssignedValue())
		poscon->full_propogate();
      else
		negcon->full_propogate();
    }
  }
};

// We add things passed to rarify in here to make sure we don't lose them.
VARDEF(vector<Constraint* > reify_cache);

template<typename BoolVar>
reify<BoolVar>*
reifyCon(Constraint* c, BoolVar var)
{ 
  reify_cache.push_back(c);
  return new reify<BoolVar>(&*c, var); 
}


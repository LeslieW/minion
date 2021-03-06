/*
* Minion http://minion.sourceforge.net
* Copyright (C) 2006-09
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

#ifndef CONSTRAINT_TABLE_H_ERT
#define CONSTRAINT_TABLE_H_ERT

template<typename VarArray>
struct TableConstraint : public AbstractConstraint
{

  virtual string extended_name()
  { return "table(basic)"; }

  virtual string constraint_name()
  { return "table"; }
  
  typedef typename VarArray::value_type VarRef;
  VarArray vars;
  AbstractConstraint* constraint;
  
  TableConstraint(const VarArray& _vars, AbstractConstraint* c) :
    vars(_vars), constraint(c)
  { 
    CheckNotBound(vars, "table constraint");
  }
  
  virtual vector<AnyVarRef> get_vars()
  { return vars; }
  
  virtual BOOL check_assignment(DomainInt* v, SysInt v_size)
  { return constraint->check_assignment(v, v_size); }
  
  virtual triggerCollection setup_internal()
  {
    triggerCollection t;
    for(UnsignedSysInt i=0; i < vars.size(); ++i)
    {
      t.push_back(make_trigger(vars[i],Trigger(this, 0), DomainChanged));
    }
    return t;
  }
  
  
  bool increment(vector<DomainInt>& v, UnsignedSysInt check_var)
  {
    for(UnsignedSysInt i=0;i<v.size();i++)
    {
      if(i == check_var)
        continue;
      ++v[i];
      while(v[i] != vars[i].getMax()+1 && !vars[i].inDomain(v[i]))
        ++v[i];
      if(v[i] != vars[i].getMax()+1)
        return true;
      v[i] = vars[i].getMin();
    }
    return false;
  }
  
  virtual void propagate(DomainInt, DomainDelta)
  {
    PROP_INFO_ADDONE(Table);
    for(UnsignedSysInt check_var = 0; check_var < vars.size(); check_var++)
    {
      //cerr << vars[check_var].data.var_num << vars[check_var].getMin() << "```" << vars[check_var].getMax() << vars[check_var].inDomain(0) <<  endl;
      for(DomainInt check_dom = vars[check_var].getMin();
          check_dom <= vars[check_var].getMax(); check_dom++)
      {
        vector<DomainInt> v(vars.size());
        for(UnsignedSysInt i=0;i<vars.size();i++)
          v[i] = vars[i].getMin();
        v[check_var] = check_dom;
        BOOL satisfied = false;
        do
        {
          if(constraint->check_assignment(v))
          { 
            satisfied = true; 
          }
        } while(!satisfied && increment(v, check_var));
        if(!satisfied)
        {
          vars[check_var].removeFromDomain(check_dom);
        }
      }
    }
  }  
  
  virtual void full_propagate()
  { propagate(0,DomainDelta::empty()); }
};

#endif

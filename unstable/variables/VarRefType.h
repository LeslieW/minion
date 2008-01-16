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

#ifndef VARREFTYPE_H
#define VARREFTYPE_H

template<typename GetContainer, typename InternalRefType>
struct VarRefType
{
  InternalRefType data;
  VarRefType(const InternalRefType& _data) : data(_data)
  {}
  
  VarRefType() 
  {}
  
  VarRefType(const VarRefType& b) : data(b.data)
  {}
  
  bool isAssigned()
  { return GetContainer::con().isAssigned(data); }
  
  int getAssignedValue()
  { return GetContainer::con().getAssignedValue(data); }
  
  bool isAssignedValue(int i)
  { 
    return GetContainer::con().isAssigned(data) &&
    GetContainer::con().getAssignedValue(data) == i;
  }
  
  bool inDomain(int b)
  { return GetContainer::con().inDomain(data, b); }

  bool inDomain_noBoundCheck(int b)
  { return GetContainer::con().inDomain_noBoundCheck(data, b); }
  
  int getMax()
  { return GetContainer::con().getMax(data); }
  
  int getMin()
  { return GetContainer::con().getMin(data); }

  int getInitialMax()
  { return GetContainer::con().getInitialMax(data); }
  
  int getInitialMin()
  { return GetContainer::con().getInitialMin(data); }
  
  void setMax(int i)
  { GetContainer::con().setMax(data,i); }
  
  void setMin(int i)
  { GetContainer::con().setMin(data,i); }
  
  void uncheckedAssign(int b)
  { GetContainer::con().uncheckedAssign(data, b); }
  
  void propogateAssign(int b)
  { GetContainer::con().propogateAssign(data, b); }
  
  void removeFromDomain(int b)
  { GetContainer::con().removeFromDomain(data, b); }
  
  void addLowerBoundTrigger(Trigger t)
  { GetContainer::con().addLowerBoundTrigger(data, t); }
  
  void addUpperBoundTrigger(Trigger t)
  { GetContainer::con().addUpperBoundTrigger(data, t); }
  
  void addAssignedTrigger(Trigger t)
  { GetContainer::con().addAssignedTrigger(data, t); }
  
  void addDomainChangedTrigger(Trigger t)
  { GetContainer::con().addDomainChangedTrigger(data, t); }
  
  void addTrigger(Trigger t, TrigType type, int val = -999)
  { GetContainer::con().addTrigger(data, t, type, val); }

  operator string()
  {
    ostringstream s;
    s << "Bool";
    s << data.var_num;
    return s.str();
  }
  
  int getDomainChange(DomainDelta d)
  { return d.XXX_get_domain_diff(); }
  
#ifdef DYNAMICTRIGGERS
  void addDynamicTrigger(DynamicTrigger* t, TrigType type, int pos = -999)
  {  GetContainer::con().addDynamicTrigger(data, t, type, pos); }
#endif
};


template<typename GetContainer, typename InternalRefType>
struct QuickVarRefType
{
  InternalRefType data;
  QuickVarRefType(const InternalRefType& _data) : data(_data)
  {}
  
  QuickVarRefType() 
  {}
  
  QuickVarRefType(const QuickVarRefType& b) : data(b.data)
  {}
  
  bool isAssigned()
  { return data.isAssigned(); }
  
  int getAssignedValue()
  { return data.getAssignedValue(); }
  
  bool isAssignedValue(int i)
  { 
    return data.isAssigned() &&
    data.getAssignedValue() == i;
  }
  bool inDomain(int b)
  { return data.inDomain(b); }
  
  bool inDomain_noBoundCheck(int b)
  { return data.inDomain_noBoundCheck(b); }

  int getMax()
  { return data.getMax(); }
  
  int getMin()
  { return data.getMin(); }

  int getInitialMax()
  { return data.getInitialMax(); }
  
  int getInitialMin()
  { return data.getInitialMin(); }
  
  void setMax(int i)
  { GetContainer::con().setMax(data,i); }
  
  void setMin(int i)
  { GetContainer::con().setMin(data,i); }
  
  void uncheckedAssign(int b)
  { GetContainer::con().uncheckedAssign(data, b); }
  
  void propogateAssign(int b)
  { GetContainer::con().propogateAssign(data, b); }
  
  void removeFromDomain(int b)
  { GetContainer::con().removeFromDomain(data, b); }
  
  void addLowerBoundTrigger(Trigger t)
  { GetContainer::con().addLowerBoundTrigger(data, t); }
  
  void addUpperBoundTrigger(Trigger t)
  { GetContainer::con().addUpperBoundTrigger(data, t); }
  
  void addAssignedTrigger(Trigger t)
  { GetContainer::con().addAssignedTrigger(data, t); }
  
  void addDomainChangedTrigger(Trigger t)
  { GetContainer::con().addDomainChangedTrigger(data, t); }
  
  void addTrigger(Trigger t, TrigType type, int val = -999)
  { GetContainer::con().addTrigger(data, t, type, val); }
  
  operator string()
  {
    ostringstream s;
    s << "Bool";
    s << data.var_num;
    return s.str();
  }
  
  int getDomainChange(DomainDelta d)
  { return d.XXX_get_domain_diff(); }
  
#ifdef DYNAMICTRIGGERS
  void addDynamicTrigger(DynamicTrigger* t, TrigType type, int pos = -999)
  {  GetContainer::con().addDynamicTrigger(data, t, type, pos); }
#endif
};


template<typename InternalRefType>
struct CompleteVarRefType
{
  InternalRefType data;
  CompleteVarRefType(const InternalRefType& _data) : data(_data)
  {}
  
  CompleteVarRefType() 
  {}
  
  CompleteVarRefType(const CompleteVarRefType& b) : data(b.data)
  {}
  
  bool isAssigned()
  { return (data.getCon()).isAssigned(data); }
  
  int getAssignedValue()
  { return (data.getCon()).getAssignedValue(data); }
  
  bool isAssignedValue(int i)
  { 
    return (data.getCon()).isAssigned(data) &&
    (data.getCon()).getAssignedValue(data) == i;
  }
  bool inDomain(int b)
  { return (data.getCon()).inDomain(data, b); }
  
  int getMax()
  { return (data.getCon()).getMax(data); }
  
  int getMin()
  { return (data.getCon()).getMin(data); }

  int getInitialMax()
  { return (data.getCon()).getInitialMax(data); }
  
  int getInitialMin()
  { return (data.getCon()).getInitialMin(data); }
  
  void setMax(int i)
  { (data.getCon()).setMax(data,i); }
  
  void setMin(int i)
  { (data.getCon()).setMin(data,i); }
  
  void uncheckedAssign(int b)
  { (data.getCon()).uncheckedAssign(data, b); }
  
  void propogateAssign(int b)
  { (data.getCon()).propogateAssign(data, b); }
  
  void removeFromDomain(int b)
  { (data.getCon()).removeFromDomain(data, b); }
  
  void addLowerBoundTrigger(Trigger t)
  { (data.getCon()).addLowerBoundTrigger(data, t); }
  
  void addUpperBoundTrigger(Trigger t)
  { (data.getCon()).addUpperBoundTrigger(data, t); }
  
  void addAssignedTrigger(Trigger t)
  { (data.getCon()).addAssignedTrigger(data, t); }
  
  void addDomainChangedTrigger(Trigger t)
  { (data.getCon()).addDomainChangedTrigger(data, t); }
  
  void addTrigger(Trigger t, TrigType type, int val = -999)
  { (data.getCon()).addTrigger(data, t, type, val); }
  
  operator string()
  {
    ostringstream s;
    s << "Bool";
    s << data.var_num;
    return s.str();
  }
  
  int getDomainChange(DomainDelta d)
  { return d.XXX_get_domain_diff(); }
  
#ifdef DYNAMICTRIGGERS
  void addDynamicTrigger(DynamicTrigger* t, TrigType type, int pos = -999)
  {  (data.getCon()).addDynamicTrigger(data, t, type, pos); }
#endif
};


template<typename VarRef>
struct PassThrough
{
  VarRef data;
  PassThrough(const VarRef& _data) : data(_data)
  {}
  
  PassThrough() 
  {}
  
  PassThrough(const PassThrough& b) : data(b.data)
  {}
  
  bool isAssigned()
  { return data.isAssigned(); }
  
  int getAssignedValue()
  { return data.getAssignedValue(); }
  
  bool isAssignedValue(int i)
  { 
    return data.isAssigned() &&
    data.getAssignedValue() == i;
  }
  bool inDomain(int b)
  { return data.inDomain( b); }
  
  int getMax()
  { return data.getMax(); }
  
  int getMin()
  { return data.getMin(); }

  int getInitialMax()
  { return data.getInitialMax(); }
  
  int getInitialMin()
  { return data.getInitialMin(); }
  
  void setMax(int i)
  { data.setMax(i); }
  
  void setMin(int i)
  { data.setMin(i); }
  
  void uncheckedAssign(int b)
  { data.uncheckedAssign(b); }
  
  void propogateAssign(int b)
  { data.propogateAssign(b); }
  
  void removeFromDomain(int b)
  { data.removeFromDomain(b); }
  
  void addLowerBoundTrigger(Trigger t)
  { data.addLowerBoundTrigger(t); }
  
  void addUpperBoundTrigger(Trigger t)
  { data.addUpperBoundTrigger(t); }
  
  void addAssignedTrigger(Trigger t)
  { data.addAssignedTrigger(t); }
  
  void addDomainChangedTrigger(Trigger t)
  { data.addDomainChangedTrigger(t); }
  
  void addTrigger(Trigger t, TrigType type, int val = -999)
  { data.addTrigger(t, type, val); }
  
  operator string()
  {
    ostringstream s;
    s << "PassThrough:";
    s << string(data);
    return s.str();
  }
  
  int getDomainChange(DomainDelta d)
  { return data.getDomainChange(d); }
  
};



/// Internal type used by AnyVarRef.
struct AnyVarRef_Abstract
{
  virtual bool isAssigned() = 0;  
  virtual int getAssignedValue() = 0;
  virtual bool isAssignedValue(int i) = 0;
  virtual bool inDomain(int b) = 0;
  virtual bool inDomain_noBoundCheck(int b) = 0;
  virtual int getMax() = 0;
  virtual int getMin() = 0;
  virtual int getInitialMax() = 0;
  virtual int getInitialMin() = 0;
  virtual void setMax(int i) = 0;
  virtual void setMin(int i) = 0;
  virtual void uncheckedAssign(int b) = 0;
  virtual void propogateAssign(int b) = 0;
  virtual void removeFromDomain(int b) = 0;
  virtual void addLowerBoundTrigger(Trigger t) = 0;
  virtual void addUpperBoundTrigger(Trigger t) = 0;
  virtual void addAssignedTrigger(Trigger t) = 0;
  virtual void addDomainChangedTrigger(Trigger t) = 0;
  virtual void addTrigger(Trigger t, TrigType type, int val = -999) = 0;

  virtual operator string() = 0;
  
  virtual ~AnyVarRef_Abstract()
  {}
  
  virtual int getDomainChange(DomainDelta d) = 0;
#ifdef DYNAMICTRIGGERS
  virtual void addDynamicTrigger(DynamicTrigger* t, TrigType type, int pos = -999) = 0;
#endif
};

/// Internal type used by AnyVarRef.
template<typename VarRef>
struct AnyVarRef_Concrete : public AnyVarRef_Abstract
{
  VarRef data;
  AnyVarRef_Concrete(const VarRef& _data) : data(_data)
  {}
  
  AnyVarRef_Concrete() 
  {}
  
  AnyVarRef_Concrete(const AnyVarRef_Concrete& b) : data(b.data)
  {}
  
  virtual bool isAssigned()
  { return data.isAssigned(); }
  
  virtual int getAssignedValue()
  { return data.getAssignedValue(); }
  
  virtual bool isAssignedValue(int i)
  { 
    return data.isAssignedValue(i);
//    return data.isAssigned() &&
//    data.getAssignedValue() == i;
  }
  
  virtual bool inDomain(int b)
  { return data.inDomain(b); }
  
  virtual bool inDomain_noBoundCheck(int b)
  { return data.inDomain_noBoundCheck(b); }
  
  virtual int getMax()
  { return data.getMax(); }
  
  virtual int getMin()
  { return data.getMin(); }

  virtual int getInitialMax()
  { return data.getInitialMax(); }
  
  virtual int getInitialMin()
  { return data.getInitialMin(); }
  
  virtual void setMax(int i)
  { data.setMax(i); }
  
  virtual void setMin(int i)
  { data.setMin(i); }
  
  virtual void uncheckedAssign(int b)
  { data.uncheckedAssign(b); }
  
  virtual void propogateAssign(int b)
  { data.propogateAssign(b); }
  
 virtual  void removeFromDomain(int b)
  { data.removeFromDomain(b); }
  
  virtual void addLowerBoundTrigger(Trigger t)
  { data.addLowerBoundTrigger(t); }
  
  virtual void addUpperBoundTrigger(Trigger t)
  { data.addUpperBoundTrigger(t); }
  
  virtual void addAssignedTrigger(Trigger t)
  { data.addAssignedTrigger(t); }
  
  virtual void addDomainChangedTrigger(Trigger t)
  { data.addDomainChangedTrigger(t); }
  
  virtual void addTrigger(Trigger t, TrigType type, int val = -999)
  { data.addTrigger(t, type, val); }
  
  virtual operator string()
  {
    ostringstream s;
    s << "VirtualRef:";
    s << string(data);
    return s.str();
  }
  
  virtual ~AnyVarRef_Concrete()
  {}
  
  int getDomainChange(DomainDelta d)
  { return data.getDomainChange(d); }

#ifdef DYNAMICTRIGGERS
  void addDynamicTrigger(DynamicTrigger* t, TrigType type, int pos = -999)
  {  data.addDynamicTrigger(t, type, pos); }
#endif
};

/// Provides a method of wrapping any variable type in a general wrapper.
struct AnyVarRef
{
  shared_ptr<AnyVarRef_Abstract> data;
  template<typename VarRef>
    AnyVarRef(const VarRef& _data) 
  { data = shared_ptr<AnyVarRef_Abstract>(new AnyVarRef_Concrete<VarRef>(_data)); }
  
  AnyVarRef() 
  {}
  
  AnyVarRef(const AnyVarRef& b) : data(b.data)
  {}
  
  virtual bool isAssigned()
  { return data->isAssigned(); }
  
  virtual int getAssignedValue()
  { return data->getAssignedValue(); }
  
  virtual bool isAssignedValue(int i)
  { 
    return data->isAssigned() &&
    data->getAssignedValue() == i;
  }
  
  virtual bool inDomain(int b)
  { return data->inDomain(b); }

  virtual bool inDomain_noBoundCheck(int b)
  { return data->inDomain_noBoundCheck(b); }
  
  virtual int getMax()
  { return data->getMax(); }
  
  virtual int getMin()
  { return data->getMin(); }

  virtual int getInitialMax()
  { return data->getInitialMax(); }
  
  virtual int getInitialMin()
  { return data->getInitialMin(); }
  
  virtual void setMax(int i)
  { data->setMax(i); }
  
  virtual void setMin(int i)
  { data->setMin(i); }
  
  virtual void uncheckedAssign(int b)
  { data->uncheckedAssign(b); }
  
  virtual void propogateAssign(int b)
  { data->propogateAssign(b); }
  
  virtual  void removeFromDomain(int b)
  { data->removeFromDomain(b); }
  
  virtual void addLowerBoundTrigger(Trigger t)
  { data->addLowerBoundTrigger(t); }
  
  virtual void addUpperBoundTrigger(Trigger t)
  { data->addUpperBoundTrigger(t); }
  
  virtual void addAssignedTrigger(Trigger t)
  { data->addAssignedTrigger(t); }
  
  virtual void addDomainChangedTrigger(Trigger t)
  { data->addDomainChangedTrigger(t); }

  virtual void addTrigger(Trigger t, TrigType type, int val = -999)
  { data->addTrigger(t, type, val); }
  

  
  virtual operator string()
  {
    ostringstream s;
    s << "VirtualRef:";
    //s << string(data);
    return s.str();
  }
  
  virtual ~AnyVarRef()
  {}
  
  int getDomainChange(DomainDelta d)
  { return data->getDomainChange(d); }
  
#ifdef DYNAMICTRIGGERS
  void addDynamicTrigger(DynamicTrigger* t, TrigType type, int pos = -999)
  {  data->addDynamicTrigger(t, type, pos); }
#endif
};


VARDEF(vector<AnyVarRef> var_order);

/// Adds an array of variables to the var ordering.
template<typename T>
void add_to_var_order(vector<T>& new_vars)
{
  var_order.reserve(var_order.size() + new_vars.size());
  for(unsigned int i = 0; i < new_vars.size(); ++i)
  {
    var_order.push_back(AnyVarRef(new_vars[i]));
  }
}

// Adds a single variable to the var ordering.
template<typename T>
void add_to_var_order(T& new_var)
{ var_order.push_back(AnyVarRef(new_var)); }

// Sets the var ordering, assuming wrapping as AnyVarRefs is done by the user.
inline void set_var_order(const vector<AnyVarRef>& vars)
{ var_order = vars; }

#endif

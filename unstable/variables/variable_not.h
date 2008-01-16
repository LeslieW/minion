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


/**
 * @brief Nots a variable reference.
 *
 * Takes a variable, and returns a new 'psuedo-variable', which is the same as the not of the
 * original. This new variable takes up no extra space of any kind after compilation
 * is performed
 */
template<typename VarRef>
struct VarNot
{
  VarRef data;
  VarNot(const VarRef& _data) : data(_data)
  {}
  
  VarNot()
  {}
  
  VarNot(const VarNot& b) : data(b.data)
  {}
  
  bool isAssigned()
  { return data.isAssigned(); }
  
  bool getAssignedValue()
  { return !data.getAssignedValue(); }
  
  bool isAssignedValue(bool i)
  { 
    return data.isAssigned() &&
    data.getAssignedValue() != i;
  }
  
  bool inDomain(int b)
  { return data.inDomain(!b); }

  bool inDomain_noBoundCheck(int b)
  { return data.inDomain(!b); }
  
  int getMax()
  { return !data.getMin(); }
  
  int getMin()
  { return !data.getMax(); }

  int getInitialMax()
  { return !data.getInitialMin(); }
  
  int getInitialMin()
  { return !data.getInitialMax(); }
  
  void setMax(bool i)
  { data.setMin(!i); }
  
  void setMin(bool i)
  { data.setMax(!i); }
  
  void uncheckedAssign(bool b)
  { data.uncheckedAssign(!b); }
  
  void propogateAssign(bool b)
  { data.propogateAssign(!b); }
  
  void removeFromDomain(bool b)
  { data.removeFromDomain(!b); }
  
  void addLowerBoundTrigger(Trigger t)
  { data.addUpperBoundTrigger(t); }
  
  void addUpperBoundTrigger(Trigger t)
  { data.addLowerBoundTrigger(t); }
  
  void addAssignedTrigger(Trigger t)
  { data.addAssignedTrigger(t); }
  
  void addDomainChangedTrigger(Trigger t)
  { data.addDomainChangedTrigger(t); }
  
  void addTrigger(Trigger t, TrigType type, int val = -999)
  { data.addTrigger(t, type, val); }

  
  operator string()
  {
    ostringstream s;
    s << "Not:";
    s << string(data);
    return s.str();
  }
  
  int getDomainChange(DomainDelta d)
  { return data.getDomainChange(d); }
  
#ifdef DYNAMICTRIGGERS
  void addDynamicTrigger(DynamicTrigger* t, TrigType type, int pos = -999)
  {  data.addDynamicTrigger(t, type, pos); }
#endif
};

template<typename T>
struct NotType
{ typedef VarNot<T> type; };

template<typename T>
struct NotType<vector<T> >
{ typedef vector<VarNot<T> > type; };

template<typename T, std::size_t i>
struct NotType<array<T, i> >
{ typedef array<VarNot<T>, i> type; };


template<typename VRef>
typename NotType<VRef>::type
VarNotRef(const VRef& var_ref)
{ return VarNot<VRef>(var_ref); }

template<typename VarRef>
vector<VarNot<VarRef> >
VarNotRef(const vector<VarRef>& var_array)
{
  vector<VarNot<VarRef> > Not_array;
  Not_array.reserve(var_array.size());
  for(unsigned int i = 0; i < var_array.size(); ++i)
    Not_array.push_back(VarNotRef(var_array[i]));
  return Not_array;
}

template<typename VarRef, std::size_t i>
array<VarNot<VarRef>, i>
VarNotRef(const array<VarRef, i>& var_array)
{
  array<VarNot<VarRef>, i> Not_array;
  for(unsigned int l = 0; l < i; ++l)
    Not_array[l] = VarNotRef(var_array[l]);
  return Not_array;
}

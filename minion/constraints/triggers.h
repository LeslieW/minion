/* Minion Constraint Solver
   http://minion.sourceforge.net
   
   For Licence Information see file LICENSE.txt 

   $Id$
*/

/*
 *  triggers.h
 *  cutecsp
 *
 *  Created by Chris Jefferson on 25/03/2006.
 *  Copyright 2006 __MyCompanyName__. All rights reserved.
 *
 */

#ifndef _TRIGGER_H
#define _TRIGGER_H

#include "../system/system.h"
#include "../constants.h"
#include "../propagation_data.h"

class Trigger;
class AbstractConstraint;
 
/// Container for a range of triggers
class TriggerRange
{
  /// Start of triggers
  Trigger* start;
  /// End of triggers
  Trigger* finish;

public:
	
  Trigger* begin() const
  { return start; }
  
  Trigger* end() const
  { return finish; }
  
  /// The domain delta from the domain change.
  /** This may not contain the actual delta, but contains data from which a variable can
   construct it, by passing it to getDomainChange. */
  int data;
  TriggerRange(Trigger* s, Trigger* e, int _data) : start(s), finish(e), data(_data)
  { 
    D_ASSERT(data >= DomainInt_Min);
    D_ASSERT(data <= DomainInt_Max);
  }
};



///The classes which are used to build the queue.
class Trigger
{ 
public:
  /// The constraint to be propagated.
  AbstractConstraint* constraint;
  /// The first value to be passed to the propagate function.
  int info;
  
  template<typename T>
    Trigger(T* _sc, int _info) : constraint(_sc), info(_info)
  {  }
  
  Trigger(const Trigger& t) : constraint(t.constraint), info(t.info) 
  {}
  
  Trigger() : constraint(NULL)
  {}
  
  void inline propagate(DomainDelta domain_data);
  void full_propagate();
  // In function_defs.hpp.
};

/// Abstract Type that represents any Trigger Creator.
struct AbstractTriggerCreator
{
  Trigger trigger;
  TrigType type;
  virtual void post_trigger() = 0;
  AbstractTriggerCreator(Trigger t, TrigType _type) : trigger(t), type(_type) {}
  virtual ~AbstractTriggerCreator()
  { }
};

/**
 * @brief Concrete Trigger Creators.
 * Allows a trigger to be passed around before being imposed. This is here so reification works.
 */
template<typename VarRef>
struct TriggerCreator : public AbstractTriggerCreator
{
  VarRef* ref;
  TriggerCreator(VarRef& v, Trigger t, TrigType _type) :
    AbstractTriggerCreator(t, _type),  ref(&v)
  {}
  
  virtual void post_trigger()
  { ref->addTrigger(trigger, type); }
  
  virtual ~TriggerCreator()
  { }
};

template<typename VarRef>
inline shared_ptr<AbstractTriggerCreator> 
make_trigger(VarRef& v, Trigger t, TrigType trigger_type)
{ return shared_ptr<AbstractTriggerCreator>(new TriggerCreator<VarRef>(v,t, trigger_type));}

#endif


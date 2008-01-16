/*
 *  triggers.h
 *  cutecsp
 *
 *  Created by Chris Jefferson on 25/03/2006.
 *  Copyright 2006 __MyCompanyName__. All rights reserved.
 *
 */

/// Container for a range of triggers
struct TriggerRange
{
  /// Start of triggers
  Trigger* start;
  /// End of triggers
  Trigger* end;
  /// The domain delta from the domain change.
  /** This may not contain the actual delta, but contains data from which a variable can
   construct it, by passing it to getDomainChange. */
  short data;
  TriggerRange(Trigger* s, Trigger* e, int _data) : start(s), end(e), data(_data)
  { 
    D_ASSERT(data >= std::numeric_limits<short>::min());
    D_ASSERT(data <= std::numeric_limits<short>::max());
  }
};


///The classes which are used to build the queue.
struct Trigger
{ 
  /// The constraint to be propagated.
  Constraint* constraint;
  /// The first value to be passed to the propagate function.
  int info;
  
  template<typename T>
    Trigger(T* _sc, int _info) : constraint(_sc), info(_info)
  {  }
  
  Trigger(const Trigger& t) : constraint(t.constraint), info(t.info) 
  {}
  
  Trigger() : constraint(NULL)
  {}
  
  void propogate(DomainDelta domain_data);
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
  VarRef ref;
  int val;
  TriggerCreator(VarRef& v, Trigger t, TrigType _type, int _val = -999) :
    AbstractTriggerCreator(t, _type),  ref(v), val(_val)
  {}
  
  virtual void post_trigger()
  { 
    switch (type)
    {
      case LowerBound:
	D_INFO(0,DI_SOLVER,string("Add Lower Bound Trigger") + string(ref));
	ref.addLowerBoundTrigger(trigger);
	break;
      case UpperBound:
	D_INFO(0,DI_SOLVER,"Add Upper Bound Trigger"+ string(ref));
	ref.addUpperBoundTrigger(trigger);
	break;
      case DomainChanged:
	D_INFO(0,DI_SOLVER,"Add Domain Changed Trigger"+ string(ref));
	ref.addDomainChangedTrigger(trigger);
	break;
      case Assigned:
	D_INFO(0,DI_SOLVER,"Add Assigned Trigger" + string(ref));
	ref.addAssignedTrigger(trigger);
	break;
	  case DomainRemoval:
	D_INFO(0,DI_SOLVER,"Add Domain Removal Trigger" + to_string(val) + ":" + string(ref));
	ref.addTrigger(trigger, DomainRemoval, val);
	break;
      default:
	D_ASSERT(0); exit(40);	
    }
  }
  
  virtual ~TriggerCreator()
  { }
};

template<typename VarRef>
inline shared_ptr<AbstractTriggerCreator> 
make_trigger(VarRef v, Trigger t, TrigType trigger_type, int val = -999)
{ return shared_ptr<AbstractTriggerCreator>(new TriggerCreator<VarRef>(v,t, trigger_type, val));}


#include "../minion.h"
/* Minion Constraint Solver
   http://minion.sourceforge.net
   
   For Licence Information see file LICENSE.txt 
*/

#include "../constraints/constraint_occurrence.h"

template<typename T1>
AbstractConstraint*
BuildCT_GEQ_OCCURRENCE(StateObj* stateObj, const T1& t1, ConstraintBlob& b)
{
  const SysInt val_to_count = checked_cast<SysInt>(b.constants[0][0]);
  DomainInt occs = b.constants[1][0];
  { return ConstantOccEqualCon(stateObj, t1, val_to_count, occs, t1.size()); }
}

BUILD_CT(CT_GEQ_OCCURRENCE, 1)

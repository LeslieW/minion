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

#define FULL_DOMAIN_TRIGGERS

// Only GCC has hashtables
#ifdef __GNUC__
#define USE_HASHTABLE
#endif

// Stupid visual C++ needs a little hack.
#ifdef _MSC_VER

// We don't want no stupid safe library warnings
#define _SCL_SECURE_NO_DEPRECATE

#define DEFAULT_CALL __std_call
// Supress 'size_t -> int' warnings.
#pragma warning(disable: 4267)
// I don't even get this warning.
#pragma warning(disable: 4244)
// I'll buy a pint for anyone who can figure how to fix this..
// 'unsigned long' : forcing value to bool 'true' or 'false'. Of course I am, that's what I want to test!
#pragma warning(disable: 4800)
// At some point I might fix these "signed/unsigned mismatch" warnings...
#pragma warning(disable: 4018)
#else
#define DEFAULT_CALL
#endif

#ifdef WATCHEDLITERALS
#define DYNAMICTRIGGERS
#endif


#define VERSION "Minion Version 0.2.2"
#define REVISION "Subversion (svn) Revision Number $Revision: 164 $"
// above line will work but only gives revision of this file,
//  not the current global revision 


#ifdef NO_MAIN
#define VARDEF_ASSIGN(x,y) extern x
#define VARDEF(x) extern x
#else
#define VARDEF_ASSIGN(x,y) x = y
#define VARDEF(x) x
#endif

#include <time.h>
/// Time at which search starts.
VARDEF(clock_t setup_time);
//extern int setup_time;

#include <vector>
#include <set>
#include <limits>

#include <algorithm>
#include <numeric>
//#include <tr1/memory>
#include "linked_ptr.h"
#include "local_array.h"
//#include <tr1/tuple>

// Note: The hash table (unordered_map) is broken in many versions of g++,
// so this can't be activated safely.
#ifdef USE_HASHTABLE
#include <ext/hash_map>
namespace __gnu_cxx
{
template<typename T>
    struct hash<T*>
    {
      size_t
      operator()(T* __x) const
      { return (size_t)__x; }
    };
}
#define MAP_TYPE __gnu_cxx::hash_map

#else
#include <map>
#define MAP_TYPE map
#endif

using namespace std;
//using namespace tr1;
//using tr1::array;

#include "constants.h"
#include "debug.h"

#include "solver.h"

#include "backtrackable_memory.h"
#include "nonbacktrack_memory.h"
#include "reversible_vals.h"

/** @brief Represents a change in domain. 
 *
 * This is used instead of a simple int as the use of various mappers on variables might mean the domain change needs
 * to be corrected. Every variable should implement the function getDomainChange which uses this and corrects the domain.
 */
class DomainDelta
{ 
  int domain_change; 
public:
  /// This function shouldn't be called directly. This object should be passed to a variables, which will do any "massaging" which 
  /// is required.
  int XXX_get_domain_diff()
{ return domain_change; }

  DomainDelta(int i) : domain_change(i)
{}
};


struct Trigger;
struct Constraint;
struct DynamicTrigger;

#include "constraints/triggers.h"

#include "variables/VarRefType.h"

#include "constraints/constraint.h"

#ifdef DYNAMICTRIGGERS
VARDEF_ASSIGN(bool dynamic_triggers_used, false);
#include "constraints/constraint_dynamic.h"
#endif 

namespace Controller
{
  /// Add a new list of triggers to the queue.
  inline void push_triggers(TriggerRange new_triggers);
#ifdef DYNAMICTRIGGERS
  inline void push_dynamic_triggers(DynamicTrigger* trigs);
#endif
}

#include "trigger_list.h"

#include "variables/booleanvariables.h"
#include "variables/intvar.h"
#include "variables/long_intvar.h"
#include "variables/intboundvar.h"
#include "variables/sparse_intboundvar.h"

#include "variables/variable_neg.h"
#include "variables/variable_switch_neg.h"
#include "variables/variable_stretch.h"
#include "variables/variable_constant.h"
#include "variables/variable_not.h"
#include "variables/variable_shift.h"
#include "variables/iterators.h"

#include "solver2.h"

#include "constraints/constraint_table.h"
#include "constraints/constraint_boundtable.h"


#include "constraints/reify.h"
#include "constraints/reify_true.h"

#include "constraints/constraint_occurrance.h"
#include "constraints/constraint_min.h"
#include "constraints/constraint_lex.h"
#include "constraints/constraint_neq.h"
#include "constraints/constraint_sum.h"
#include "constraints/constraint_less.h"
#include "constraints/constraint_and.h"
#include "constraints/constraint_lightsum.h"
#include "constraints/constraint_fullsum.h"
#include "constraints/constraint_element.h"
#include "constraints/constraint_GACelement.h"
#include "constraints/constraint_equal.h"
#include "constraints/constraint_weightedboolsum.h"
#include "constraints/constraint_unaryequals.h"
#include "constraints/constraint_unaryneq.h"
#include "constraints/constraint_product.h"

#ifdef DYNAMICTRIGGERS
#include "constraints/constraint_GACtable.h"
#include "dynamic_constraints/dynamic_sum.h"
#include "dynamic_constraints/dynamic_element.h"
#endif

#include "constraints/function_defs.hpp"

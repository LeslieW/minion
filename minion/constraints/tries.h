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


#include <numeric>

struct TupleComparator
{
  int significantIndex;
  int arity;
  
  TupleComparator(int i, int a)
  {
    significantIndex = i;
    arity = a; 
  }
  
  // returns tuple1 <= tuple2 under our ordering.
  bool operator()(const vector<int>& tuple1, const vector<int>& tuple2)
  {
    if(tuple1[significantIndex] != tuple2[significantIndex])
	  return tuple1[significantIndex] < tuple2[significantIndex];
    for(int tupleIndex = 0; tupleIndex < arity; tupleIndex++)
    {
      if(tuple1[tupleIndex] != tuple2[tupleIndex])
        return tuple1[tupleIndex] < tuple2[tupleIndex];
    }
	return true;
  }
};


template<typename VarArray>
struct TupleTrie {
  VarArray scope_vars;
  int arity ;
  int significantIndex ;
  int delim ;
  vector<int> dom_size;
  int* levelLengths ;
  int** trie ;
  
  
  /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	Constructor
	Assumes tuples is non-empty and each tuple has same length.
WCase: each level of trie has |tuples| elements.
	Tuples are first sorted lexicographically, then added to trie.
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */
  TupleTrie(const VarArray& _vars,
            const int _significantIndex, const int _delim, 
            vector<vector<int> > tuples,
            const vector<int>& _dom_size) : 
	scope_vars(_vars),
	significantIndex(_significantIndex), 
	delim(_delim),
	dom_size(_dom_size)
  {
	  int componentIndex, trieLevel, tupleIndex ;
	  int noTuples = tuples.size() ;
	  arity = tuples[0].size() ;
	  // Sort tuples for ease of addition to trie
	  sort(tuples.begin(), tuples.end(), TupleComparator(significantIndex, arity));
	  
	  // JAVA was:
	  //      int[][] initTrie = new int[arity][tuples.length*4] ;       //val+2ptr+del
	  
	  int** initTrie = new int*[arity];
	  for(int i=0; i<arity ; i++){
		initTrie[i] = new int[noTuples*4]; //val+2ptr+del
	  }; 
	  
	  // Hope that was ok!
	  
	  // currElement, currIndices. Indexed by trie level.
	  int* currElement = new int[arity] ;
	  int* currIndices = new int[arity] ;
	  levelLengths = new int[arity] ;
	  for (int index = 0; index < arity; index++) {
		currElement[index] = delim ;
		levelLengths[index] = 0 ;
	  }
	  // Iterate over tuples, adding each to the trie.
	  for (tupleIndex = 0; tupleIndex < noTuples; tupleIndex++) {
		vector<int> tuple = tuples[tupleIndex] ;
		// lev 0 = signif idx. No delimiters/back ptrs on level 0
		if (tuple[significantIndex] != currElement[0]) {
		  currElement[0] = tuple[significantIndex] ;
		  initTrie[0][levelLengths[0]++] = tuple[significantIndex] ;
		  if (arity > 1) {
			// Start new block at next level
			if (levelLengths[1] > 0)
			  initTrie[1][levelLengths[1]++] = delim ;
			// point to new block at next level
			initTrie[0][levelLengths[0]++] = levelLengths[1] ;
			// reset currElement at next level
			currElement[1] = delim ;
		  }
		}
		// Now look at rest of tuple
		for (trieLevel = 1; trieLevel < arity; trieLevel++) { 
		  componentIndex = ((trieLevel <= significantIndex) ?  
							trieLevel - 1 : 
							trieLevel) ;
		  // Only modify this level if this is a new prefix
		  if (tuple[componentIndex] != currElement[trieLevel]) {
			currElement[trieLevel] = tuple[componentIndex] ;
			initTrie[trieLevel][levelLengths[trieLevel]++] =
			  tuple[componentIndex] ;
			if (trieLevel < (arity - 1)) {
			  // Start new block at next level
			  if (levelLengths[trieLevel+1] > 0)
				initTrie[trieLevel+1][levelLengths[trieLevel+1]++] = delim ;
			  // point to new block at next level
			  initTrie[trieLevel][levelLengths[trieLevel]++] =
				levelLengths[trieLevel+1] ;
			  // reset currElement at next level
			  currElement[trieLevel+1] = delim ;
			}
			// Point back to parent
			initTrie[trieLevel][levelLengths[trieLevel]++] =
			  levelLengths[trieLevel-1] - ((trieLevel==1) ? 2 : 3) ;
		  } // end of modified prefix test 
		} // end of trieLevel loop
	  } // end of tuple loop
		// Create final, immutable trie.
	  
	  trie = new int*[arity] ;
	  for (trieLevel = 0; trieLevel < arity; trieLevel++) 
	  {
		trie[trieLevel] = new int[levelLengths[trieLevel]] ;
		// JAVA Was: 
		// System.arraycopy(initTrie[trieLevel], 0, trie[trieLevel], 0, trie[trieLevel].size()) ;
		
		for (int i=0; i < levelLengths[trieLevel]; ++i) 
		{
		  trie[trieLevel][i] = initTrie[trieLevel][i];
		}
	  }
	  // COULD BE WRONG BELOW
	  
	  for(int i=0; i < arity; i++)
	  { delete[] (initTrie[i]); };
	  delete[] (initTrie);           
	  // Just incantations, sorry if I have it wrong.    Obviously this is not necessarily 
	  // optimal as we could keep this around for other constructs.  Not to worry.
  }
  
  /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	identifySupportingTuple
	Assumes valToSupport is in domain.
	Returns tuple supporting this value or null.
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */
  int identifySupportingTuple(int valToSupport) {
	int index = 0, trieLevel = 1, val ;
	// find valToSupport, then search for a supporting tuple
	for (index = 0; index < levelLengths[0]; index += 2)
	  if (trie[0][index] == valToSupport)
	    return searchTrie(1, trie[0][index+1]) ;
	return -1 ;
  }
  
  /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	nextSupportingTuple
	Assumes tuple is supporting the ancestor at trieLevel 0.
	index is into last level of trie.
	Find highest lvl where domelem in tuple gone. Search from here.
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */
  int nextSupportingTuple (int valPtr) {
    int highestLevel = arity, highestIndex = 0, index = valPtr ;
	for (int trieLevel = arity-1; trieLevel > 0; trieLevel--) { 
	  if (!inDomain(trie[trieLevel][index], trieLevel)) { 
		highestLevel = trieLevel ; 
		highestIndex = index ;
	  }
	  index = trie[trieLevel][index+((trieLevel == arity-1)? 1:2)] ;
	}
	// still supported
	if (highestLevel == arity) return valPtr ;
	// search for next supporting tuple
	return searchTrie(highestLevel, highestIndex) ;
  }
  
  /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	searchTrie
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */
  int searchTrie(int trieLevel, int index) {
    int val ;
	while (trieLevel > 0) {
	  val = trie[trieLevel][index] ;
      if (inDomain(val, trieLevel)) {
	    // done?
	    if (trieLevel == (arity - 1))
	      return index ;
	    // Look for rest of supporting tuple
		index = trie[trieLevel][index+1] ;
	    trieLevel++ ;
	  }
	  else {
	    // move to next value
	    index += ((trieLevel == (arity-1)? 2:3)) ;
		// if reached end of level, failed.
		if (index == levelLengths[trieLevel]) return -1 ;		
		// if blk end, backtrack. Otherwise next val on this level
		if (trie[trieLevel][index] == delim) {
		  // point to value *after* parent
		  index = trie[trieLevel][index-1]+3 ;
		  trieLevel-- ;
		}
	  }
    } // end of trie search
	return -1 ;  
  }
  
  /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
reconstructTuple
Follow back ptrs up the tree.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */
void reconstructTuple(int*& supportingTuple, int valPtr) {
  int componentIndex ;
  for (int trieLevel = arity-1; trieLevel > 0; trieLevel--) {
	componentIndex = ((trieLevel <= significantIndex) ?  
					  trieLevel - 1 : trieLevel) ;
	supportingTuple[componentIndex] = trie[trieLevel][valPtr] ;
	// get back ptr
	valPtr = trie[trieLevel][valPtr+((trieLevel == arity-1)? 1:2)] ;
  }
  supportingTuple[significantIndex] = trie[0][valPtr] ;
}  

  /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	inDomain
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */
  /*					 
	return true iff val is in domain scope[componentIndex]
	*/
  
private: 
bool inDomain(int val, int trieLevel) {
  int componentIndex = ((trieLevel <= significantIndex) ?  
						trieLevel - 1 : trieLevel) ;
  // This should be the CSP Var?
  return scope_vars[componentIndex].inDomain(val);
}
};       // end of TupleTrie struct


template<typename VarArray>
struct TupleTrieArray { 
  int arity; 
  TupleTrie<VarArray>* tupleTries;
  
  TupleTrie<VarArray>& getTrie(int varIndex) 
  {
    return tupleTries[varIndex];
  };
  
  /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	Constructor
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */
  
  TupleTrieArray(const VarArray& vars,
                 const vector<vector<int> >& tuples
                 ) : 
    arity(vars.size())
  {
     vector<int> dom_size(arity);
	 for(int i = 0; i < arity; ++i)
	 {
	   // getInitial* should be a const function, but fixing constness of all of minion is a major undertaking
	   // I'm not going to get into right now, so for now we'll get rid of the constness. This isn't illegal
	   // C++ or anything, just a bit nasty looking.
	   VarArray& non_const_vars = const_cast<VarArray&>(vars);
	   dom_size[i] = non_const_vars[i].getInitialMax() - non_const_vars[i].getInitialMin() + 1;
	  }
      // create	one trie for each element of scope.
	  tupleTries = (TupleTrie<VarArray>*) malloc(sizeof(TupleTrie<VarArray>) * arity);
	  //new TupleTrie<VarArray>[arity] ; 
	  for (unsigned varIndex = 0; varIndex < arity; varIndex++) 
	  {
		new (tupleTries + varIndex) TupleTrie<VarArray>(vars, varIndex, -1, tuples, dom_size);
		//tupleTries[varIndex] = new TupleTrie<VarArray>(vars, varIndex, -1, tuples, dom_size) ;
	  };
	  //
  }
  
};

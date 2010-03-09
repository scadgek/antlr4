package org.antlr.v4.automata;

import org.antlr.v4.misc.Utils;
import org.antlr.v4.tool.Grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A DFA (converted from a grammar's NFA).
 *  DFAs are used as prediction machine for alternative blocks in all kinds
 *  of recognizers (lexers, parsers, tree walkers).
 */
public class DFA {
	Grammar g;

	/** What's the start state for this DFA? */
    public DFAState startState;

	public int decision;

	/** From what NFAState did we create the DFA? */
	public DecisionState decisionNFAStartState;

	/** A set of all uniquely-numbered DFA states.  Maps hash of DFAState
     *  to the actual DFAState object.  We use this to detect
     *  existing DFA states.  Map<DFAState,DFAState>.  Use Map so
	 *  we can get old state back (Set only allows you to see if it's there).
	 *  Not used during fixed k lookahead as it's a waste to fill it with
	 *  a dup of states array.
     */
    public Map<DFAState, DFAState> uniqueStates = new HashMap<DFAState, DFAState>();

	/** Maps the state number to the actual DFAState.  This contains all
	 *  states, but the states are not unique.  s3 might be same as s1 so
	 *  s3 -> s1 in this table.  This is how cycles occur.  If fixed k,
	 *  then these states will all be unique as states[i] always points
	 *  at state i when no cycles exist.
	 *
	 *  This is managed in parallel with uniqueStates and simply provides
	 *  a way to go from state number to DFAState rather than via a
	 *  hash lookup.
	 */
	//protected List<DFAState> states = new ArrayList<DFAState>();

	/** Each alt in an NFA derived from a grammar must have a DFA state that
     *  predicts it lest the parser not know what to do.  Nondeterminisms can
     *  lead to this situation (assuming no semantic predicates can resolve
     *  the problem) and when for some reason, I cannot compute the lookahead
     *  (which might arise from an error in the algorithm or from
     *  left-recursion etc...).  This list starts out with all alts contained
     *  and then in method doesStateReachAcceptState() I remove the alts I
     *  know to be uniquely predicted.
     */
    public List<Integer> unreachableAlts;

	public int nAlts = 0;

	/** We only want one accept state per predicted alt; track here */
	public DFAState[] altToAcceptState;	
	
	/** Unique state numbers per DFA */
	int stateCounter = 0;

	public DFA(Grammar g, DecisionState startState) {
		this.g = g;
		this.decisionNFAStartState = startState;
		nAlts = startState.getNumberOfTransitions();
		decision = startState.decision;
		unreachableAlts = new ArrayList<Integer>();
		for (int i = 1; i <= nAlts; i++) {
			unreachableAlts.add(Utils.integer(i));
		}
		altToAcceptState = new DFAState[nAlts+1];
	}

	/** Add a new DFA state to this DFA (doesn't check if already present). */
	public void addState(DFAState d) {
		uniqueStates.put(d,d);
		d.stateNumber = stateCounter++;
	}

	public void defineAcceptState(int alt, DFAState acceptState) {
		altToAcceptState[alt] = acceptState;
	}
	
	public DFAState newState() {
		DFAState n = new DFAState(this);
//		states.setSize(n.stateNumber+1);
//		states.set(n.stateNumber, n); // track state num to state
		return n;
	}

	public String toString() {
		if ( startState==null ) return "";
		DFASerializer serializer = new DFASerializer(g, startState);
		return serializer.toString();
	}	

}

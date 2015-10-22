package org.ggp.base.util.propnet.state;

import org.apache.lucene.util.OpenBitSet;

public class ExternalPropnetState {

	/** Currently set values of the BASE propositions. */
	private OpenBitSet currentState;

	/** Currently set values of the TRANSITIONS. */
	private OpenBitSet nextState;

	/** Currently set values for the INPUTS.
	 * One input for each role will be set to true.
	 */
	private OpenBitSet currentJointMove;

	/** Currently set values of the AND and OR gates.
	 *
	 * Each integer in the array corresponds to a gate in the propnet.
	 * It keeps track of the number of true inputs of the gate so that the sign bit
	 * of the integer also represents the truth value of the gate.
	 * It's initial value is set so that the integer will overflow when the truth value
	 * of the gate becomes true, so that it will correspond to the sign bit of the integer
	 * being set to 1.
	 */
	private int[] andOrGatesValues;

	/** Index that the first LEGAL proposition of each role has in the OpenbitSet 'otherComponents'
	 *
	 * Since they will be grouped by role, knowing the first index also tells us where all other
	 * legal propositions for the role are.
	 */
	private int[] firstLegalPropositionIndex;

	private int[] firstGoalPropositionIndex;


	/** Currently set values of all the components not yet included in the previous parameters.
	 *
	 * Note that the first bit corresponds to the terminal state.
	 * After that there will be the values of the legal propositions, ordered by role, then the
	 * ones of the goal propositions, ordered by role, then the ones of all other propositions
	 * and in the end the ones of the NOT components.
	 */
	private OpenBitSet otherComponents;


	public OpenBitSet getCurrentState(){
		return this.currentState;
	}

	public OpenBitSet getCurrentJointMove(){
		return this.currentJointMove;
	}



}

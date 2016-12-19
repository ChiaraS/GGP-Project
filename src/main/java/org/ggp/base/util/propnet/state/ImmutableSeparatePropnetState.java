package org.ggp.base.util.propnet.state;

import java.io.Serializable;

import org.apache.lucene.util.OpenBitSet;

public class ImmutableSeparatePropnetState implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** Currently set values of the BASE propositions. */
	private OpenBitSet currentState;

	/** Currently set values of the TRANSITIONS. */
	private OpenBitSet nextState;

	/** Currently set values for the INPUTS.
	 * One input for each role will be set to true.
	 */
	private OpenBitSet currentJointMove;

	/**
	 *  List containing for each role the index that its first goal proposition
	 *  has in the otherComponnets array. This array contains one element more
	 *  that gives the first index of a non-goal proposition in the otherComponents
	 *  arrray.
	 */
	private int[] firstGoalIndices;

	/**
	 *  List containing for each role the index that its first legal proposition
	 *  has in the otherComponnets array. This array contains one element more
	 *  that gives the first index of a non-legal proposition in the otherComponents
	 *  arrray.
	 */
	private int[] firstLegalIndices;

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

	/** Currently set values of all the components not yet included in the previous parameters.
	 *
	 * Note that the first bit corresponds to the terminal state.
	 * After that there will be the values of the legal propositions, ordered by role, then the
	 * ones of the goal propositions, ordered by role, then the ones of all other propositions
	 * and in the end the ones of the NOT components.
	 */
	private OpenBitSet otherComponents;

	/***************************************** Constructor ******************************************/

	public ImmutableSeparatePropnetState(OpenBitSet currentState, OpenBitSet nextState, OpenBitSet currentJointMove,
			int[] firstGoalIndices, int[] firstLegalIndices, int[] andOrGatesValues, OpenBitSet otherComponents){
		this.currentState = currentState;
		this.nextState = nextState;
		this.currentJointMove = currentJointMove;
		this.firstGoalIndices = firstGoalIndices;
		this.firstLegalIndices = firstLegalIndices;
		this.andOrGatesValues = andOrGatesValues;
		this.otherComponents = otherComponents;
	}

	/***************** Getters for part of the state values (used by the state machine) ******************/

	public OpenBitSet getCurrentState(){
		return this.currentState;
	}

	public OpenBitSet getNextState(){
		return this.nextState;
	}

	public OpenBitSet getCurrentJointMove(){
		return this.currentJointMove;
	}

	public int[] getFirstGoalIndices(){
		return this.firstGoalIndices;
	}

	public int[] getFirstLegalIndices(){
		return this.firstLegalIndices;
	}

	/*
	public OpenBitSet[] getGoals(){
		return this.goals;
	}

	public OpenBitSet getGoals(int role){
		return this.goals[role];
	}

	public OpenBitSet[] getLegals(){
		return this.legals;
	}

	public OpenBitSet getLegals(int role){
		return this.legals[role];
	}

	*/

	public OpenBitSet getOtherComponents(){
		return this.otherComponents;
	}

	public boolean isTerminal(){
		return this.otherComponents.fastGet(0);
	}

	/******************* Methods to change single components values (used by components) ********************/

	public void flipBaseValue(int index){
		this.currentState.fastFlip(index);
	}

	public void flipTransitionValue(int index){
		this.nextState.fastFlip(index);
	}

	public void flipInputValue(int index){
		this.currentJointMove.fastFlip(index);
	}

	public void flipOtherValue(int index){
		this.otherComponents.fastFlip(index);
	}

	public void incrementTrueInputs(int index, int increment){
		this.andOrGatesValues[index] += increment;
	}

	public void incrementTrueInputs(int index){
		this.andOrGatesValues[index]++;
	}

	public void decrementTrueInputs(int index){
		this.andOrGatesValues[index]--;
	}

	public void resetTrueInputsAnd(int index, int numInputs){
		this.andOrGatesValues[index] = Integer.MAX_VALUE - numInputs + 1;
	}

	public void resetTrueInputsOr(int index){
		this.andOrGatesValues[index] = Integer.MAX_VALUE;
	}

	/******************** Methods to get single component values (used by components) ********************/

	public boolean getBaseValue(int index){
		return this.currentState.fastGet(index);
	}

	public boolean getTransitionValue(int index){
		return this.nextState.fastGet(index);
	}

	public boolean getInputValue(int index){
		return this.currentJointMove.fastGet(index);
	}

	public boolean getOtherValue(int index){
		return this.otherComponents.fastGet(index);
	}

	public boolean getGateValue(int index){
		return this.andOrGatesValues[index] < 0;
	}

	/******************************************* Clone method ********************************************/

	/**
	 * This method is used to clone this external state for every new thread
	 * that wants to use the propnet with this state.
	 */
	@Override
	public ImmutableSeparatePropnetState clone(){
		return new ImmutableSeparatePropnetState(this.currentState.clone(), this.nextState.clone(), this.currentJointMove.clone(), this.firstGoalIndices, this.firstLegalIndices, this.andOrGatesValues.clone(), this.otherComponents.clone());
	}

}

package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters;

public abstract class TunableParameter {

	/**
	 * Indicates in which order the parameters should be tuned, if an order is required by the tuning method.
	 * If the tuning method doesn't require a specific order this value might be ignored by it. If the tuning
	 * method requires a specific order but this value is not specified then no guarantee is given on the order
	 * that will be used by the tuner (nor on the correctness of its performance). Also, this value must be
	 * unique for each parameter being tuned and the values must be specified in order starting from 0 up, without
	 * skipping any intermediate value.
	 */
	private int tuningOrderIndex;

	public TunableParameter(){
		this(-1);
	}

	public TunableParameter(int tuningOrderIndex){

		this.tuningOrderIndex = tuningOrderIndex;

	}

	public abstract int getPossibleValuesLength();

	public abstract void setMyRoleNewValue(int myRoleIndex, int newValueIndex);

	public abstract void setAllRolesNewValues(int[] newValuesIndices);

	public String getParameters(String indentation) {
		return indentation + "TUNING_ORDER_INDEX = " + this.tuningOrderIndex;
	}

	public int getTuningOrderIndex(){
		return this.tuningOrderIndex;
	}

}

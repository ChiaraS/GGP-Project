package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters;

public abstract class TunableParameter {

	private String name;

	/**
	 * Indicates in which order the parameters should be tuned, if an order is required by the tuning method.
	 * If the tuning method doesn't require a specific order this value might be ignored by it. If the tuning
	 * method requires a specific order but this value is not specified then no guarantee is given on the order
	 * that will be used by the tuner (nor on the correctness of its performance). Also, this value must be
	 * unique for each parameter being tuned and the values must be specified in order starting from 0 up, without
	 * skipping any intermediate value.
	 */
	private int tuningOrderIndex;

	/**
	 * This array specifies a penalty for each of the possible values of the tunable parameter. The greater the
	 * penalty the worse the value is expected to perform. When selecting the next parameter to be evaluated
	 * this penalty will be used to compute a bias that will reward more parameters expected to perform well
	 * and reward less parameters expected to perform bad.
	 *
	 * Example on how to compute the penalty value: perform preliminary experiments where you test the different
	 * possible values of the parameter singularly on a certain set of games.
	 * Suppose that V={v_1,...,v_n} is the set of possible values that you tested for this parameter and that
	 * W={w_1,...,w_n} is a set where each w_i is the win percentage (i.e. a value in the interval [0, 100]) of
	 * the player that was using the value v_i for the considered parameter.
	 * You can assign a penalty of 0 to the value v_i that obtained the highest win percentage and a penalty of
	 * w_i-w_j to each other value v_j. When evaluating which parameter to select next, a bias will be computed
	 * that will have an higher value the lower the penalty is.
	 */
	private double[] possibleValuesPenalty;

	public TunableParameter(String name, double[] possibleValuesPenalty, int tuningOrderIndex){

		this.name = name;

		this.possibleValuesPenalty =  possibleValuesPenalty;

		this.tuningOrderIndex = tuningOrderIndex;

	}

	/**
	 * Get the index of the current value for each role.
	 *
	 * @return an array, where each entry corresponds to a role and is the index in the list of possible
	 * values of the value that is currently set for the role.
	 */
	public abstract int[] getCurrentValuesIndices();

	public abstract int getNumPossibleValues();

	public abstract String[] getPossibleValues();

	/**
	 * Returns the value at the given index as a double.
	 * @param valueIndex
	 * @return
	 */
	public abstract double getPossibleValue(int valueIndex);

	public abstract void setMyRoleNewValue(int myRoleIndex, int newValueIndex);

	public abstract void setAllRolesNewValues(int[] newValuesIndices);

	public String getParameters(String indentation) {

		String s;

		if(this.possibleValuesPenalty != null){
			s = "[ ";
			for(int i = 0; i < this.possibleValuesPenalty.length; i++){
				s += this.possibleValuesPenalty[i] + " ";
			}
			s += "]";
		}else{
			s = "null";
		}
		return indentation + "NAME = " + this.name +
				indentation + "POSSIBLE_VALUES_PENALTY = " + s +
				indentation + "TUNING_ORDER_INDEX = " + this.tuningOrderIndex;
	}

	public double[] getPossibleValuesPenalty(){
		return this.possibleValuesPenalty;
	}

	public int getTuningOrderIndex(){
		return this.tuningOrderIndex;
	}

	public String getName(){
		return this.name;
	}

}

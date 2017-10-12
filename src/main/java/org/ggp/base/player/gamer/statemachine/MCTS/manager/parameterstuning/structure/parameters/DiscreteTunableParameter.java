package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters;

/**
 * TODO: should this become a generic class?
 * @author C.Sironi
 *
 */
public class DiscreteTunableParameter extends TunableParameter {

	/**
	 * If we are tuning this parameter for at least one role, these are all the possible values
	 * that the parameter can assume.
	 */
	private double[] possibleValues;

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

	public DiscreteTunableParameter(String name, double fixedValue, int tuningOrderIndex, double[] possibleValues, double[] possibleValuesPenalty) {

		super(name, fixedValue, tuningOrderIndex);

		this.possibleValues = possibleValues;

		this.possibleValuesPenalty =  possibleValuesPenalty;

	}

	public int getNumPossibleValues(){

		if(this.possibleValues == null){
			return 0;
		}else{
			return this.possibleValues.length;
		}
	}

	public void setMyRoleNewValue(int myRoleIndex, int newValueIndex) {
		// We are tuning only the parameter of myRole
		this.currentValues[myRoleIndex] = this.possibleValues[newValueIndex];
	}

	public void setAllRolesNewValues(int[] newValuesIndices) {
		// We are tuning for all roles
		for(int i = 0; i < newValuesIndices.length; i++){
			this.currentValues[i] = this.possibleValues[newValuesIndices[i]];
		}
	}

	@Override
	public String getParameters(String indentation) {

		String superParams = super.getParameters(indentation);

		String params = "";

		if(this.possibleValues != null){
			String possibleValuesString = "[ ";

			for(int i = 0; i < this.possibleValues.length; i++){
				if(this.possibleValues[i] == Double.MAX_VALUE){
					possibleValuesString += "inf ";
				}else{
					possibleValuesString += this.possibleValues[i] + " ";
				}

			}

			possibleValuesString += "]";

			params += indentation + "POSSIBLE_VALUES = " + possibleValuesString;
		}else{
			params += indentation + "POSSIBLE_VALUES = null";
		}

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

		params += (indentation + "POSSIBLE_VALUES_PENALTY = " + s);

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

	public String[] getPossibleValues() {
		String[] values = new String[this.possibleValues.length];
		for(int i = 0; i < this.possibleValues.length; i++){
			if(this.possibleValues[i] == Double.MAX_VALUE){
				values[i] = "inf";
			}else{
				values[i] = ""+this.possibleValues[i];
			}
		}
		return values;
	}

	/**
	 * Returns the value at the given index as a double.
	 * @param valueIndex
	 * @return
	 */
	public double getPossibleValue(int valueIndex) {
		return this.possibleValues[valueIndex];
	}

	/**
	 * Get the index of the current value for each role.
	 *
	 * @return an array, where each entry corresponds to a role and is the index in the list of possible
	 * values of the value that is currently set for the role.
	 */
	public int[] getCurrentValuesIndices() {
		int[] currentValuesIndices = new int[this.currentValues.length];
		for(int roleIndex = 0; roleIndex < this.currentValues.length; roleIndex++){
			currentValuesIndices[roleIndex] = 0;
			while(currentValuesIndices[roleIndex] < this.possibleValues.length && this.possibleValues[currentValuesIndices[roleIndex]] != this.currentValues[roleIndex]){
				currentValuesIndices[roleIndex]++;
			}
		}
		return currentValuesIndices;
	}

	public double[] getPossibleValuesPenalty(){
		return this.possibleValuesPenalty;
	}

}

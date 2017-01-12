package org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution;

/**
 * TODO: should this become a generic class?
 * @author C.Sironi
 *
 */
public class DoubleTunableParameter extends TunableParameter {

	/**
	 * Value of the parameter for the roles not being tuned.
	 * It's also the values used to initialize each value at the beginning of each new game.
	 */
	private double fixedValue;

	/**
	 * If we are tuning this parameter for at least one role, these are all the possible values
	 * that the parameter can assume.
	 */
	private double[] possibleValues;

	/**
	 * For each role the current value being set.
	 */
	private double[] currentValues;

	public DoubleTunableParameter(double fixedValue) {

		this(fixedValue, null);

	}

	public DoubleTunableParameter(double fixedValue, double[] possibleValues) {

		this.fixedValue = fixedValue;

		this.possibleValues = possibleValues;

		this.currentValues = null;

	}

	public void clearParameter(){
		this.currentValues = null;
	}

	public void setUpParameter(int numRoles){
		this.currentValues = new double[numRoles];

		for(int i = 0; i < this.currentValues.length; i++){
			this.currentValues[i] = this.fixedValue;
		}
	}

	public double getValuePerRole(int roleIndex){
		return this.currentValues[roleIndex];
	}

	@Override
	public int getPossibleValuesLength(){

		if(this.possibleValues == null){
			return 0;
		}else{
			return this.possibleValues.length;
		}
	}

	@Override
	public void setMyRoleNewValue(int myRoleIndex, int newValueIndex) {
		// We are tuning only the parameter of myRole
		this.currentValues[myRoleIndex] = this.possibleValues[newValueIndex];
	}

	@Override
	public void setAllRolesNewValues(int[] newValuesIndices) {
		// We are tuning for all roles
		for(int i = 0; i < newValuesIndices.length; i++){
			this.currentValues[i] = this.possibleValues[newValuesIndices[i]];
		}
	}

	@Override
	public String getParameters(String indentation) {

		String params = indentation + "FIXED_VALUE = " + this.fixedValue;

		if(this.possibleValues != null){
			String possibleValuesString = "[ ";

			for(int i = 0; i < this.possibleValues.length; i++){

				possibleValuesString += this.possibleValues[i] + " ";

			}

			possibleValuesString += "]";

			params += indentation + "POSSIBLE_VALUES = " + possibleValuesString;
		}else{
			params += indentation + "POSSIBLE_VALUES = null";
		}

		if(this.currentValues != null){
			String currentValuesString = "[ ";

			for(int i = 0; i < this.currentValues.length; i++){

				currentValuesString += this.currentValues[i] + " ";

			}

			currentValuesString += "]";

			params += indentation + "current_values = " + currentValuesString;
		}else{
			params += indentation + "current_values = null";
		}

		return params;

	}

}

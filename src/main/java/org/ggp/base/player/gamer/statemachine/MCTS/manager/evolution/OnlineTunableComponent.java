package org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution;

/**
 * TODO: all subclasses have a lot of similar code. Make it general and extrapolate it.
 * @author C.Sironi
 *
 */
public abstract interface OnlineTunableComponent {

	public String printOnlineTunableComponent(String indentation);

	public void setNewValues(double[] newValues);

	public void setNewValuesFromIndices(int[] newValuesIndices);

	/**
	 *
	 * @return the lengths of the list of possible values for each parameter that this component is tuning.
	 * Each entry in the array corresponds to a parameter and specifies for that parameter the number of all
	 * the possible values that the parameter can assume.
	 */
	public int[] getPossibleValuesLengths();

}

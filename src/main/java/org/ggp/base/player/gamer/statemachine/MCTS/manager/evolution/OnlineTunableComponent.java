package org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution;

public abstract interface OnlineTunableComponent {

	public String printOnlineTunableComponent(String indentation);

	public void setNewValues(double[] newValues);

	public void setNewValuesFromIndices(int[] newValuesIndices);

	public double[] getPossibleValues();

}

package org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution;

public abstract interface OnlineTunableComponent {

	public String printOnlineTunableComponent();

	public void setNewValues(double[] newValues);

	public double[] getPossibleValues();

}

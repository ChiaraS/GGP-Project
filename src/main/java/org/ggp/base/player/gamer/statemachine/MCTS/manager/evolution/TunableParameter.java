package org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution;

public abstract class TunableParameter {

	public abstract int getPossibleValuesLength();

	public abstract void setMyRoleNewValue(int myRoleIndex, int newValueIndex);

	public abstract void setAllRolesNewValues(int[] newValuesIndices);

	public abstract String getParameters(String indentation);

}

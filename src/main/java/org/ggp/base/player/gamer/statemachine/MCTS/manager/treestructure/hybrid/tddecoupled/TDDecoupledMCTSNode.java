package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSNode;

public class TDDecoupledMCTSNode extends DecoupledMCTSNode {

	/**
	 * List that contains for each role the minimum state-action value estimate seen so far in this state.
	 */
	private double[] minStateActionValue;

	/**
	 * List that contains for each role the maximum state-action value estimate seen so far in this state.
	 */
	private double[] maxStateActionValue;

	public TDDecoupledMCTSNode(DecoupledMCTSMoveStats[][] movesStats, int[] goals, boolean terminal, int numRoles){
		super(movesStats, goals, terminal);

		this.minStateActionValue = new double[numRoles];
		this.maxStateActionValue = new double[numRoles];

		for(int i = 0; i < numRoles; i++){
			this.minStateActionValue[i] = Double.MAX_VALUE;
			this.maxStateActionValue[i] = -Double.MAX_VALUE;
		}

	}

	public double getMinStateActionValueForRole(int roleIndex){
		return this.minStateActionValue[roleIndex];
	}

	public double getMaxStateActionValueForRole(int roleIndex){
		return this.maxStateActionValue[roleIndex];
	}

	public void setMinStateActionValueForRole(double newMinStateActionValue, int roleIndex){
		this.minStateActionValue[roleIndex] = newMinStateActionValue;
	}

	public void setMaxStateActionValueForRole(double newMaxStateActionValue, int roleIndex){
		this.maxStateActionValue[roleIndex] = newMaxStateActionValue;
	}

}

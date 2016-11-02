package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSNode;

public class TDDecoupledMCTSNode extends DecoupledMCTSNode {

	private double maxStateActionValue;

	private double minStateActionValue;

	public TDDecoupledMCTSNode(DecoupledMCTSMoveStats[][] movesStats, int[] goals, boolean terminal){
		super(movesStats, goals, terminal);

		this.maxStateActionValue = -Double.MAX_VALUE;
		this.minStateActionValue = Double.MAX_VALUE;

	}

	public double getMaxStateActionValue(){
		return this.maxStateActionValue;
	}

	public double getMinStateActionValue(){
		return this.minStateActionValue;
	}

	public void setMaxStateActionValue(double newMaxStateActionValue){
		this.maxStateActionValue = newMaxStateActionValue;
	}

	public void setMinStateActionValue(double newMinStateActionValue){
		this.minStateActionValue = newMinStateActionValue;
	}

}

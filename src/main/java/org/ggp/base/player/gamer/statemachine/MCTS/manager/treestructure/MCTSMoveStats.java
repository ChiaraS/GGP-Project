package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class MCTSMoveStats extends MoveStats{

	private double uct;

	public MCTSMoveStats() {
		super();
		this.uct = 0.0;
	}

	public MCTSMoveStats(long visits, long scoreSum, double uct) {
		super(visits, scoreSum);
		this.uct = 0.0;
	}

	public double getUct() {
		return this.uct;
	}

	public void setUct(double uct) {
		this.uct = uct;
	}

	@Override
	public String toString(){
		return super.toString() + ", UCT(" + this.uct + ")";
	}


}

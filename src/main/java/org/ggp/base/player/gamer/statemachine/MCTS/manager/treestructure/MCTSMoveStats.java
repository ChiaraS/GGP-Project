package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MCSMoveStats;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MCTSMoveStats extends MCSMoveStats{

	private double uct;

	public MCTSMoveStats(InternalPropnetMove theMove) {
		super(theMove);
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

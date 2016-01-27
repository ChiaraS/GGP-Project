package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MCSMove;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMove;

public class MCTSMove extends MCSMove{

	private double uct;

	public MCTSMove(InternalPropnetMove theMove) {
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

package org.ggp.base.player.gamer.statemachine.MCS.manager.prover;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public class ProverCompleteMoveStats extends MoveStats{

	protected ExplicitMove theMove;

	public ProverCompleteMoveStats(ExplicitMove theMove){
		super();
		this.theMove = theMove;
	}

	public ProverCompleteMoveStats(int visits, double scoreSum, ExplicitMove theMove){
		super(visits, scoreSum);
		this.theMove = theMove;
	}

	public ExplicitMove getTheMove() {
		return this.theMove;
	}

	@Override
	public String toString(){

		return "MOVE(" + theMove.toString() + "), " + super.toString();
	}

}

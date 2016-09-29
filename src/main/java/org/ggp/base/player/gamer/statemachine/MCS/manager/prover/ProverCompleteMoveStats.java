package org.ggp.base.player.gamer.statemachine.MCS.manager.prover;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.Move;

public class ProverCompleteMoveStats extends MoveStats{

	protected Move theMove;

	public ProverCompleteMoveStats(Move theMove){
		super();
		this.theMove = theMove;
	}

	public ProverCompleteMoveStats(int visits, double scoreSum, Move theMove){
		super(visits, scoreSum);
		this.theMove = theMove;
	}

	public Move getTheMove() {
		return this.theMove;
	}

	@Override
	public String toString(){

		return "MOVE(" + theMove.toString() + "), " + super.toString();
	}

}

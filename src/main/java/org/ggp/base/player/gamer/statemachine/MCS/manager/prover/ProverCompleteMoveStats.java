package org.ggp.base.player.gamer.statemachine.MCS.manager.prover;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;

public class ProverCompleteMoveStats extends MoveStats{

	protected ProverMove theMove;

	public ProverCompleteMoveStats(ProverMove theMove){
		super();
		this.theMove = theMove;
	}

	public ProverCompleteMoveStats(int visits, double scoreSum, ProverMove theMove){
		super(visits, scoreSum);
		this.theMove = theMove;
	}

	public ProverMove getTheMove() {
		return this.theMove;
	}

	@Override
	public String toString(){

		return "MOVE(" + theMove.toString() + "), " + super.toString();
	}

}

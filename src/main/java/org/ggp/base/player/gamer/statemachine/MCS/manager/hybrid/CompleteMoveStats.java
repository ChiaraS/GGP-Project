package org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.Move;

public class CompleteMoveStats extends MoveStats {

	protected Move theMove;

	public CompleteMoveStats(Move theMove){
		super();
		this.theMove = theMove;
	}

	public CompleteMoveStats(int visits, double scoreSum, Move theMove){
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

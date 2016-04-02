package org.ggp.base.player.gamer.statemachine.MCS.manager.propnet;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class CompleteMoveStats extends MoveStats{

	protected InternalPropnetMove theMove;

	public CompleteMoveStats(InternalPropnetMove theMove){
		super();
		this.theMove = theMove;
	}

	public CompleteMoveStats(int visits, int scoreSum, InternalPropnetMove theMove){
		super(visits, scoreSum);
		this.theMove = theMove;
	}

	public InternalPropnetMove getTheMove() {
		return this.theMove;
	}

	@Override
	public String toString(){
		return "MOVE(" + theMove.toString() + "), " + super.toString();
	}

}

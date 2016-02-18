package org.ggp.base.player.gamer.statemachine.MCS.manager;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MCSMoveStats extends MoveStats{

	protected InternalPropnetMove theMove;

	public MCSMoveStats(InternalPropnetMove theMove){
		super();
		this.theMove = theMove;
	}

	public InternalPropnetMove getTheMove() {
		return this.theMove;
	}

	@Override
	public String toString(){
		return "Move(" + theMove.toString() + "), " + super.toString();
	}

}

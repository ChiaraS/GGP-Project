package org.ggp.base.player.gamer.statemachine.MCS.manager.propnet;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class PnCompleteMoveStats extends MoveStats{

	protected CompactMove theMove;

	public PnCompleteMoveStats(CompactMove theMove){
		super();
		this.theMove = theMove;
	}

	public PnCompleteMoveStats(int visits, double scoreSum, CompactMove theMove){
		super(visits, scoreSum);
		this.theMove = theMove;
	}

	public CompactMove getTheMove() {
		return this.theMove;
	}

	@Override
	public String toString(){
		return "MOVE(" + theMove.toString() + "), " + super.toString();
	}

}

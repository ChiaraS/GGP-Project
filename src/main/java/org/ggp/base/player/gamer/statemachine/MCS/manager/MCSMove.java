package org.ggp.base.player.gamer.statemachine.MCS.manager;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MCSMove {

	protected InternalPropnetMove theMove;

	protected long visits;

	protected long scoreSum;

	public MCSMove(InternalPropnetMove theMove) {
		this.theMove = theMove;
		this.visits = 0L;
		this.scoreSum = 0L;
	}

	public InternalPropnetMove getTheMove() {
		return this.theMove;
	}

	public long getVisits() {
		return this.visits;
	}

	public void incrementVisits() {
		this.visits++;
	}

	public long getScoreSum() {
		return this.scoreSum;
	}

	public void incrementScoreSum(long newScore) {
		this.scoreSum += newScore;
	}

	@Override
	public String toString(){
		return "Move(" + theMove.toString() + "), Visits(" + this.visits + "), ScoreSum(" + this.scoreSum + ")";
	}

}

package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MCTSMove {

	private InternalPropnetMove theMove;

	private long visits;

	private long scoreSum;

	private double uct;

	public MCTSMove(InternalPropnetMove theMove) {
		this.theMove = theMove;
		this.visits = 0L;
		this.scoreSum = 0L;
		this.uct = 0.0;
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

	public double getUct() {
		return this.uct;
	}

	public void setUct(double uct) {
		this.uct = uct;
	}

	@Override
	public String toString(){
		return "[Move(" + theMove.toString() + "), Visits(" + this.visits + "), ScoreSum(" + this.scoreSum + "), UCT(" + this.uct + ")]";
	}



}

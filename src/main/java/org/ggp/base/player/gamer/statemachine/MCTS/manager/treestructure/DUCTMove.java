package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class DUCTMove {

	private InternalPropnetMove theMove;

	private long visits;

	private double scoreSum;

	private double uct;

	public DUCTMove(InternalPropnetMove theMove) {
		this.theMove = theMove;
		this.visits = 0L;
		this.scoreSum = 0.0;
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

	public double getScoreSum() {
		return this.scoreSum;
	}

	public void incrementScoreSum(double newScore) {
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

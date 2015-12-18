package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class DUCTMove {

	private InternalPropnetMove theMove;

	private int visits;

	private int scoreSum;

	private double uct;

	public DUCTMove(InternalPropnetMove theMove) {
		this.theMove = theMove;
		this.visits = 0;
		this.scoreSum = 0;
		this.uct = 0.0;
	}

	public InternalPropnetMove getTheMove() {
		return this.theMove;
	}

	public int getVisits() {
		return this.visits;
	}

	public void incrementVisits() {
		this.visits++;
	}

	public int getScoreSum() {
		return this.scoreSum;
	}

	public void incrementScoreSum(int newScore) {
		this.scoreSum += newScore;
	}

	public double getUct() {
		return this.uct;
	}

	public void setUct(double uct) {
		this.uct = uct;
	}




}

/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCS.manager;


/**
 * @author C.Sironi
 *
 */
public class MoveStats{

	protected int visits;

	protected int scoreSum;

	public MoveStats() {
		this.visits = 0;
		this.scoreSum = 0;
	}

	public MoveStats(int visits, int scoreSum) {
		this.visits = visits;
		this.scoreSum = scoreSum;
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

	@Override
	public String toString(){
		return "VISITS(" + this.visits + "), SCORE_SUM(" + this.scoreSum + ")";
	}

}

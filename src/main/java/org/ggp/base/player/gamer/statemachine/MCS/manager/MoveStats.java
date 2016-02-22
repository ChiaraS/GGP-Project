/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCS.manager;


/**
 * @author C.Sironi
 *
 */
public abstract class MoveStats{

	protected long visits;

	protected long scoreSum;

	public MoveStats() {
		this.visits = 0L;
		this.scoreSum = 0L;
	}

	public MoveStats(long visits, long scoreSum) {
		this.visits = visits;
		this.scoreSum = scoreSum;
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
		return "Visits(" + this.visits + "), ScoreSum(" + this.scoreSum + ")";
	}

}

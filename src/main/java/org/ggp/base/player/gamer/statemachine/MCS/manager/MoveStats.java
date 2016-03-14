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

	/**
	 * Note: this method rounds the decayed visits to the closest integer.
	 * To deal with this the scoreSum is re-computed as equal to the average multiplied
	 * by the rounded number of visits, instead of simply decaying the scoreSum with the
	 * factor as well. This however is still not 100% precise, as the new scoreSum might
	 * as well not be a perfect integer and thus it is also rounded to the closest integer.
	 *
	 * Also note that if the number of visits decays to 0, also the scoreSum will be reset to 0.
	 * => REMEMBER TO CHECK IF VISITS == 0 WHENEVER YOU USE THESE STATS!
	 *
	 * @param factor how much of the original values we want to keep (e.g. factor=0,2 means that
	 * the new value will be (approximately) 0,2 times the old value).
	 */
	public void decreaseByFactor(double factor){
		double avg = ((double) this.scoreSum)/((double)this.visits);
		this.visits = (int) Math.round(((double)this.visits)*factor);
		this.scoreSum = (int) Math.round(((double)this.visits)*avg);
	}

	@Override
	public String toString(){
		return "VISITS(" + this.visits + "), SCORE_SUM(" + this.scoreSum + ")";
	}

}

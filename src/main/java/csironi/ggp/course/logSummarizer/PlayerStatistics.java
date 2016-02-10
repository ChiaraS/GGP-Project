/**
 *
 */
package csironi.ggp.course.logSummarizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author C.Sironi
 *
 */
public class PlayerStatistics {

	/**
	 * List containing the score for all matches of the player.
	 */
	private List<Integer> scores;

	/**
	 * List containing the points for all matches of the player.
	 * (0 if some other player got a higher score, 1/(#players that got highest score) if the player is one of them).
	 */
	private List<Double> points;

	/**
	 * Average of the scores.
	 */
	private int scoreSum;

	/**
	 * Average of the points.
	 */
	private double pointsSum;

	/**
	 * Maximum score ever obtained by the player.
	 */
	private int maxScore;

	/**
	 * Minimum score ever obtained by the player.
	 */
	private int minScore;

	/**
	 * Maximum points ever obtained by the player.
	 */
	private double maxPoints;

	/**
	 * Minimum points ever obtained by the player.
	 */
	private double minPoints;


	/**
	 *
	 */
	public PlayerStatistics() {
		this.scores = new ArrayList<Integer>();
		this.points = new ArrayList<Double>();
		this.scoreSum = 0;
		this.pointsSum = 0;
		this.maxScore = Integer.MIN_VALUE;
		this.minScore = Integer.MAX_VALUE;
		this.maxPoints = -1.0; // Points can only take a value between 0 and 1
		this.minPoints = 2.0;
	}

	public void addScore(int score){
		this.scores.add(score);
		this.scoreSum += score;
		if(score > this.maxScore){
			this.maxScore = score;
		}
		if(score < this.minScore){
			this.minScore = score;
		}
	}

	public void addPoints(double points){
		this.points.add(points);
		this.pointsSum += points;
		if(points > this.maxPoints){
			this.maxPoints = points;
		}
		if(points < this.minPoints){
			this.minPoints = points;
		}
	}

	public List<Integer> getScores(){
		return this.scores;
	}

	public List<Double> getPoints(){
		return this.points;
	}

	public double getAvgScore(){
		if(this.scores.isEmpty()){
			return -1;
		}
		return ((double)this.scoreSum)/((double) this.scores.size());
	}

	public double getAvgPoints(){
		if(this.points.isEmpty()){
			return -1;
		}
		return ((double)this.pointsSum)/((double) this.points.size());
	}

	public int getMaxScore(){
		return this.maxScore;
	}

	public int getMinScore(){
		return this.minScore;
	}

	public double getMaxPoints(){
		return this.maxPoints;
	}

	public double getMinPoints(){
		return this.minPoints;
	}

	public double getScoresStandardDeviation(){
		if(this.scores.isEmpty()){
			return -1;
		}
		double squaredSum = 0.0;
		double avgScore = this.getAvgScore();
		for(Integer score : this.scores){
			double difference = ((double) score.intValue()) - avgScore;
			squaredSum += (difference * difference);
		}
		return Math.sqrt(squaredSum/(this.scores.size()-1));
	}

	public double getPointsStandardDeviation(){
		if(this.points.isEmpty()){
			return -1;
		}
		double squaredSum = 0.0;
		double avgPoints = this.getAvgPoints();
		for(Double point : this.points){
			double difference = point - avgPoints;
			squaredSum += (difference * difference);
		}
		return Math.sqrt(squaredSum/(this.points.size()-1));
	}

	public double getScoresSEM(){
		double standardDev = this.getScoresStandardDeviation();
		if(standardDev == -1){
			return -1;
		}
		return (standardDev / Math.sqrt(this.scores.size()));
	}

	public double getPointsSEM(){
		double standardDev = this.getPointsStandardDeviation();
		if(standardDev == -1){
			return -1;
		}
		return (standardDev / Math.sqrt(this.points.size()));
	}

}

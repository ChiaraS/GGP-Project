/**
 *
 */
package csironi.ggp.course.statsSummarizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author C.Sironi
 *
 */
public class PlayerStatistics {

	/**
	 * List containing the combination for each inserted score value of the player
	 */
	private List<String> scoresCombinations;

	/**
	 * List containing the match number for each inserted score value of the player
	 */
	private List<String> scoresMatchNumbers;

	/**
	 * List containing the combination for each inserted win/loss of the player
	 */
	private List<String> winsCombinations;

	/**
	 * List containing the match number for each inserted win/loss of the player
	 */
	private List<String> winsMatchNumbers;

	/**
	 * List containing the score for all matches of the player.
	 */
	private List<Integer> scores;

	/**
	 * List containing the wins for all matches of the player.
	 * (0 if some other player got a higher score, 1/(#players that got highest score) if the player is one of them).
	 */
	private List<Double> wins;

	/**
	 * Average of the scores.
	 */
	private int scoreSum;

	/**
	 * Average of the points.
	 */
	private double winsSum;

	/**
	 * Maximum score ever obtained by the player.
	 */
	private int maxScore;

	/**
	 * Minimum score ever obtained by the player.
	 */
	private int minScore;

	/**
	 * Maximum win percentage ever obtained by the player.
	 */
	private double maxWinPercentage;

	/**
	 * Minimum win percentage ever obtained by the player.
	 */
	private double minWinPercentage;


	/**
	 *
	 */
	public PlayerStatistics() {
		this.scoresCombinations = new ArrayList<String>();
		this.scoresMatchNumbers = new ArrayList<String>();
		this.winsCombinations = new ArrayList<String>();
		this.winsMatchNumbers = new ArrayList<String>();
		this.scores = new ArrayList<Integer>();
		this.wins = new ArrayList<Double>();
		this.scoreSum = 0;
		this.winsSum = 0;
		this.maxScore = Integer.MIN_VALUE;
		this.minScore = Integer.MAX_VALUE;
		this.maxWinPercentage = -1.0; // Wins can only take a value between 0 and 1
		this.minWinPercentage = 2.0;
	}

	public void addScore(int score, String combination, String matchNumber){

		this.scoresCombinations.add(combination);
		this.scoresMatchNumbers.add(matchNumber);

		this.scores.add(score);

		this.scoreSum += score;
		if(score > this.maxScore){
			this.maxScore = score;
		}
		if(score < this.minScore){
			this.minScore = score;
		}
	}

	public void addWins(double win, String combination, String matchNumber){

		this.winsCombinations.add(combination);
		this.winsMatchNumbers.add(matchNumber);

		this.wins.add(win);
		this.winsSum += win;
		if(win > this.maxWinPercentage){
			this.maxWinPercentage = win;
		}
		if(win < this.minWinPercentage){
			this.minWinPercentage = win;
		}
	}

	public List<Integer> getScores(){
		return this.scores;
	}

	public List<Double> getWins(){
		return this.wins;
	}

	public List<String> getScoresCombinations(){
		return this.scoresCombinations;
	}

	public List<String> getScoresMatchNumbers(){
		return this.scoresMatchNumbers;
	}

	public List<String> getWinsCombinations(){
		return this.winsCombinations;
	}

	public List<String> getWinsMatchNumbers(){
		return this.winsMatchNumbers;
	}

	public double getAvgScore(){
		if(this.scores.isEmpty()){
			return -1;
		}
		return ((double)this.scoreSum)/((double) this.scores.size());
	}

	public double getAvgWins(){
		if(this.wins.isEmpty()){
			return -1;
		}
		return ((double)this.winsSum)/((double) this.wins.size());
	}

	public int getMaxScore(){
		return this.maxScore;
	}

	public int getMinScore(){
		return this.minScore;
	}

	public double getMaxWinPercentage(){
		return this.maxWinPercentage;
	}

	public double getMinWinPercentage(){
		return this.minWinPercentage;
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

	public double getWinsStandardDeviation(){
		if(this.wins.isEmpty()){
			return -1;
		}
		double squaredSum = 0.0;
		double avgPoints = this.getAvgWins();
		for(Double point : this.wins){
			double difference = point - avgPoints;
			squaredSum += (difference * difference);
		}
		return Math.sqrt(squaredSum/(this.wins.size()-1));
	}

	public double getScoresSEM(){
		double standardDev = this.getScoresStandardDeviation();
		if(standardDev == -1){
			return -1;
		}
		return (standardDev / Math.sqrt(this.scores.size()));
	}

	public double getWinsSEM(){
		double standardDev = this.getWinsStandardDeviation();
		if(standardDev == -1){
			return -1;
		}
		return (standardDev / Math.sqrt(this.wins.size()));
	}

}

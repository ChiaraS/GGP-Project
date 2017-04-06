package csironi.ggp.course.statsSummarizer;

public class ParamsComboInfo {

	private int numCommits;
	private int numWins;
	private int numTies;
	private int numLosses;

	public ParamsComboInfo(){
		this.numCommits = 0;
		this.numWins = 0;
		this.numTies = 0;
		this.numLosses = 0;

	}

	public int getNumCommits() {
		return numCommits;
	}

	public void increaseNumCommits() {
		this.numCommits++;
	}

	public int getNumWins() {
		return numWins;
	}

	public void increaseNumWins() {
		this.numWins++;
	}

	public int getNumTies() {
		return numTies;
	}

	public void increaseNumTies() {
		this.numTies++;
	}

	public int getNumLosses() {
		return numLosses;
	}

	public void increaseNumLosses() {
		this.numLosses++;
	}

}

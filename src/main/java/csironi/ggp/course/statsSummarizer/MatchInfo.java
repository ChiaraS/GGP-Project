package csironi.ggp.course.statsSummarizer;

import java.io.File;

public class MatchInfo {

	private File correspondingFile; // Not sure if to remove

	private String[] playersNames;
	private int[] playersGoals;

	public MatchInfo(String[] playersNames, int[] playersGoals, File correspondingFile){
		this.playersNames = playersNames;
		this.playersGoals = playersGoals;

		this.correspondingFile = correspondingFile;
	}

	public String[] getPlayersNames(){
		return this.playersNames;
	}

	public int[] getplayersGoals(){
		return this.playersGoals;
	}

	public File getCorrespondingFile(){
		return this.correspondingFile;
	}

	public String getMatchNumber(){
		return this.correspondingFile.getName().split("\\.")[0];
	}

	public String getCombination(){
		String combination = this.correspondingFile.getParentFile().getName();
		return combination.replace("Combination", "");
	}

}

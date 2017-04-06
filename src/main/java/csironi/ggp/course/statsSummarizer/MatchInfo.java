package csironi.ggp.course.statsSummarizer;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import csironi.ggp.course.utils.Pair;

public class MatchInfo {

	private File correspondingFile;

	private String[] playersNames;
	private String[] playersRoles;
	private int[] playersGoals;

	/**
	 * Register for each (player-type,role) pair the win%.
	 * FOR SINGLE-PLAYER GAMES:
	 * 1 if the player got the highest score (i.e. 100), 0 otherwise.
	 * FOR MULTI_PLAYER GAMES:
	 * 0 if the player playing the role lost (i.e. there is at least one other player that got a higher score).
	 * 1 if the player playing the role was the only one that got the highest score.
	 * 1/n if the player playing the role was among the n players that got the highest score.
	 */
	private Map<Pair<String,String>,Double> finalOutcome;

	private Set<String> playerTypes;
	private Set<String> roles;


	public MatchInfo(String[] playersNames, String[] playersRoles, int[] playersGoals, File correspondingFile){
		this.playersNames = playersNames;
		this.playersRoles = playersRoles;
		this.playersGoals = playersGoals;

		this.correspondingFile = correspondingFile;

		this.finalOutcome = new HashMap<Pair<String,String>,Double>();

		this.playerTypes = new HashSet<String>();

		for(int i = 0; i < this.playersNames.length; i++){
			this.playerTypes.add(this.playersNames[i]);
		}

		this.roles = new HashSet<String>();

		for(int i = 0; i < this.playersRoles.length; i++){
			this.roles.add(this.playersRoles[i]);
		}
	}

	public String[] getPlayersNames(){
		return this.playersNames;
	}

	public String[] getPlayersRoles(){
		return this.playersRoles;
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

	/**
	 * Adds the given outcome for the given player type.
	 *
	 * If the player type is not one of the player types involved in this match or if
	 * its outcome had already been set, this method does nothing and returns false to
	 * signal that something went wrong. Note that this should never happen, so if it
	 * does there must be something wrong in the external code.	 *
	 *
	 * @param playerType
	 * @param outcome
	 * @return
	 */
	public boolean addFinalOutcome(String playerType, String playerRole, double outcome){

		if(this.playerTypes.contains(playerType) && this.roles.contains(playerRole)){

			Pair<String,String> theKey = new Pair<String,String>(playerType, playerRole);
			if(this.finalOutcome.get(theKey) == null){
				this.finalOutcome.put(theKey, new Double(outcome));
				return true;
			}
		}

		return false;

	}

	public double getFinalOutcome(String playerType, String playerRole){
		return this.finalOutcome.get(new Pair<String,String>(playerType, playerRole));
	}

}

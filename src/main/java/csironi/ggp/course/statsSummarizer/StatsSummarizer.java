/**
 *
 */
package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import external.JSON.JSONArray;
import external.JSON.JSONException;
import external.JSON.JSONObject;

/**
 * @author C.Sironi
 *
 */
public class StatsSummarizer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/************************************ Prepare the folders *********************************/

		if(args.length != 1){
			System.out.println("Impossible to start statistics summarization. Specify the folder containing the MatchesLogs folder for which to create the statistics.");
			return;
		}

		String mainFolderPath = args[0];

		String matchesLogsFolderPath = mainFolderPath + "/MatchesLogs";

		File matchesLogsFolder = new File(matchesLogsFolderPath);

		if(!matchesLogsFolder.isDirectory()){
			System.out.println("Impossible to find the log directory to summarize.");
			return;
		}

		// Create (or empty if it already exists) the folder where to move all the match log files
		// that have been rejected and haven't been considered when computing the statistics.
		String rejectedFilesFolderPath = mainFolderPath + "/RejectedFiles";
		File rejectedFilesFolder = new File(rejectedFilesFolderPath);
		if(rejectedFilesFolder.isDirectory()){
			if(!emptyFolder(rejectedFilesFolder)){
				System.out.println("Summarization interrupted. Cannot empty the RejectedFiles folder.");
				return;
			}
		}else{
			if(!rejectedFilesFolder.mkdir()){
				System.out.println("Summarization interrupted. Cannot create the RejectedFiles folder.");
				return;
			}
		}

		// Create (or empty if it already exists) the folder where to save all the statistics.
		String statsFolderPath = mainFolderPath + "/Statistics";
		File statsFolder = new File(statsFolderPath);
		if(statsFolder.isDirectory()){
			if(!emptyFolder(statsFolder)){
				System.out.println("Summarization interrupted. Cannot empty the Statistics folder.");
				return;
			}
		}else{
			if(!statsFolder.mkdir()){
				System.out.println("Summarization interrupted. Cannot create the Statistics folder.");
				return;
			}
		}

		// TODO: here, before analyzing all .json files, we can look in the folder for each combination of players
		// and reject the files of the last n-1 completed matches for the given players configuration.
		// This assumes that every tourney is splitted in k subtourneys, one for each possible combination of players,
		// and that all the subtourneys are played sequentially using a different executor that always executes n
		// parallel matches. In this way, for every configuration we can eliminate from the statistics the last n-1
		// completed matches, that are the ones that were not running with a total of exactly n matches at the same
		// time and thus can alter the statistics.

		File[] comboDirs;

		File[] matchLogs;

		// Lists of match info grouped by configuration.
		List<List<MatchInfo>> matchInfo = new ArrayList<List<MatchInfo>>();

		List<MatchInfo> comboMatchInfo;

		MatchInfo theInfo;

		/** Parse the log files of every match for every combination to retrieve the needed info about the matches and reject the ones that cannot be used **/

		// Keep track of the minimum number of matches that could be accepted for a single combination.
		int minAccepted = Integer.MAX_VALUE;

		// Iterate over the directories containing the matches logs for each player's combination.
		comboDirs = matchesLogsFolder.listFiles();

		// For the folder of each players'combination...
		for(int i = 0; i < comboDirs.length; i++){

			if(comboDirs[i].isDirectory()){

				comboMatchInfo = new ArrayList<MatchInfo>();

				//...get all the matches logs...
				matchLogs = comboDirs[i].listFiles();

				//...and for each of them, check if they are usable or must be rejected.
				for(int j = 0; j < matchLogs.length; j++){

					if(matchLogs[j].isFile()){
						theInfo = getMatchInfo(matchLogs[j]);
						if(theInfo != null){
							comboMatchInfo.add(theInfo);
						}else{
							rejectFile(matchLogs[j], rejectedFilesFolderPath + "/" + comboDirs[i].getName());
						}
					}
				}

				if(comboMatchInfo.size() < minAccepted){
					minAccepted = comboMatchInfo.size();
				}

				matchInfo.add(comboMatchInfo);
			}
		}

		/** Discard for each combination the amount of matches needed to consider the same amount of matches for each combination **/

		Random random = new Random();

		for(List<MatchInfo> infoList : matchInfo){
			while((infoList.size() - minAccepted) > 0){
				System.out.println("Excluding extra file for a combination.");
				theInfo = infoList.remove(random.nextInt(infoList.size()));
				rejectFile(theInfo.getCorrespondingFile(), rejectedFilesFolderPath + "/" + theInfo.getCorrespondingFile().getParentFile().getName());
			}
		}

		/******************** Compute the statistics for every player in the tournament **********************/

		// Create a map that contains the statistic for every player.
		Map<String, PlayerStatistics> playersStatistics = new HashMap<String, PlayerStatistics>();

		int maxScore;
        List<Integer> maxScoreIndices;
        String[] playersNames;
        int[] playersGoals;
        double[] playersPoints;
        PlayerStatistics theStats;
        double winnerPoint;

		for(List<MatchInfo> infoList : matchInfo){
			for(MatchInfo mi : infoList){

				playersNames = mi.getPlayersNames();
				playersGoals = mi.getplayersGoals();
				playersPoints = new double[playersGoals.length];

	            if(playersNames.length > 1){
	            	maxScore = Integer.MIN_VALUE;
	            	maxScoreIndices = new ArrayList<Integer>();

	            	for(int i = 0; i < playersGoals.length; i++){
	            		if(playersGoals[i] > maxScore){
	            			maxScore = playersGoals[i];
	            			maxScoreIndices.clear();
	            			maxScoreIndices.add(i);
	            		}else if(playersGoals[i] == maxScore){
	            			maxScoreIndices.add(i);
	            		}
					}

	            	winnerPoint = 1.0/((double)maxScoreIndices.size());

		            for(int i = 0; i < maxScoreIndices.size(); i++){
		            	playersPoints[maxScoreIndices.get(i).intValue()] = winnerPoint;
		            }

	            }else{
	            	playersPoints[0] = ((double)playersGoals[0]) / 100.0;
	            }

	            // Now that for the match we have all the player names, their scores and their points,
	            // we can add the values to the players statistics. NOTE: we must check if a player name
	            // has no associated statistics yet, and if so, create them.
	            for(int i = 0; i < playersNames.length; i++){
	            	theStats = playersStatistics.get(playersNames[i]);
	            	if(theStats == null){
	            		playersStatistics.put(playersNames[i], new PlayerStatistics());
	            		theStats = playersStatistics.get(playersNames[i]);
	            	}
	            	theStats.addScore(playersGoals[i]);
	            	theStats.addPoints(playersPoints[i]);
	            	theStats.addCombination("C"+mi.getCombination());
	            	theStats.addMatchNumber(mi.getMatchNumber());
	            }
			}
		}

		String scoresStatsFilePath = statsFolder + "/ScoreStats.csv";

		String pointsStatsFilePath = statsFolder + "/PointsStats.csv";

		writeToFile(scoresStatsFilePath, "Player;#Samples;MaxScore;MinScore;StandardDeviation;StdErrMean;AvgScore;ConfidenceInterval;");

		writeToFile(pointsStatsFilePath, "Player;#Samples;MaxPoints;MinPoints;StandardDeviation;StdErrMean;AvgWin%;ConfidenceInterval;");

		for(Entry<String, PlayerStatistics> entry : playersStatistics.entrySet()){

			PlayerStatistics stats = entry.getValue();

			writeToFile(statsFolder + "/" + entry.getKey() + "-ScoreAndPoints.csv", "Combination;Match number;Scores;Points;");
			List<String> combinations = stats.getCombinations();
			List<String> matchNumbers = stats.getMatchNumbers();
			List<Integer> scores = stats.getScores();
			List<Double> points = stats.getPoints();
			for(int i = 0; i < scores.size(); i++){
				writeToFile(statsFolder + "/" + entry.getKey() + "-ScoreAndPoints.csv", combinations.get(i) + ";" + matchNumbers.get(i) + ";" + scores.get(i) + ";" + points.get(i) + ";");
			}

			writeToFile(scoresStatsFilePath, entry.getKey() + ";" + scores.size() + ";" + stats.getMaxScore() + ";"
					+ stats.getMinScore() + ";" + stats.getScoresStandardDeviation() + ";" + stats.getScoresSEM() + ";"
					+ stats.getAvgScore() + ";" + (stats.getScoresSEM() * 1.96) + ";");

			writeToFile(pointsStatsFilePath, entry.getKey() + ";" + points.size() + ";" + stats.getMaxPoints() + ";"
					+ stats.getMinPoints() + ";" + stats.getPointsStandardDeviation() + ";" + stats.getPointsSEM() + ";"
					+ (stats.getAvgPoints()*100) + ";" + (stats.getPointsSEM() * 1.96 * 100) + ";");
		}

	}

	private static boolean emptyFolder(File theFolder){

		if(!theFolder.isDirectory()){
			return false;
		}

		File[] children = theFolder.listFiles();

		for(int i=0; i < children.length; i++){
			if(children[i].isDirectory()){
				if(!emptyFolder(children[i])){
					return false;
				}
			}
			if(!children[i].delete()){
				return false;
			}
		}

		return true;
	}

	/**
	 * This method tries to get from the given file all the info about the match needed to compute the statistics.
	 * If it fails for any reason, it will return null.
	 *
	 * @param file the file containing the information.
	 * @return the information about the match if they can be computed, null otherwise.
	 */
	private static MatchInfo getMatchInfo(File file) {

		String[] splittedName = file.getName().split("\\.");

		if(!splittedName[splittedName.length-1].equalsIgnoreCase("json")){
			System.out.println("File without .json extension.");
			return null;
		}

		BufferedReader br;
		String theLine;
		try {
			br = new BufferedReader(new FileReader(file));
			theLine = br.readLine();
			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading a file in the match log folder.");
        	e.printStackTrace();
        	return null;
		}

		// Check if the file was empty.
		if(theLine == null || theLine.equals("")){
			System.out.println("Empty JSON file.");
        	return null;
		}

        // Check if the file is a JSON file with the correct syntax.
        JSONObject matchJSONObject = null;
        try{
        	matchJSONObject = new JSONObject(theLine);
        }catch(JSONException e){
        	System.out.println("Exception when parsing file to JSON.");
        	e.printStackTrace();
        	return null;
        }

        // Check if the JSON file contains all the needed information.
        if(matchJSONObject == null || !matchJSONObject.has("isAborted") || !matchJSONObject.has("isCompleted")
        		 || !matchJSONObject.has("errors") || !matchJSONObject.has("goalValues")
        		 || !matchJSONObject.has("playerNamesFromHost")){

        	System.out.println("Missing information in the JSON file.");
        	return null;
        }

        // Check if the JSON file corresponds to a match properly completed
        try{
        	if(!matchJSONObject.getBoolean("isCompleted") || matchJSONObject.getBoolean("isAborted")){
        		System.out.println("JSON file corresponding to a match that didn't complete correctly.");
        		return null;
        	}
        }catch(JSONException e){
        	System.out.println("Information (\"isCompleted\", \"isAborted\") improperly formatted in the JSON file.");
        	e.printStackTrace();
        	return null;
        }

        // Check that no player had any error during the match.
        // NOTE: if for at least one player there is an error, the match won't be considered in the statistics.
        // However, this is not always a good idea. Change this behavior if you want to consider errors in the
        // statistics (modifying the goals according to the meaning that those errors have).

        // Get the array with errors for all game steps.
        JSONArray errors;
        try{
        	errors = matchJSONObject.getJSONArray("errors");
        }catch(JSONException e){
        	System.out.println("Information (\"errors\") improperly formatted in the JSON file.");
        	e.printStackTrace();
        	return null;
        }

        // For each game step get the array with errors for every player.
        for(int j = 0; j < errors.length(); j++){
        	JSONArray stepErrors = null;
            try{
            	stepErrors = errors.getJSONArray(j);
            }catch(JSONException e){
            	System.out.println("Information (\"errors\" array) improperly formatted in the JSON file.");
            	e.printStackTrace();
            	return null;
            }

            // Check for every players if there are errors.
            for(int k = 0; k < stepErrors.length(); k++){
            	try {
					if(!stepErrors.getString(k).equals("")){
						System.out.println("Found an error for a player in the match corresponding to the JSON file.");
						return null;
					}
				} catch (JSONException e) {
					System.out.println("Information (\"errors\" array of single step) improperly formatted in the JSON file.");
	            	e.printStackTrace();
					return null;
				}
            }
    	}

        // Get the player names
        String[] playersNames;
        int[] playersGoals;

        try{
        	JSONArray players = matchJSONObject.getJSONArray("playerNamesFromHost");
        	playersNames = new String[players.length()];
        	for(int j = 0; j < players.length(); j++){
        		playersNames[j] = players.getString(j);
        	}
        }catch(JSONException e){
        	System.out.println("Information (\"playerNamesFromHost\" array) improperly formatted in the JSON file.");
        	e.printStackTrace();
        	return null;
        }

        // Parse all the goals for the players.
        try{
        	JSONArray goalValues = matchJSONObject.getJSONArray("goalValues");
        	playersGoals = new int[goalValues.length()];
        	for(int j = 0; j < goalValues.length(); j++){
        		playersGoals[j] = goalValues.getInt(j);
        		if(playersGoals[j] < 0 || playersGoals[j] > 100){
                	System.out.println("Error: found goal " + playersGoals[j] + " outside of the interval [0, 100].");
                	return null;
        		}
        	}
        }catch(JSONException e){
        	System.out.println("Information (\"goalValues\" array) improperly formatted in the JSON file.");
        	e.printStackTrace();
        	return null;
        }

        if(playersNames.length <= 0 || playersGoals.length <= 0){
        	System.out.println("Error: found no players names and/or no players goals.");
        	return null;
        }

        if(playersNames.length != playersGoals.length){
        	System.out.println("Error: found " + playersGoals.length + " goal values for " + playersNames.length + " players.");
        	return null;
        }

        return new MatchInfo(playersNames, playersGoals, file);

	}

	private static void rejectFile(File theRejectedFile, String rejectedFilesFolderPath){

		System.out.println("Rejecting file " + theRejectedFile.getPath());

		boolean success = theRejectedFile.renameTo(new File(rejectedFilesFolderPath + "/" + theRejectedFile.getName()));
    	if(!success){
    		System.out.println("Failed to move file " + theRejectedFile.getPath() + " to the RejectedFiles folder. Excluding it from the summary anyway.");
    	}
	}

	private static void writeToFile(String filename, String message){
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(filename, true));
			out.write(message+"\n");
            out.close();
		} catch (IOException e) {
			System.out.println("Error writing file " + filename + ".");
			e.printStackTrace();
		}
	}

}

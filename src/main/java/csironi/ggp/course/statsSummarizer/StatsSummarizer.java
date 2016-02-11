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

		if(args.length != 1){
			System.out.println("Impossible to start statistics summarization. Specify the folder containing the MatchesLogs folder for which to create the statistics.");
			return;
		}

		String mainFolderPath = args[0];

		String matchLogFolderPath = mainFolderPath + "/MatchesLogs";

		File matchLogFolder = new File(matchLogFolderPath);

		if(matchLogFolder.isDirectory()){

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
				}
			}

			// TODO: here, before analyzing all .json files, we can look in the folder for each combination of players
			// and reject the files of the last n-1 completed matches for the given players configuration.
			// This assumes that every tourney is splitted in k subtourneys, one for each possible combination of players,
			// and that all the subtourneys are played sequentially using a different executor that always executes n
			// parallel matches. In this way, for every configuration we can eliminate from the statistics the last n-1
			// completed matches, that are the ones that were not running with a total of exactly n matches at the same
			// time and thus can alter the statistics.

			File[] children = matchLogFolder.listFiles();

			// Create a map that contains the statistic for every player.
			Map<String, PlayerStatistics> playersStatistics = new HashMap<String, PlayerStatistics>();

			for(int i = 0; i < children.length; i++){

				if(children[i].isFile()){

					String[] splittedName = children[i].getName().split("\\.");

					if(!splittedName[splittedName.length-1].equalsIgnoreCase("json")){
						System.out.println("Found a file without .json extension.");
		            	System.out.println("Rejecting file.");
		            	rejectFile(children[i], rejectedFilesFolderPath);
		            	continue;
					}

					BufferedReader br;
					String theLine;
					try {
						br = new BufferedReader(new FileReader(children[i]));
						theLine = br.readLine();
						br.close();
					} catch (IOException e) {
						System.out.println("Exception when reading a file in the match log folder.");
		            	e.printStackTrace();
		            	System.out.println("Rejecting file.");
		            	rejectFile(children[i], rejectedFilesFolderPath);
		            	continue;
					}

					// Check if the file was empty.
					if(theLine == null || theLine.equals("")){
						System.out.println("Empty JSON file.");
		            	System.out.println("Rejecting file.");
		            	rejectFile(children[i], rejectedFilesFolderPath);
		            	continue;
					}

		            // Check if the file is a JSON file with the correct syntax.
		            JSONObject matchJSONObject = null;
		            try{
		            	matchJSONObject = new JSONObject(theLine);
		            }catch(JSONException e){
		            	System.out.println("Exception when parsing file to JSON.");
		            	e.printStackTrace();
		            	System.out.println("Rejecting file.");
		            	rejectFile(children[i], rejectedFilesFolderPath);
		            	continue;
		            }

		            // Check if the file contains all the information we need properly formatted.
		            if(!isProperJSONObject(matchJSONObject)){
		            	System.out.println("Rejecting file.");
			        	rejectFile(children[i], rejectedFilesFolderPath);
			        	continue;
		            }

		            // Get the player names
		            List<String> players = new ArrayList<String>();
		            try{
		            	JSONArray playerNames = matchJSONObject.getJSONArray("playerNamesFromHost");
		            	for(int j = 0; j < playerNames.length(); j++){
		            		players.add(playerNames.getString(j));
		            	}
		            }catch(JSONException e){
		            	System.out.println("Information (\"playerNamesFromHost\" array) improperly formatted in the JSON file.");
		            	e.printStackTrace();
		            	rejectFile(children[i], rejectedFilesFolderPath);
		            	continue;
		            }

		            List<Integer> scores = new ArrayList<Integer>();
		            List<Double> points = new ArrayList<Double>();
		            int maxScore = Integer.MIN_VALUE;
		            int currentScore = -1;
		            List<Integer> maxScoreIndices = new ArrayList<Integer>();

		            // Parse all the scores for the players, computing the maximum score and keeping
		            // track of the indices of the players that obtained the maximum score.
		            // In the meanwhile, initialize to 0 the points for every player
		            try{
		            	JSONArray goalValues = matchJSONObject.getJSONArray("goalValues");
		            	for(int j = 0; j < goalValues.length(); j++){
		            		currentScore = goalValues.getInt(j);
		            		scores.add(currentScore);
		            		points.add(0.0);
		            		if(currentScore > maxScore){
		            			maxScore = currentScore;
		            			maxScoreIndices.clear();
		            			maxScoreIndices.add(j);
		            		}else if(currentScore == maxScore){
		            			maxScoreIndices.add(j);
		            		}
		            	}
		            }catch(JSONException e){
		            	System.out.println("Information (\"goalValues\" array) improperly formatted in the JSON file.");
		            	e.printStackTrace();
		            	rejectFile(children[i], rejectedFilesFolderPath);
		            	continue;
		            }

		            if(maxScoreIndices.size() == 0){
		            	System.out.println("Error retrieving players with maximum score for match. MaxScore = " + maxScore + ".");
		            	rejectFile(children[i], rejectedFilesFolderPath);
		            	continue;
		            }

		            if(players.size() != scores.size()){
		            	System.out.println("Error: found " + scores.size() + " goal values for " + players.size() + " players.");
		            	rejectFile(children[i], rejectedFilesFolderPath);
		            	continue;
		            }

		            // Split the winning point.
		            double winnerPoint;
		            if(players.size() == 1){
		            	winnerPoint = ((double)scores.get(0)) / 100.0;
		            }else{
		            	winnerPoint = 1.0/((double)maxScoreIndices.size());
		            }

		            for(int j = 0; j < maxScoreIndices.size(); j++){
		            	points.set(maxScoreIndices.get(j).intValue(), winnerPoint);
		            }

		            // Now that for the match we have all the player names, their scores and their points,
		            // we can add the values to the players statistics. NOTE: we must check if a player name
		            // has no associated statistics yet, and if so, create them.
		            for(int j = 0; j < players.size(); j++){
		            	PlayerStatistics theStats = playersStatistics.get(players.get(j));
		            	if(theStats == null){
		            		playersStatistics.put(players.get(j), new PlayerStatistics());
		            		theStats = playersStatistics.get(players.get(j));
		            	}
		            	theStats.addScore(scores.get(j));
		            	theStats.addPoints(points.get(j));
		            }
				}else{
					System.out.println("Summarization interrupted. Make sure the folder with match logs only contains files and not directories.");
					return;
				}
			}

			// Here we parsed all .json files for all matches and collected all the scores.
			// We can compute the statistics and save them to file.

			String scoresStatsFilePath = statsFolder + "/ScoreStats.csv";

			String pointsStatsFilePath = statsFolder + "/PointsStats.csv";

			writeToFile(scoresStatsFilePath, "Player;#Samples;MaxScore;MinScore;StandardDeviation;StdErrMean;AvgScore;ConfidenceInterval;");

			writeToFile(pointsStatsFilePath, "Player;#Samples;MaxPoints;MinPoints;StandardDeviation;StdErrMean;AvgWin%;ConfidenceInterval;");

			for(Entry<String, PlayerStatistics> entry : playersStatistics.entrySet()){

				PlayerStatistics stats = entry.getValue();

				writeToFile(statsFolder + "/" + entry.getKey() + "-ScoreAndPoints.csv", "Score;Points;");
				List<Integer> scores = stats.getScores();
				List<Double> points = stats.getPoints();
				for(int i = 0; i < scores.size(); i++){
					writeToFile(statsFolder + "/" + entry.getKey() + "-ScoreAndPoints.csv", scores.get(i) + ";" + points.get(i) + ";");
				}

				writeToFile(scoresStatsFilePath, entry.getKey() + ";" + scores.size() + ";" + stats.getMaxScore() + ";"
						+ stats.getMinScore() + ";" + stats.getScoresStandardDeviation() + ";" + stats.getScoresSEM() + ";"
						+ stats.getAvgScore() + ";" + (stats.getScoresSEM() * 1.96) + ";");

				writeToFile(pointsStatsFilePath, entry.getKey() + ";" + points.size() + ";" + stats.getMaxPoints() + ";"
						+ stats.getMinPoints() + ";" + stats.getPointsStandardDeviation() + ";" + stats.getPointsSEM() + ";"
						+ (stats.getAvgPoints()*100) + ";" + (stats.getPointsSEM() * 1.96 * 100) + ";");

			}

		}else{
			System.out.println("Impossible to find the log directory to summarize.");
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

	private static void rejectFile(File theRejectedFile, String rejectedFilesFolderPath){
		boolean success = theRejectedFile.renameTo(new File(rejectedFilesFolderPath + "/" + theRejectedFile.getName()));
    	if(!success){
    		System.out.println("Failed to move file " + theRejectedFile.getPath() + " to the RejectedFiles folder. Excluding it from the summary anyway.");
    	}
	}

	private static boolean isProperJSONObject(JSONObject matchJSONObject){

        // Check if the JSON file contains all the needed information.
        if(matchJSONObject == null || !matchJSONObject.has("isAborted") || !matchJSONObject.has("isCompleted")
        		 || !matchJSONObject.has("errors") || !matchJSONObject.has("goalValues")
        		 || !matchJSONObject.has("playerNamesFromHost")){

        	System.out.println("Missing information in the JSON file.");
        	return false;
        }

        // Check if the JSON file corresponds to a match properly completed
        try{
        	if(!matchJSONObject.getBoolean("isCompleted") || matchJSONObject.getBoolean("isAborted")){
        		System.out.println("JSON file corresponding to a match that didn't complete correctly.");
        		return false;
        	}
        }catch(JSONException e){
        	System.out.println("Information (\"isCompleted\", \"isAborted\") improperly formatted in the JSON file.");
        	e.printStackTrace();
        	return false;
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
        	return false;
        }

        // For each game step get the array with errors for every player.
        for(int j = 0; j < errors.length(); j++){
        	JSONArray stepErrors = null;
            try{
            	stepErrors = errors.getJSONArray(j);
            }catch(JSONException e){
            	System.out.println("Information (\"errors\" array) improperly formatted in the JSON file.");
            	e.printStackTrace();
            	return false;
            }

            // Check for every players if there are errors.
            for(int k = 0; k < stepErrors.length(); k++){
            	try {
					if(!stepErrors.getString(k).equals("")){
						System.out.println("Found an error for a player in the match corresponding to the JSON file.");
						return false;
					}
				} catch (JSONException e) {
					System.out.println("Information (\"errors\" array of single step) improperly formatted in the JSON file.");
	            	e.printStackTrace();
					return false;
				}
            }

    	}

        return true;
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

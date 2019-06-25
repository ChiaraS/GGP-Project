/**
 *
 */
package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import csironi.ggp.course.experiments.propnet.SingleValueDoubleStats;
import csironi.ggp.course.experiments.propnet.SingleValueLongStats;
import csironi.ggp.course.experiments.propnet.SingleValueStats;
import external.JSON.JSONArray;
import external.JSON.JSONException;
import external.JSON.JSONObject;

/**
 * Given the folder with the logs of a tourney, this class computes the scores and wins statistics for the tourney.
 *
 * @author C.Sironi
 *
 */
public class StatsSummarizer {

	public static final Map<String, String> headerToFile;

	static{
		headerToFile = new HashMap<String, String>();

		// Maps the game keys into the corresponding game name we want to have in the paper
		headerToFile.put("Thinking time(ms)","ThinkingTimeMs");
		headerToFile.put("Search time(ms)","SearchTimeMs");
		headerToFile.put("Iterations/second","IterationsPerSecond");
		headerToFile.put("Nodes/second","NodesPerSecond");
		headerToFile.put("#Nodes","NumNodes");
		headerToFile.put("#ActionsStats","NumActionsStats");
		headerToFile.put("#RAVE_AMAFStats","NumRaveAmafStats");
		headerToFile.put("#GRAVE_AMAFStats","NumGraveAmafStats");
		headerToFile.put("ActionsStats/Node","ActionsStatsPerNode");
		headerToFile.put("RAVE_AMAFStats/Node","RaveAmafStatsPerNode");
		headerToFile.put("GRAVE_AMAFStats/Node","GraveAmafStatsPerNode");
	}

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

		/**
		 * NOTE: the last folder in the path given as input might look like one of the following:
		 * - 6PEvoTune.checkers.Tourney-Stats
		 * - 123456789012.6PEvoTune.checkers.Tourney-Stats
		 * The code detects which one of the two is used and acts accordingly
		 */

		/** New instructions that work only if the main folder path contains in the last folder name
		 *  the tourney type and the game key surrounded by "." in the 2nd and 3rd position respectively.
		 *
		 *  E.g.: something.something.gameKey.somethingElse (usually 354658372535.Tourney.gameKey)
		 */
		File mainFolder = new File(mainFolderPath);
		//String[] splitMainFolderPath = mainFolderPath.split("/"); // Works ony if the psth usesthe "/" separator and not
		// Get the name of the tourney folder
		String statsFolderName = mainFolder.getName();
		// Split the tourney folder name
		String tourneyType;
		String gameKey;
		String[] splitStatsFolderName = statsFolderName.split("\\.");
		if(splitStatsFolderName.length == 3) {
			tourneyType = splitStatsFolderName[0];
			gameKey = splitStatsFolderName[1];
		}else if(splitStatsFolderName.length == 4) {
			tourneyType = splitStatsFolderName[1];
			gameKey = splitStatsFolderName[2];
		}else {
			System.out.println("Wrong format for the name of the folder with the statistics to summarize: " + args[0] + ".");
			System.out.println("Interrupting summarization!");
			return;
		}

		// True if we are using for the logs folder the simple format (i.e. without tourneyType and gameKey in the name of the folder)
		// E.g. MatchesLogs
		// False otherwise
		// E.g. 6PUcbNaiveTune.breakthrough.MatchesLogs
		boolean simpleFolderFormat;

		//System.out.println("mainFolderPath= " + mainFolderPath);

		String matchesLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".MatchesLogs";

		//System.out.println("matchesLogsFolderPath= " + matchesLogsFolderPath);

		File matchesLogsFolder = new File(matchesLogsFolderPath);

		if(matchesLogsFolder.isDirectory()){
			simpleFolderFormat = false;
		}else{
			matchesLogsFolderPath = mainFolderPath + "/MatchesLogs";
			matchesLogsFolder = new File(matchesLogsFolderPath);
			if(matchesLogsFolder.isDirectory()){
				simpleFolderFormat = true;
			}else{
				System.out.println("Impossible to find the log directory to summarize. Couldn't find neither " +
						(mainFolderPath + "/" + tourneyType + "." + gameKey + ".MatchesLogs") + " nor " +
						matchesLogsFolder.getPath());
				return;
			}
		}

		// Create (or empty if it already exists) the folder where to move all the match log files
		// that have been rejected and haven't been considered when computing the statistics.
		String rejectedFilesFolderPath;
		if(simpleFolderFormat) {
			rejectedFilesFolderPath = mainFolderPath + "/RejectedMatchesLogs";
		}else{
			rejectedFilesFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedMatchesLogs";
		}

		//System.out.println("rejectedFilesFolderPath= " + rejectedFilesFolderPath);


		File rejectedFilesFolder = new File(rejectedFilesFolderPath);
		if(rejectedFilesFolder.isDirectory()){
			if(!emptyFolder(rejectedFilesFolder)){
				System.out.println("Summarization interrupted. Cannot empty the RejectedFiles folder: " + rejectedFilesFolder.getPath());
				return;
			}
		}else{
			if(!rejectedFilesFolder.mkdir()){
				System.out.println("Summarization interrupted. Cannot create the RejectedFiles folder: " + rejectedFilesFolder.getPath());
				return;
			}
		}

		// Create (or empty if it already exists) the folder where to save all the statistics.
		String statsFolderPath;
		if(simpleFolderFormat) {
			statsFolderPath = mainFolderPath + "/Statistics";
		}else{
			statsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".Statistics";
		}
		File statsFolder = new File(statsFolderPath);
		if(statsFolder.isDirectory()){
			if(!emptyFolder(statsFolder)){
				System.out.println("Summarization interrupted. Cannot empty the Statistics folder: " + statsFolder.getPath());
				return;
			}
		}else{
			if(!statsFolder.mkdir()){
				System.out.println("Summarization interrupted. Cannot create the Statistics folder: " + statsFolder.getPath());
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

		if(minAccepted == 0){
			System.out.println("Summarization interrupted. No valid matches for at least one of the combinations.");
			return;
		}

		if(matchInfo.size() == 0){
			System.out.println("Summarization interrupted. No combinations folders detected.");
			return;
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

		/****************************** Save to file the results of all matches ******************************/

		//writeToFile(statsFolder + "/AllMatchesScores.csv", "Combination;Match number;Scores;");

		String toWrite;
        String[] playersNames;
        String[] playersRoles;
        int[] playersGoals;

		for(List<MatchInfo> infoList : matchInfo){

			toWrite = "Match number;";

			playersNames = infoList.get(0).getPlayersNames();

			for(int i = 0; i < playersNames.length; i++){
				toWrite += playersNames[i] + ";";
			}

			StatsUtils.writeToFileMkParentDir(statsFolder + "/Combination" + infoList.get(0).getCombination() + ".csv", toWrite);

			for(MatchInfo mi : infoList){

				toWrite = mi.getMatchNumber() + ";";

				playersGoals = mi.getplayersGoals();

				for(int i = 0; i < playersGoals.length; i++){
					toWrite += playersGoals[i] + ";";
				}
				StatsUtils.writeToFileMkParentDir(statsFolder + "/Combination" + mi.getCombination() + ".csv", toWrite);
			}
		}

		/******************** Compute the statistics for every player in the tournament **********************/

		// Create a map that contains the statistic for every player.
		Map<String, PlayerStatistics> playersStatistics = new HashMap<String, PlayerStatistics>();

		int maxScore;
		Set<String> playerTypesSet;
        Set<String> maxScorePlayerTypes;
        PlayerStatistics theStats;
        double splitWin;

		for(List<MatchInfo> infoList : matchInfo){
			for(MatchInfo mi : infoList){

				playersNames = mi.getPlayersNames();
				playersRoles = mi.getPlayersRoles();
				playersGoals = mi.getplayersGoals();

				// For each role add to the corresponding player statistics the score that the player obtained playing that role
				for(int i = 0; i < playersNames.length; i++){
	            	// Get the stats of the player
	            	theStats = playersStatistics.get(playersNames[i]);
	            	if(theStats == null){
	            		playersStatistics.put(playersNames[i], new PlayerStatistics());
	            		theStats = playersStatistics.get(playersNames[i]);
	            	}

	            	theStats.addScore(playersGoals[i], mi.getCombination(), mi.getMatchNumber());
				}

				// Add the wins
	            if(playersNames.length > 1){

	            	// For more roles we need to find the algorithm(s) that won and split 1 win between them

	            	maxScore = Integer.MIN_VALUE;
	            	playerTypesSet = new HashSet<String>();
	            	maxScorePlayerTypes = new HashSet<String>();

	            	for(int i = 0; i < playersGoals.length; i++){
	            		playerTypesSet.add(playersNames[i]);
	            		if(playersGoals[i] > maxScore){
	            			maxScore = playersGoals[i];
	            			maxScorePlayerTypes.clear();
	            			maxScorePlayerTypes.add(playersNames[i]);
	            		}else if(playersGoals[i] == maxScore){
	            			maxScorePlayerTypes.add(playersNames[i]);
	            		}
					}

	            	splitWin = 1.0/((double)maxScorePlayerTypes.size());

	            	// Memorize the outcome for every player in the MatchInfo
	            	for(int i = 0; i < playersGoals.length; i++){
	            		if(playersGoals[i] == maxScore){
		            		if(!mi.addFinalOutcome(playersNames[i], playersRoles[i], splitWin)){
		            			System.out.println("Error when adding final outcome " + splitWin + " to MatchInfo for player " + playersNames[i] + " playing role " + playersRoles[i] + ". The BestComboStats, if any, will be incomplete.");
		            		}
	            		}else{
	            			if(!mi.addFinalOutcome(playersNames[i], playersRoles[i], 0)){
		            			System.out.println("Error when adding final outcome " + 0 + " to MatchInfo for player " + playersNames[i] + " playing role " + playersRoles[i] + ". The BestComboStats, if any, will be incomplete.");
		            		}
	            		}
	            	}

	            	// For each distinct player type that won, update the statistics adding the (split) win
	            	// and for the losers add a loss (i.e. 0).
		            for(String thePlayer: playerTypesSet){

		            	// Get the stats of the player
		            	theStats = playersStatistics.get(thePlayer);
		            	if(theStats == null){
		            		playersStatistics.put(thePlayer, new PlayerStatistics());
		            		theStats = playersStatistics.get(thePlayer);
		            	}

		            	if(maxScorePlayerTypes.contains(thePlayer)){
		            		theStats.addWins(splitWin, mi.getCombination(), mi.getMatchNumber());
		            	}else{
		            		theStats.addWins(0, mi.getCombination(), mi.getMatchNumber());
		            	}

		            }

	            }else{
	            	// Get the stats of the player
	            	theStats = playersStatistics.get(playersNames[0]);
	            	if(theStats == null){
	            		playersStatistics.put(playersNames[0], new PlayerStatistics());
	            		theStats = playersStatistics.get(playersNames[0]);
	            	}

	            	if(playersGoals[0] != 100){
	            		theStats.addWins(0, mi.getCombination(), mi.getMatchNumber());
	            		if(!mi.addFinalOutcome(playersNames[0], playersRoles[0], 0)){
	            			System.out.println("Error when adding final outcome " + 0 + " to MatchInfo for player " + playersNames[0] + ". The BestComboStats, if sny, will be incomplete.");
	            		}
	            	}else{
	            		theStats.addWins(1, mi.getCombination(), mi.getMatchNumber());
	            		if(!mi.addFinalOutcome(playersNames[0], playersRoles[0], 1)){
	            			System.out.println("Error when adding final outcome " + 1 + " to MatchInfo for player " + playersNames[0] + ". The BestComboStats, if sny, will be incomplete.");
	            		}
	            	}
	            }
			}
		}

		String scoresStatsFilePath = statsFolder + "/ScoreStats.csv";

		String winsStatsFilePath = statsFolder + "/WinsStats.csv";

		StatsUtils.writeToFileMkParentDir(scoresStatsFilePath, "Player;#Samples;MinScore;MaxScore;StandardDeviation;StdErrMean;AvgScore;ConfidenceInterval;MinAvgScore;MaxAvgScore;");

		StatsUtils.writeToFileMkParentDir(winsStatsFilePath, "Player;#Samples;MinWins;MaxWins;StandardDeviation;StdErrMean;AvgWin%;ConfidenceInterval;MinAvgWin%;MaxAvgWin%");


		PlayerStatistics stats;
		List<Integer> scores;
		List<Double> wins;
		List<String> combinations;
		List<String> matchNumbers;

		for(Entry<String, PlayerStatistics> entry : playersStatistics.entrySet()){

			stats = entry.getValue();

			scores = stats.getScores();
			combinations = stats.getScoresCombinations();
			matchNumbers = stats.getScoresMatchNumbers();

			StatsUtils.writeToFileMkParentDir(statsFolder + "/" + entry.getKey() + "-ScoreSamples.csv", "Combination;Match number;Score;");

			for(int i = 0; i < scores.size(); i++){
				StatsUtils.writeToFileMkParentDir(statsFolder + "/" + entry.getKey() + "-ScoreSamples.csv", "C" + combinations.get(i) + ";" + matchNumbers.get(i) + ";" + scores.get(i) + ";");
			}

			wins = stats.getWins();
			combinations = stats.getWinsCombinations();
			matchNumbers = stats.getWinsMatchNumbers();

			StatsUtils.writeToFileMkParentDir(statsFolder + "/" + entry.getKey() + "-WinsSamples.csv", "Combination;Match number;Win percentage;");

			for(int i = 0; i < wins.size(); i++){
				StatsUtils.writeToFileMkParentDir(statsFolder + "/" + entry.getKey() + "-WinsSamples.csv", "C" + combinations.get(i) + ";" + matchNumbers.get(i) + ";" + wins.get(i) + ";");
			}

			double avgScore = stats.getAvgScore();
			double scoreCi = (stats.getScoresSEM() * 1.96);

			StatsUtils.writeToFileMkParentDir(scoresStatsFilePath, entry.getKey() + ";" + scores.size() + ";" + stats.getMinScore() + ";"
					+ stats.getMaxScore() + ";" + stats.getScoresStandardDeviation() + ";" + stats.getScoresSEM() + ";"
					+ avgScore + ";" + scoreCi + ";" + (avgScore - scoreCi) + ";" + (avgScore + scoreCi) + ";");

			double avgWinPerc = (stats.getAvgWins()*100);
			double winCi = (stats.getWinsSEM() * 1.96 * 100);

			StatsUtils.writeToFileMkParentDir(winsStatsFilePath, entry.getKey() + ";" + wins.size() + ";" + stats.getMinWinPercentage() + ";"
					+ stats.getMaxWinPercentage() + ";" + stats.getWinsStandardDeviation() + ";" + stats.getWinsSEM() + ";"
					+ avgWinPerc + ";" + winCi + ";" + (avgWinPerc - winCi) + ";" + (avgWinPerc + winCi) + ";");
		}

        // While iterating over the MatchInfos, build a map with the unique match ID as key
        // and the corresponding MatchInfo as value.
        Map<String,MatchInfo> acceptedMatches = new HashMap<String,MatchInfo>();
		for(List<MatchInfo> infoList : matchInfo){
			for(MatchInfo mi : infoList){
				acceptedMatches.put(mi.getCorrespondingFile().getName().substring(0, mi.getCorrespondingFile().getName().length()-5), mi);
			}
		}



		/****************** Compute speed statistics of the matches that were considered in the previous statistics *******************/

		String speedLogsFolderPath;

		if(simpleFolderFormat) {
			speedLogsFolderPath = mainFolderPath + "/SpeedLogs";
		}else{
			speedLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".SpeedLogs";
		}

		//System.out.println("speedLogsFolderPath= " + speedLogsFolderPath);

		File speedLogsFolder = new File(speedLogsFolderPath);

		if(speedLogsFolder.isDirectory()){
			// Create (or empty if it already exists) the folder where to move all the speed log files
			// that have been rejected and haven't been considered when computing the statistics.
			String rejectedSpeedFilesFolderPath;
			if(simpleFolderFormat){
				rejectedSpeedFilesFolderPath = mainFolderPath + "/RejectedSpeedFiles";
			}else{
				rejectedSpeedFilesFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedSpeedFiles";
			}

			//System.out.println("RejectedSpeedFilesFolderPath= " + rejectedSpeedFilesFolderPath);


			File rejectedSpeedFilesFolder = new File(rejectedSpeedFilesFolderPath);
			if(rejectedSpeedFilesFolder.isDirectory()){
				if(!emptyFolder(rejectedSpeedFilesFolder)){
					System.out.println("Summarization interrupted. Cannot empty the RejectedSpeedFiles folder: " + rejectedSpeedFilesFolder.getPath());
					return;
				}
			}else{
				if(!rejectedSpeedFilesFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the RejectedSpeedFiles folder: " + rejectedSpeedFilesFolder.getPath());
					return;
				}
			}

			// Create (or empty if it already exists) the folder where to save all the speed statistics.
			String speedStatsFolderPath;
			if(simpleFolderFormat){
				speedStatsFolderPath = mainFolderPath + "/SpeedStatistics";
			}else{
				speedStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".SpeedStatistics";
			}
			File speedStatsFolder = new File(speedStatsFolderPath);
			if(speedStatsFolder.isDirectory()){
				if(!emptyFolder(speedStatsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the SpeedStatistics folder: " + speedStatsFolder.getPath());
					return;
				}
			}else{
				if(!speedStatsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the SpeedStatistics folder: " + speedStatsFolder.getPath());
					return;
				}
			}

			//List<String> acceptedMatches = new ArrayList<String>();

			int numRoles = matchInfo.get(0).get(0).getplayersGoals().length;

			int summarizedFiles = 0;

			File[] playerTypesDirs;

			File[] playerRolesDirs = null;

			File[] speedLogs = null;

			//File randomSpeedLog;

			String[] columnHeaders = null;
			ColumnType[] columnTypes = null;
			int[] columnIndices = null;

			// Iterate over the directories containing the matches logs for each player's type.
			playerTypesDirs = speedLogsFolder.listFiles();

			// Figure out if logs are old style (i.e. without added states and state mem count) or not
			// For the folder of each player type...
			for(int i = 0; i < playerTypesDirs.length; i++){
				if(playerTypesDirs[i].isDirectory()){
					playerRolesDirs = playerTypesDirs[i].listFiles();
					break;
				}
			}

			if(playerRolesDirs == null) {
				System.out.println("Summarization interrupted. Cannot check speed stats log files header.");
				return;
			}

			// Get one of the folders corresponding to the different roles the player played
			for(File f : playerRolesDirs) {
				if(f.isDirectory()) {
					speedLogs = f.listFiles();
					break;
				}
			}

			if(speedLogs == null) {
				System.out.println("Summarization interrupted. Cannot check speed stats log files header because no player role directory was found.");
				return;
			}

			// For each stats file...
			for(int k = 0; k < speedLogs.length; k++){
				String[] splittedName = speedLogs[k].getName().split("\\.");
				// If it's a .csv file, check the header
				if(splittedName[splittedName.length-1].equalsIgnoreCase("csv")){
					// Check if the header contains "Added nodes" in position 5 and "Memorized states" in osition 6
					BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(speedLogs[k]));
						// Read header
						String header = br.readLine();
						String[] splitHeader = header.split(";");
						if(splitHeader.length > 6 && splitHeader[5].equals("Added nodes") && splitHeader[6].equals("Memorized states")){
							columnHeaders = new String[]{"Thinking time(ms)", "Search time(ms)", "Added nodes", "Memorized states", "Iterations/second", "Nodes/second"};
							columnTypes = new ColumnType[]{ColumnType.LONG, ColumnType.LONG, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE};
							columnIndices = new int[]{1,2,5,6,7,8};
						}else {
							columnHeaders = new String[]{"Thinking time(ms)", "Search time(ms)", "Iterations/second", "Nodes/second"};
							columnTypes = new ColumnType[]{ColumnType.LONG, ColumnType.LONG, ColumnType.DOUBLE, ColumnType.DOUBLE};
							columnIndices = new int[]{1,2,5,6};
						}
						br.close();
					} catch (IOException e) {
						System.out.println("Exception when reading the header of the .csv file " + speedLogs[k].getName() + ".");
						System.out.println("Cannot set correct header names, indices and types for speed stats to summrize.");
			        	e.printStackTrace();
			        	if(br != null){
				        	try {
								br.close();
							} catch (IOException ioe) {
								System.out.println("Exception when closing the .csv file " + speedLogs[k].getName() + ".");
								ioe.printStackTrace();
							}
			        	}
			        	columnHeaders = new String[]{"Thinking time(ms)", "Search time(ms)", "Iterations/second", "Nodes/second"};
						columnTypes = new ColumnType[]{ColumnType.LONG, ColumnType.LONG, ColumnType.DOUBLE, ColumnType.DOUBLE};
						columnIndices = new int[]{1,2,5,6};
					}
					break;
				}
			}

			String playerType;

			String playerRole;

			StatsExtractor extractor;


			// Prepare all the SingleValueStats that will compute the aggregated statistics for each player-role combination over all the matches
			Map<String, Map<String, Map<String, SingleValueDoubleStats>>> aggregatedStatistics = new HashMap<String, Map<String, Map<String, SingleValueDoubleStats>>>();

			// Statistics for which to compute the aggregated statistics
			String[] statisticsNames = new String[]{"numSamples","minValue","maxValue","median","sd","sem","avg","ci"};
			double[] statisticsValues = new double[8];

			// Iterate over the directories containing the matches logs for each player's type.
			playerTypesDirs = speedLogsFolder.listFiles();

			// For the folder of each player type...
			for(int i = 0; i < playerTypesDirs.length; i++){

				if(playerTypesDirs[i].isDirectory()){

					playerType = playerTypesDirs[i].getName();

					playerRolesDirs = playerTypesDirs[i].listFiles();

					// Iterate over all the folders corresponding to the different roles the player played
					for(int j = 0; j < playerRolesDirs.length; j++){

						if(playerRolesDirs[j].isDirectory()) {
							playerRole = playerRolesDirs[j].getName();

							// Create the cumulative speed stats files for the player
							for(int k = 0; k < columnHeaders.length; k++){

								//String acceptableHeader = columnHeaders[i].replaceAll(" ", "_");

								String acceptableHeader = headerToFile.get(columnHeaders[k]);

								if(acceptableHeader == null){
									acceptableHeader = columnHeaders[k];
								}

								StatsUtils.writeToFileMkParentDir(speedStatsFolderPath + "/" + playerType + "/" + acceptableHeader + "-AllMatches-" + playerRole + ".csv", "MatchID;#Samples;Min;Max;Median;SD;SEM;Avg;CI");

							}

							speedLogs = playerRolesDirs[j].listFiles();

							// For each stats file...
							for(int k = 0; k < speedLogs.length; k++){

								String[] splittedName = speedLogs[k].getName().split("\\.");

								// If it's a .csv file, compute and log the statistics
								if(!(splittedName[splittedName.length-1].equalsIgnoreCase("csv"))){
									System.out.println("Found file with no .csv extension when summarizing speed statistics.");
									rejectFile(speedLogs[k], rejectedSpeedFilesFolderPath + "/" + playerType + "/" + playerRole);
								}else{

									// If the stats are referring to a match that was rejected, reject them too

									if(!(acceptedMatches.containsKey(speedLogs[k].getName().substring(0, speedLogs[k].getName().length()-10)))){

										System.out.println("Found Speed Statistics file for a match that was previously rejected from statistics.");
										rejectFile(speedLogs[k], rejectedSpeedFilesFolderPath + "/" + playerType + "/" + playerRole);
									}else{

										extractor = new StatsExtractor(speedLogs[k], columnHeaders, columnTypes, columnIndices);

										Map<String, SingleValueStats> extractedStats = extractor.getExtractedStats();

										if(extractedStats == null){

											System.out.println("Error when computing speed statistics for the .csv file " + speedLogs[k].getName() + ".");
											System.out.println("Excluding file from statistics. NOTE THAT THE SPEED STATISTICS WON'T REFER TO THE WHOLE TOURNAMENT ANYMORE!");
											rejectFile(speedLogs[k], rejectedSpeedFilesFolderPath + "/" + playerType + "/" + playerRole);

										}else{

											boolean reject = false;

											List<SingleValueStats> allTheStats = new ArrayList<SingleValueStats>();

											// ...prepare an entry for each cumulative speed stats file
											for(int l = 0; l < columnHeaders.length; l++){

												SingleValueStats statsToWrite = extractedStats.get(columnHeaders[l]);

												if(statsToWrite == null){
													System.out.println("Error when computing speed statistics for the value " + columnHeaders[l] + " for the .csv file " + speedLogs[k].getName() + ".");
													reject = true;
													break;
												}else{

													allTheStats.add(statsToWrite);

												}
											}

											if(reject){
												System.out.println("Excluding file from statistics. NOTE THAT THE SPEED STATISTICS WON'T REFER TO THE WHOLE TOURNAMENT ANYMORE!");
												rejectFile(speedLogs[k], rejectedSpeedFilesFolderPath + "/" + playerType + "/" + playerRole);
											}else{

												summarizedFiles++;

												// ...write an entry in each cumulative speed stats file
												for(int l = 0; l < columnHeaders.length; l++){

													String acceptableHeader = headerToFile.get(columnHeaders[l]);

													if(acceptableHeader == null){
														acceptableHeader = columnHeaders[l];
													}

													SingleValueStats statsToWrite = allTheStats.get(l);

													statisticsValues[0] = statsToWrite.getNumSamples();
													statisticsValues[1] = statsToWrite.getMinValue();
													statisticsValues[2] = statsToWrite.getMaxValue();
													statisticsValues[3] = statsToWrite.getMedian();
													statisticsValues[4] = statsToWrite.getValuesStandardDeviation();
													statisticsValues[5] = statsToWrite.getValuesSEM();
													statisticsValues[6] = statsToWrite.getAvgValue();
													statisticsValues[7] = statsToWrite.get95ConfidenceInterval();

													StatsUtils.writeToFileMkParentDir(speedStatsFolderPath + "/" + playerType + "/" + acceptableHeader + "-AllMatches-" + playerRole + ".csv",
															speedLogs[k].getName().substring(0, speedLogs[k].getName().length()-10) + ";" + statisticsValues[0] +
															";" + statisticsValues[1] + ";" + statisticsValues[2] + ";" + statisticsValues[3] + ";" + statisticsValues[4] +
															";" + statisticsValues[5] +	";" + statisticsValues[6] + ";" + statisticsValues[7] + ";");

													// Add all values to the correct cumulative SingleValueStats
													addStatisticsValues(aggregatedStatistics, playerType + "-AllRoles", acceptableHeader, statisticsNames, statisticsValues);

													addStatisticsValues(aggregatedStatistics, playerType + "-" + playerRole, acceptableHeader, statisticsNames, statisticsValues);

												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			// Log all the aggregated statistics

			SingleValueDoubleStats theStatsToLog = null;

			for(Entry<String, Map<String, Map<String, SingleValueDoubleStats>>> playerRoleStats: aggregatedStatistics.entrySet()){

				for(Entry<String, Map<String, SingleValueDoubleStats>> statHeaderStats: playerRoleStats.getValue().entrySet()){

					StatsUtils.writeToFileMkParentDir(speedStatsFolderPath + "/" + playerRoleStats.getKey() + "-" + statHeaderStats.getKey() + "-AggrStats.csv", "StatType;#Samples;Min;Max;Median;SD;SEM;Avg;CI");

					for(int j = 0; j< statisticsNames.length; j++){
						theStatsToLog = statHeaderStats.getValue().get(statisticsNames[j]);

						if(theStatsToLog != null){
							StatsUtils.writeToFileMkParentDir(speedStatsFolderPath + "/" + playerRoleStats.getKey() + "-" + statHeaderStats.getKey() + "-AggrStats.csv", statisticsNames[j] +
									";" + theStatsToLog.getNumSamples() + ";" + theStatsToLog.getMinValue() + ";" + theStatsToLog.getMaxValue() + ";" +
									theStatsToLog.getMedian() + ";" + theStatsToLog.getValuesStandardDeviation() + ";" + theStatsToLog.getValuesSEM() +
									";" + theStatsToLog.getAvgValue() + ";" + theStatsToLog.get95ConfidenceInterval() + ";");
						}
					}
				}
			}

			//System.out.println();

			//System.out.println();

			//System.out.println("SummarizedFiles = " + summarizedFiles);

			//System.out.println("NumRoles = " + numRoles);

			//System.out.println("AcceptedMatchesSize = " + acceptedMatches.size());

			if(summarizedFiles != (numRoles*acceptedMatches.size())){

				System.out.println();

				System.out.println("SummarizedFiles = " + summarizedFiles);

				System.out.println("NumRoles = " + numRoles);

				System.out.println("AcceptedMatchesSize = " + acceptedMatches.size());

				System.out.println();

				System.out.println("!!! Unless you are testing external players, not all the Speed Stats files have been included in the Speed statistics due to some error. Speed stats are not completely clean!");

			}
		}else {
			//System.out.println("Impossible to find the speed logs directory to summarize: " + speedLogsFolder.getPath());
		}

		/****************** Compute tree size statistics of the matches that were considered in the previous statistics *******************/

		//System.out.println("TREE SIZE START");

		preprocessTreeStats(mainFolderPath, tourneyType, gameKey);


		String treeLogsFolderPath;
		if(simpleFolderFormat) {
			treeLogsFolderPath = mainFolderPath + "/TreeLogsEnd";
		}else{
			treeLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".TreeLogsEnd";
		}

		//System.out.println("treeLogsFolderPath= " + treeLogsFolderPath);

		File treeLogsFolder = new File(treeLogsFolderPath);

		if(treeLogsFolder.isDirectory()){
			// Create (or empty if it already exists) the folder where to move all the speed log files
			// that have been rejected and haven't been considered when computing the statistics.
			String rejectedTreeFilesFolderPath;
			if(simpleFolderFormat) {
				rejectedTreeFilesFolderPath = mainFolderPath + "/RejectedTreeFiles";
			}else{
				rejectedTreeFilesFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedTreeFiles";
			}

			//System.out.println("rejectedTreeFilesFolderPath= " + rejectedTreeFilesFolderPath);


			File rejectedTreeFilesFolder = new File(rejectedTreeFilesFolderPath);
			if(rejectedTreeFilesFolder.isDirectory()){
				if(!emptyFolder(rejectedTreeFilesFolder)){
					System.out.println("Summarization interrupted. Cannot empty the RejectedTreeFiles folder: " + rejectedTreeFilesFolder.getPath());
					return;
				}
			}else{
				if(!rejectedTreeFilesFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the RejectedTreeFiles folder: " + rejectedTreeFilesFolder.getPath());
					return;
				}
			}

			// Create (or empty if it already exists) the folder where to save all the speed statistics.
			String treeStatsFolderPath;
			if(simpleFolderFormat) {
				treeStatsFolderPath = mainFolderPath + "/TreeStatistics";
			}else{
				treeStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".TreeStatistics";
			}

			File treeStatsFolder = new File(treeStatsFolderPath);
			if(treeStatsFolder.isDirectory()){
				if(!emptyFolder(treeStatsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the TreeStatistics folder: " + treeStatsFolder.getPath());
					return;
				}
			}else{
				if(!treeStatsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the TreeStatistics folder: " + treeStatsFolder.getPath());
					return;
				}
			}

			String[] columnHeaders = new String[]{"#Nodes", "#ActionsStats", "#RAVE_AMAFStats", "#GRAVE_AMAFStats", "ActionsStats/Node", "RAVE_AMAFStats/Node", "GRAVE_AMAFStats/Node"};

			ColumnType[] columnTypes = new ColumnType[]{ColumnType.LONG, ColumnType.LONG, ColumnType.LONG, ColumnType.LONG, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE};

			int[] columnIndices = new int[]{2,3,4,5,6,7,8};

			File[] treeLogs;

			StatsExtractor extractor;

			// Statistics for which to compute the aggregated statistics
			String[] statisticsNames = new String[]{"numSamples","minValue","maxValue","median","sd","sem","avg","ci"};
			double[] statisticsValues = new double[8];

			// Prepare all the SingleValueStats that will compute the aggregated statistics for each player-role combination over all the matches
			Map<String, Map<String, Map<String, SingleValueDoubleStats>>> aggregatedStatistics = new HashMap<String, Map<String, Map<String, SingleValueDoubleStats>>>();

			// Iterate over the directories containing the matches logs for each player's type.
			File[] playerTypesDirs = treeLogsFolder.listFiles();

			// For the folder of each player type...
			for(int i = 0; i < playerTypesDirs.length; i++){

				if(playerTypesDirs[i].isDirectory()){

					String playerType = playerTypesDirs[i].getName();

					File[] playerRolesDirs = playerTypesDirs[i].listFiles();

					// Iterate over all the folder corresponding to the different roles the player played
					for(int j = 0; j < playerRolesDirs.length; j++){

						String playerRole = playerRolesDirs[j].getName();

						// Create the cumulative speed stats files for the player
						for(int k = 0; k < columnHeaders.length; k++){

							//String acceptableHeader = columnHeaders[i].replaceAll(" ", "_");

							String acceptableHeader = headerToFile.get(columnHeaders[k]);

							if(acceptableHeader == null){
								acceptableHeader = columnHeaders[k];
							}

							StatsUtils.writeToFileMkParentDir(treeStatsFolderPath + "/" + playerType + "/" + acceptableHeader + "-AllMatches-" + playerRole + ".csv", "MatchID;#Samples;Min;Max;Median;SD;SEM;Avg;CI");

						}

						treeLogs = playerRolesDirs[j].listFiles();

						// For each tree stats file...
						for(int k = 0; k < treeLogs.length; k++){

							String[] splittedName = treeLogs[k].getName().split("\\.");

							// If it's a .csv file, compute and log the statistics
							if(!(splittedName[splittedName.length-1].equalsIgnoreCase("csv"))){
								System.out.println("Found file with no .csv extension when summarizing tree statistics.");
								rejectFile(treeLogs[k], rejectedTreeFilesFolderPath + "/" + playerType + "/" + playerRole);
							}else{

								// If the stats are referring to a match that was rejected, reject them too

								if(!(acceptedMatches.containsKey(treeLogs[k].getName().substring(0, treeLogs[k].getName().length()-23)))){
									System.out.println("Found Tree Statistics file for a match that was previously rejected from statistics.");
									rejectFile(treeLogs[k], rejectedTreeFilesFolderPath + "/" + playerType + "/" + playerRole);
								}else{

									extractor = new StatsExtractor(treeLogs[k], columnHeaders, columnTypes, columnIndices);

									Map<String, SingleValueStats> extractedStats = extractor.getExtractedStats();

									if(extractedStats == null){

										System.out.println("Error when computing tree statistics for the .csv file " + treeLogs[k].getName() + ".");
										System.out.println("Excluding file from statistics. NOTE THAT THE TREE STATISTICS WON'T REFER TO THE WHOLE TOURNAMENT ANYMORE!");
										rejectFile(treeLogs[k], rejectedTreeFilesFolderPath + "/" + playerType + "/" + playerRole);

									}else{

										boolean reject = false;

										List<SingleValueStats> allTheStats = new ArrayList<SingleValueStats>();

										// ...prepare an entry for each cumulative speed stats file
										for(int l = 0; l < columnHeaders.length; l++){

											SingleValueStats statsToWrite = extractedStats.get(columnHeaders[l]);

											if(statsToWrite == null){
												System.out.println("Error when computing tree statistics for the value " + columnHeaders[l] + " for the .csv file " + treeLogs[k].getName() + ".");
												reject = true;
												break;
											}else{

												allTheStats.add(statsToWrite);

											}
										}

										if(reject){
											System.out.println("Excluding file from statistics. NOTE THAT THE TREE STATISTICS WON'T REFER TO THE WHOLE TOURNAMENT ANYMORE!");
											rejectFile(treeLogs[k], rejectedTreeFilesFolderPath + "/" + playerType + "/" + playerRole);
										}else{

											// ...write an entry in each cumulative speed stats file
											for(int l = 0; l < columnHeaders.length; l++){

												String acceptableHeader = headerToFile.get(columnHeaders[l]);

												if(acceptableHeader == null){
													acceptableHeader = columnHeaders[l];
												}

												SingleValueStats statsToWrite = allTheStats.get(l);

												statisticsValues[0] = statsToWrite.getNumSamples();
												statisticsValues[1] = statsToWrite.getMinValue();
												statisticsValues[2] = statsToWrite.getMaxValue();
												statisticsValues[3] = statsToWrite.getMedian();
												statisticsValues[4] = statsToWrite.getValuesStandardDeviation();
												statisticsValues[5] = statsToWrite.getValuesSEM();
												statisticsValues[6] = statsToWrite.getAvgValue();
												statisticsValues[7] = statsToWrite.get95ConfidenceInterval();

												StatsUtils.writeToFileMkParentDir(treeStatsFolderPath + "/" + playerType + "/" + acceptableHeader + "-AllMatches-" + playerRole + ".csv",
														treeLogs[k].getName().substring(0, treeLogs[k].getName().length()-10) + ";" + statisticsValues[0] +
														";" + statisticsValues[1] + ";" + statisticsValues[2] + ";" + statisticsValues[3] + ";" + statisticsValues[4] +
														";" + statisticsValues[5] +	";" + statisticsValues[6] + ";" + statisticsValues[7] + ";");

												// Add all values to the correct cumulative SingleValueStats
												addStatisticsValues(aggregatedStatistics, playerType + "-AllRoles", acceptableHeader, statisticsNames, statisticsValues);

												addStatisticsValues(aggregatedStatistics, playerType + "-" + playerRole, acceptableHeader, statisticsNames, statisticsValues);

											}
										}
									}
								}
							}
						}
					}
				}
			}

			// Log all the aggregated statistics

			SingleValueDoubleStats theStatsToLog = null;

			for(Entry<String, Map<String, Map<String, SingleValueDoubleStats>>> playerRoleStats: aggregatedStatistics.entrySet()){

				for(Entry<String, Map<String, SingleValueDoubleStats>> statHeaderStats: playerRoleStats.getValue().entrySet()){

					StatsUtils.writeToFileMkParentDir(treeStatsFolderPath + "/" + playerRoleStats.getKey() + "-" + statHeaderStats.getKey() + "-AggrStats.csv", "StatType;#Samples;Min;Max;Median;SD;SEM;Avg;CI");

					for(int j = 0; j< statisticsNames.length; j++){
						theStatsToLog = statHeaderStats.getValue().get(statisticsNames[j]);

						if(theStatsToLog != null){
							StatsUtils.writeToFileMkParentDir(treeStatsFolderPath + "/" + playerRoleStats.getKey() + "-" + statHeaderStats.getKey() + "-AggrStats.csv", statisticsNames[j] +
									";" + theStatsToLog.getNumSamples() + ";" + theStatsToLog.getMinValue() + ";" + theStatsToLog.getMaxValue() + ";" +
									theStatsToLog.getMedian() + ";" + theStatsToLog.getValuesStandardDeviation() + ";" + theStatsToLog.getValuesSEM() +
									";" + theStatsToLog.getAvgValue() + ";" + theStatsToLog.get95ConfidenceInterval() + ";");
						}
					}
				}
			}
		}else {
			//System.out.println("Impossible to find the tree logs directory to summarize: " + treeLogsFolder.getPath());
		}



		//System.out.println();

		//System.out.println();

		//System.out.println("SummarizedFiles = " + summarizedTreeFiles);

		//System.out.println("NumRoles = " + numRoles);

		//System.out.println("AcceptedMatchesSize = " + acceptedMatches.size());

		/****************** Compute parameters statistics of the matches that were considered in the previous statistics *******************/

		String paramLogsFolderPath;
		if(simpleFolderFormat) {
			paramLogsFolderPath = mainFolderPath + "/ParamsLogs";
		}else{
			paramLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".ParamsLogs";
		}

		//System.out.println("paramLogsFolderPath= " + paramLogsFolderPath);

		File paramLogsFolder = new File(paramLogsFolderPath);

		if(paramLogsFolder.isDirectory()){
			// Create (or empty if it already exists) the folder where to move all the speed log files
			// that have been rejected and haven't been considered when computing the statistics.
			String rejectedParamsFilesFolderPath;
			if(simpleFolderFormat) {
				rejectedParamsFilesFolderPath = mainFolderPath + "/RejectedParamsFiles";
			}else{
				rejectedParamsFilesFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedParamsFiles";
			}

			//System.out.println("rejectedFilesFolderPath= " + rejectedFilesFolderPath);


			File rejectedParamsFilesFolder = new File(rejectedParamsFilesFolderPath);
			if(rejectedParamsFilesFolder.isDirectory()){
				if(!emptyFolder(rejectedParamsFilesFolder)){
					System.out.println("Summarization interrupted. Cannot empty the RejectedParamsFiles folder: " + rejectedParamsFilesFolder.getPath());
					return;
				}
			}else{
				if(!rejectedParamsFilesFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the RejectedParamsFiles folder: " + rejectedParamsFilesFolder.getPath());
					return;
				}
			}

			// Create (or empty if it already exists) the folder where to save all the speed statistics.
			String paramsStatsFolderPath;
			if(simpleFolderFormat) {
				paramsStatsFolderPath = mainFolderPath + "/ParamsStatistics";
			}else{
				paramsStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".ParamsStatistics";
			}

			File paramsStatsFolder = new File(paramsStatsFolderPath);
			if(paramsStatsFolder.isDirectory()){
				if(!emptyFolder(paramsStatsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the ParamsStatistics folder: " + paramsStatsFolder.getPath());
					return;
				}
			}else{
				if(!paramsStatsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the ParamsStatistics folder: " + paramsStatsFolder.getPath());
					return;
				}
			}

			File[] paramsStatsFiles;

			String mabType;

			// Maps for parameters statistics

			Map<String,Map<String,Map<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>>>> aggregatedParamsStats = new HashMap<String,Map<String,Map<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>>>>();

			Map<String,Map<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>>> playerTypeMap;

			Map<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>> playerAllRolesMap;
			Map<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>> playerRoleMap;

			Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>> mabTypeMap;
			Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>> mabTypeMapAllRoles;

			// Maps for final best move choice statistics
			Map<String,Map<String,Map<String,Map<String,Map<String,ParamsComboInfo>>>>> aggregatedBestComboStats = new HashMap<String,Map<String,Map<String,Map<String,Map<String,ParamsComboInfo>>>>>();
			Map<String,Map<String,Map<String,Map<String,ParamsComboInfo>>>> playerTypeComboStatsMap; // <playerType,comboStatsPerPlayedRole>
			Map<String,Map<String,Map<String,ParamsComboInfo>>> playerRoleComboStatsMap; //<MyPlayedRole,comboStatsPerRoleInGame>
			Map<String,Map<String,Map<String,ParamsComboInfo>>> playerAllRolesComboStatsMap; // <AllRoles,comboStatsPerRoleInGame>

			// Iterate over the directories containing the matches logs for each player's type.
			File[] playerTypesDirs = paramLogsFolder.listFiles();

			// For the folder of each player type...
			for(int i = 0; i < playerTypesDirs.length; i++){

				if(playerTypesDirs[i].isDirectory()){

					String playerType = playerTypesDirs[i].getName();

					//System.out.println("Visiting folder of player type: " + playerType);

					// Get the map corresponding to this player type
					playerTypeMap = aggregatedParamsStats.get(playerType);
					if(playerTypeMap == null){
						playerTypeMap = new HashMap<String,Map<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>>>();
						aggregatedParamsStats.put(playerType, playerTypeMap);
					}
					// Get the comboStatsMap corresponding to this player type
					playerTypeComboStatsMap = aggregatedBestComboStats.get(playerType);
					if(playerTypeComboStatsMap == null){
						playerTypeComboStatsMap = new HashMap<String,Map<String,Map<String,Map<String,ParamsComboInfo>>>>();
						aggregatedBestComboStats.put(playerType, playerTypeComboStatsMap);
					}

					File[] playerRolesDirs = playerTypesDirs[i].listFiles();

					// Get the map for all the roles
					playerAllRolesMap = playerTypeMap.get("AllRoles");
					if(playerAllRolesMap == null){
						playerAllRolesMap = new HashMap<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>>();
						playerTypeMap.put("AllRoles", playerAllRolesMap);
					}
					// Get the comboStats for all the roles
					playerAllRolesComboStatsMap = playerTypeComboStatsMap.get("AllRoles");
					if(playerAllRolesComboStatsMap == null){
						playerAllRolesComboStatsMap = new HashMap<String,Map<String,Map<String,ParamsComboInfo>>>();
						playerTypeComboStatsMap.put("AllRoles", playerAllRolesComboStatsMap);
					}

					// Iterate over all the folders corresponding to the different roles the player played
					for(int j = 0; j < playerRolesDirs.length; j++){

						if(playerRolesDirs[j].isDirectory()){

							String playerRole = playerRolesDirs[j].getName();

							//System.out.println("Visiting folder of role: " + playerRole);

							// Get the map corresponding to the role played by the player
							playerRoleMap = playerTypeMap.get(playerRole);
							if(playerRoleMap == null){
								playerRoleMap = new HashMap<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>>();
								playerTypeMap.put(playerRole, playerRoleMap);
							}
							// Get the ComboStats map corresponding to the role played by the player
							playerRoleComboStatsMap = playerTypeComboStatsMap.get(playerRole);
							if(playerRoleComboStatsMap == null){
								playerRoleComboStatsMap = new HashMap<String,Map<String,Map<String,ParamsComboInfo>>>();
								playerTypeComboStatsMap.put(playerRole, playerRoleComboStatsMap);
							}

							paramsStatsFiles = playerRolesDirs[j].listFiles();

							// Iterate over all the params stats files
							for(int k = 0; k < paramsStatsFiles.length; k++){

								//System.out.println("Visiting file: " + paramsStatsFiles[k]);

								String[] splittedName = paramsStatsFiles[k].getName().split("\\.");

								// If it's a .csv file, compute and log the statistics
								if(!(splittedName[splittedName.length-1].equalsIgnoreCase("csv"))){
									System.out.println("Found file with no .csv extension when summarizing params statistics.");
									rejectFile(paramsStatsFiles[k], rejectedParamsFilesFolderPath + "/" + playerType + "/" + playerRole);
								}else{

									// If the stats are referring to a match that was rejected, reject them too

									if(!(acceptedMatches.containsKey(paramsStatsFiles[k].getName().substring(0, paramsStatsFiles[k].getName().length()-26))) &&
											!(acceptedMatches.containsKey(paramsStatsFiles[k].getName().substring(0, paramsStatsFiles[k].getName().length()-25))) &&
											!(acceptedMatches.containsKey(paramsStatsFiles[k].getName().substring(0, paramsStatsFiles[k].getName().length()-20)))){
										System.out.println("Found Params Statistics file for a match that was previously rejected from statistics.");
										rejectFile(paramsStatsFiles[k], rejectedParamsFilesFolderPath + "/" + playerType + "/" + playerRole);
									}else{

										if(paramsStatsFiles[k].getName().endsWith("GlobalParamTunerStats.csv")){
											mabType = "Global";
										}else if(paramsStatsFiles[k].getName().endsWith("LocalParamTunerStats.csv")){
											mabType = "Local";
										}else if(paramsStatsFiles[k].getName().endsWith("BestParamsCombo.csv")){
											summarizeBestCombos(paramsStatsFiles[k],
													acceptedMatches.get(paramsStatsFiles[k].getName().substring(0, paramsStatsFiles[k].getName().length()-20)),
													playerType, playerRole,
													playerRoleComboStatsMap, playerAllRolesComboStatsMap);
											continue;
										}else{
											System.out.println("Unrecognized type of parameters stats. Skipping file: " + paramsStatsFiles[k].getPath());
											continue;
										}

										mabTypeMap = playerRoleMap.get(mabType);
										if(mabTypeMap == null){
											mabTypeMap = new HashMap<String,Map<String,Map<String,Map<String,SingleValueStats>>>>();
											playerRoleMap.put(mabType, mabTypeMap);
										}
										mabTypeMapAllRoles = playerAllRolesMap.get(mabType);
										if(mabTypeMapAllRoles == null){
											mabTypeMapAllRoles = new HashMap<String,Map<String,Map<String,Map<String,SingleValueStats>>>>();
											playerAllRolesMap.put(mabType, mabTypeMapAllRoles);
										}

										//System.out.println("Extracting stats");
										extractParamsStats(paramsStatsFiles[k], mabType, mabTypeMap, mabTypeMapAllRoles);
									}
								}
							}
						}
					}
				}
			}

			// Log all the aggregated statistics
			for(Entry<String,Map<String,Map<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>>>> playerTypeStats: aggregatedParamsStats.entrySet()){

				for(Entry<String,Map<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>>> playerRoleStats: playerTypeStats.getValue().entrySet()){

					for(Entry<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>> mabTypeStats: playerRoleStats.getValue().entrySet()){

						for(Entry<String,Map<String,Map<String,Map<String,SingleValueStats>>>> roleStats: mabTypeStats.getValue().entrySet()){

							StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
									"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "ROLE = " + roleStats.getKey());
							StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
									"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "");

							for(Entry<String,Map<String,Map<String,SingleValueStats>>> parameterStats: roleStats.getValue().entrySet()){

								StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
										"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "PARAM = " + parameterStats.getKey());
								StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
										"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "VALUE;TOTAL_VISITS;AVG_VISITS;AVG_SCORE_SUM;AVG_AVG_VALUE;AVG_PENALTY;NUM_SAMPLES;");

								for(Entry<String,Map<String,SingleValueStats>> parameterValueStats: parameterStats.getValue().entrySet()){

									int numSamples = -1;

									// Check that all stats have the same number of samples. If not, something went wrong so we log -1 as number of samples.
									for(Entry<String,SingleValueStats> stat: parameterValueStats.getValue().entrySet()){
										if(numSamples == -1){
											numSamples = stat.getValue().getNumSamples();
										}else if(numSamples != stat.getValue().getNumSamples()){
											numSamples = -1;
											break;
										}
									}

									String statisToLog = parameterValueStats.getKey() + ";" +
											((SingleValueLongStats)parameterValueStats.getValue().get("VISITS=")).getTotalSum() + ";" +
											parameterValueStats.getValue().get("VISITS=").getAvgValue() + ";" +
											parameterValueStats.getValue().get("SCORE_SUM=").getAvgValue() + ";" +
											parameterValueStats.getValue().get("AVG_VALUE=").getAvgValue() + ";" +
											parameterValueStats.getValue().get("PENALTY=").getAvgValue() + ";" +
											numSamples + ";";


									StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
											"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", statisToLog);

								}

								StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
										"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "");

							}

							StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
									"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "");

						}
					}
				}
			}

			// Log all the best combos statistics
			// TODO: add number of wins!!!!!
			for(Entry<String,Map<String,Map<String,Map<String,Map<String,ParamsComboInfo>>>>> playerTypeComboStats : aggregatedBestComboStats.entrySet()){
				for(Entry<String,Map<String,Map<String,Map<String,ParamsComboInfo>>>> playerRoleComboStats : playerTypeComboStats.getValue().entrySet()){
					for(Entry<String,Map<String,Map<String,ParamsComboInfo>>> roleCombosStats : playerRoleComboStats.getValue().entrySet()){

						StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeComboStats.getKey() + "-" + playerRoleComboStats.getKey() +
								"-BestParamsCombo-AggrStats.csv", "");
						StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeComboStats.getKey() + "-" + playerRoleComboStats.getKey() +
								"-BestParamsCombo-AggrStats.csv", "ROLE = " + roleCombosStats.getKey());

						for(Entry<String,Map<String,ParamsComboInfo>> paramCombosStats : roleCombosStats.getValue().entrySet()){

							StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeComboStats.getKey() + "-" + playerRoleComboStats.getKey() +
									"-BestParamsCombo-AggrStats.csv", "PARAMS = " + paramCombosStats.getKey() + ";NUM_COMMITS;NUM_WINS;NUM_TIES;NUM_LOSSES;");

							for(Entry<String,ParamsComboInfo> comboStat : paramCombosStats.getValue().entrySet()){
								StatsUtils.writeToFileMkParentDir(paramsStatsFolderPath + "/" + playerTypeComboStats.getKey() + "-" + playerRoleComboStats.getKey() +
										"-BestParamsCombo-AggrStats.csv", comboStat.getKey() + ";" + comboStat.getValue().getNumCommits() + ";" +
										comboStat.getValue().getNumWins() + ";" + comboStat.getValue().getNumTies() + ";" +
										comboStat.getValue().getNumLosses() + ";");
							}
						}
					}
				}
			}
		}else {
			//System.out.println("Impossible to find the params logs directory to summarize: " + paramLogsFolder.getPath());
		}

		/****************** Compute tuner samples statistics of the matches that were considered in the previous statistics *******************/

		//System.out.println("TUNER SMAPLES START");

		String tunerSamplesLogsFolderPath;
		if(simpleFolderFormat) {
			tunerSamplesLogsFolderPath = mainFolderPath + "/TunerSamplesLogs";
		}else{
			tunerSamplesLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".TunerSamplesLogs";
		}

		File tunerSamplesLogsFolder = new File(tunerSamplesLogsFolderPath);

		if(tunerSamplesLogsFolder.isDirectory()){
			// Create (or empty if it already exists) the folder where to move all the speed log files
			// that have been rejected and haven't been considered when computing the statistics.
			String rejectedTunerSamplesFilesFolderPath;
			if(simpleFolderFormat) {
				rejectedTunerSamplesFilesFolderPath = mainFolderPath + "/RejectedTunerSamplesFiles";
			}else{
				rejectedTunerSamplesFilesFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedTunerSamplesFiles";
			}

			File rejectedTunerSamplesFilesFolder = new File(rejectedTunerSamplesFilesFolderPath);
			if(rejectedTunerSamplesFilesFolder.isDirectory()){
				if(!emptyFolder(rejectedTunerSamplesFilesFolder)){
					System.out.println("Summarization interrupted. Cannot empty the RejectedTunerSamplesFilesFolder folder: " + rejectedTunerSamplesFilesFolder.getPath());
					return;
				}
			}else{
				if(!rejectedTunerSamplesFilesFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the RejectedTunerSamplesFilesFolder folder: " + rejectedTunerSamplesFilesFolder.getPath());
					return;
				}
			}

			// Create (or empty if it already exists) the folder where to save all the speed statistics.
			String tunerSamplesStatsFolderPath;
			if(simpleFolderFormat) {
				tunerSamplesStatsFolderPath = mainFolderPath + "/TunerSamplesStatistics";
			}else{
				tunerSamplesStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".TunerSamplesStatistics";
			}

			File tunerSamplesStatsFolder = new File(tunerSamplesStatsFolderPath);
			if(tunerSamplesStatsFolder.isDirectory()){
				if(!emptyFolder(tunerSamplesStatsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the TunerSamplesStatistics folder: " + tunerSamplesStatsFolder.getPath());
					return;
				}
			}else{
				if(!tunerSamplesStatsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the TunerSamplesStatistics folder: " + tunerSamplesStatsFolder.getPath());
					return;
				}
			}

			Map<String,Map<String,StatsFileData>> samplesEstimates = new HashMap<String,Map<String,StatsFileData>>();
			Map<String,Map<String,StatsFileData>> samplesUsagePerRole = new HashMap<String,Map<String,StatsFileData>>();

			Map<String,StatsFileData> samplesEstimateForPlayerType;
			Map<String,StatsFileData> samplesUsagePerRoleForPlayerType;

			StatsFileData samplesEstimateForPlayerTypeForRole;
			StatsFileData samplesUsagePerRoleForPlayerTypeForRole;

			File[] tunerSamplesStatsFiles;

			// Iterate over the directories containing the matches logs for each player's type.
			File[] playerTypesDirs = tunerSamplesLogsFolder.listFiles();

			// For the folder of each player type...
			for(int i = 0; i < playerTypesDirs.length; i++){

				if(playerTypesDirs[i].isDirectory()){

					String playerType = playerTypesDirs[i].getName();

					//System.out.println("Visiting folder of player type: " + playerType);

					File[] playerRolesDirs = playerTypesDirs[i].listFiles();

					// Iterate over all the folders corresponding to the different roles the player played
					for(int j = 0; j < playerRolesDirs.length; j++){

						if(playerRolesDirs[j].isDirectory()){

							String playerRole = playerRolesDirs[j].getName();

							//System.out.println("Visiting folder of role: " + playerRole);

							tunerSamplesStatsFiles = playerRolesDirs[j].listFiles();

							// Iterate over all the tuner samples stats files
							for(int k = 0; k < tunerSamplesStatsFiles.length; k++){

								String[] splittedName = tunerSamplesStatsFiles[k].getName().split("\\.");

								// If it's a .csv file, compute and log the statistics
								if(!(splittedName[splittedName.length-1].equalsIgnoreCase("csv"))){
									System.out.println("Found file with no .csv extension when summarizing tuner samples statistics.");
									rejectFile(tunerSamplesStatsFiles[k], rejectedTunerSamplesFilesFolderPath + "/" + playerType + "/" + playerRole);
								}else{

									// If the stats are referring to a match that was rejected, reject them too

									StatsFileData theCurrentStatsFileData = new StatsFileData(tunerSamplesStatsFiles[k]);

									if(!(acceptedMatches.containsKey(theCurrentStatsFileData.getMatchID()))){
										System.out.println("Found Tuner Samples Statistics file for a match that was previously rejected from statistics.");
										rejectFile(tunerSamplesStatsFiles[k], rejectedTunerSamplesFilesFolderPath + "/" + playerType + "/" + playerRole);
									}else{
										if(tunerSamplesStatsFiles[k].getName().endsWith("SamplesEstimates.csv")){
											// Get the SamplesEstimates map corresponding to this player type
											samplesEstimateForPlayerType = samplesEstimates.get(playerType);
											if(samplesEstimateForPlayerType == null){
												samplesEstimateForPlayerType = new HashMap<String,StatsFileData>();
												samplesEstimates.put(playerType, samplesEstimateForPlayerType);
											}
											// Get the StatsFileData corresponding to the role played by the player
											samplesEstimateForPlayerTypeForRole = samplesEstimateForPlayerType.get(playerRole);
											if(samplesEstimateForPlayerTypeForRole == null){
												samplesEstimateForPlayerTypeForRole = new StatsFileData(theCurrentStatsFileData.getFileHeader());
												for(String line : theCurrentStatsFileData.getFileLines()) {
													samplesEstimateForPlayerTypeForRole.addLine(theCurrentStatsFileData.getMatchID() + ";" + line);
												}
												samplesEstimateForPlayerType.put(playerRole, samplesEstimateForPlayerTypeForRole);
											}else {
												if(theCurrentStatsFileData.getFileHeader().equals(samplesEstimateForPlayerTypeForRole.getFileHeader())) {
													for(String line : theCurrentStatsFileData.getFileLines()) {
														samplesEstimateForPlayerTypeForRole.addLine(theCurrentStatsFileData.getMatchID() + ";" + line);
													}
												}else {
													System.out.println("Wrong header for tuner samples stats: " + theCurrentStatsFileData.getFileHeader() + ". Skipping file: " + tunerSamplesStatsFiles[k].getPath());
													continue;
												}
											}
										}else if(tunerSamplesStatsFiles[k].getName().endsWith("SamplesUsagePerRole.csv")){
											// Get the SamplesUsagePerRole map corresponding to this player type
											samplesUsagePerRoleForPlayerType = samplesUsagePerRole.get(playerType);
											if(samplesUsagePerRoleForPlayerType == null){
												samplesUsagePerRoleForPlayerType = new HashMap<String,StatsFileData>();
												samplesUsagePerRole.put(playerType, samplesUsagePerRoleForPlayerType);
											}
											// Get the StatsFileData corresponding to the role played by the player
											samplesUsagePerRoleForPlayerTypeForRole = samplesUsagePerRoleForPlayerType.get(playerRole);
											if(samplesUsagePerRoleForPlayerTypeForRole == null){
												samplesUsagePerRoleForPlayerTypeForRole = new StatsFileData(theCurrentStatsFileData.getFileHeader());
												for(String line : theCurrentStatsFileData.getFileLines()) {
													samplesUsagePerRoleForPlayerTypeForRole.addLine(theCurrentStatsFileData.getMatchID() + ";" + line);
												}
												samplesUsagePerRoleForPlayerType.put(playerRole, samplesUsagePerRoleForPlayerTypeForRole);
											}else {
												if(theCurrentStatsFileData.getFileHeader().equals(samplesUsagePerRoleForPlayerTypeForRole.getFileHeader())) {
													for(String line : theCurrentStatsFileData.getFileLines()) {
														samplesUsagePerRoleForPlayerTypeForRole.addLine(theCurrentStatsFileData.getMatchID() + ";" + line);
													}
												}else {
													System.out.println("Wrong header for tuner samples stats: " + theCurrentStatsFileData.getFileHeader() + ". Skipping file: " + tunerSamplesStatsFiles[k].getPath());
													continue;
												}
											}
										}else{
											System.out.println("Unrecognized type of tuner samples stats. Skipping file: " + tunerSamplesStatsFiles[k].getPath());
											continue;
										}
									}
								}
							}
						}
					}
				}
			}

			for(Entry<String,Map<String,StatsFileData>> samplesEstimatesForPlayer: samplesEstimates.entrySet()) {
				String playerType = samplesEstimatesForPlayer.getKey();
				for(Entry<String,StatsFileData> samplesEstimatesForPlayerForRole: samplesEstimatesForPlayer.getValue().entrySet()) {
					String playerRole = samplesEstimatesForPlayerForRole.getKey();
					StatsUtils.writeToFileMkParentDir(tunerSamplesStatsFolderPath + "/" + playerType + "-" + playerRole + "-SamplesEstimates.csv", samplesEstimatesForPlayerForRole.getValue().toLogs());
				}
			}

			for(Entry<String,Map<String,StatsFileData>> samplesUsagePerRoleForPlayer: samplesUsagePerRole.entrySet()) {
				String playerType = samplesUsagePerRoleForPlayer.getKey();
				for(Entry<String,StatsFileData> samplesUsagePerRoleForPlayerForRole: samplesUsagePerRoleForPlayer.getValue().entrySet()) {
					String playerRole = samplesUsagePerRoleForPlayerForRole.getKey();
					StatsUtils.writeToFileMkParentDir(tunerSamplesStatsFolderPath + "/" + playerType + "-" + playerRole + "-SamplesUsagePerRole.csv", samplesUsagePerRoleForPlayerForRole.getValue().toLogs());
				}
			}

			//System.out.println();

			//System.out.println();

			//System.out.println("SummarizedFiles = " + summarizedTreeFiles);

			//System.out.println("NumRoles = " + numRoles);

			//System.out.println("AcceptedMatchesSize = " + acceptedMatches.size());
		}else {
			//System.out.println("Impossible to find the tuner samples logs directory to summarize: " + tunerSamplesLogsFolder.getPath());
		}


		/****************** Compute branching statistics of the matches that were considered in the previous statistics *******************/

		String branchingLogsFolderPath;

		if(simpleFolderFormat) {
			branchingLogsFolderPath = mainFolderPath + "/BranchingLogs";
		}else{
			branchingLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".BranchingLogs";
		}

		//System.out.println("speedLogsFolderPath= " + speedLogsFolderPath);

		File branchingLogsFolder = new File(branchingLogsFolderPath);

		if(branchingLogsFolder.isDirectory()){
			// Create (or empty if it already exists) the folder where to move all the speed log files
			// that have been rejected and haven't been considered when computing the statistics.
			String rejectedBranchingLogsFolderPath;
			if(simpleFolderFormat){
				rejectedBranchingLogsFolderPath = mainFolderPath + "/RejectedBranchingLogs";
			}else{
				rejectedBranchingLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedBranchingLogs";
			}

			//System.out.println("RejectedSpeedFilesFolderPath= " + rejectedSpeedFilesFolderPath);


			File rejectedBranchingLogsFolder = new File(rejectedBranchingLogsFolderPath);
			if(rejectedBranchingLogsFolder.isDirectory()){
				if(!emptyFolder(rejectedBranchingLogsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the RejectedBranchingLogs folder: " + rejectedBranchingLogsFolder.getPath());
					return;
				}
			}else{
				if(!rejectedBranchingLogsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the RejectedBranchingLogs folder: " + rejectedBranchingLogsFolder.getPath());
					return;
				}
			}

			// Create (or empty if it already exists) the folder where to save all the speed statistics.
			String branchingStatsFolderPath;
			if(simpleFolderFormat){
				branchingStatsFolderPath = mainFolderPath + "/BranchingStatistics";
			}else{
				branchingStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".BranchingStatistics";
			}
			File branchingStatsFolder = new File(branchingStatsFolderPath);
			if(branchingStatsFolder.isDirectory()){
				if(!emptyFolder(branchingStatsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the BranchingStatistics folder: " + branchingStatsFolder.getPath());
					return;
				}
			}else{
				if(!branchingStatsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the BranchingStatistics folder: " + branchingStatsFolder.getPath());
					return;
				}
			}

			//List<String> acceptedMatches = new ArrayList<String>();

			File[] playerTypesDirs;

			File[] playerRolesDirs = null;

			File[] branchingLogs = null;

			//File randomSpeedLog;

			String playerType;

			String playerRole;

			// Prepare all the SingleValueStats that will compute the aggregated statistics for each player-role combination over all the matches
			Map<String, Map<String, Map<String, Map<String, SingleValueDoubleStats>>>> aggregatedBranchingStatistics =
					new HashMap<String, Map<String, Map<String, Map<String, SingleValueDoubleStats>>>>();

			Map<String, Map<String, Map<String, SingleValueDoubleStats>>> playerTypeBranchingMap =
					new HashMap<String, Map<String, Map<String, SingleValueDoubleStats>>>();

			Map<String, Map<String, SingleValueDoubleStats>> roleBranchingMap =
					new HashMap<String, Map<String, SingleValueDoubleStats>>();
			Map<String, Map<String, SingleValueDoubleStats>> allRoleBranchingMap =
					new HashMap<String, Map<String, SingleValueDoubleStats>>();

			// Iterate over the directories containing the matches logs for each player's type.
			playerTypesDirs = branchingLogsFolder.listFiles();

			// For the folder of each player type...
			for(int i = 0; i < playerTypesDirs.length; i++){

				if(playerTypesDirs[i].isDirectory()){

					playerType = playerTypesDirs[i].getName();

					// Get the map corresponding to this player type
					playerTypeBranchingMap = aggregatedBranchingStatistics.get(playerType);
					if(playerTypeBranchingMap == null){
						playerTypeBranchingMap = new HashMap<String, Map<String, Map<String, SingleValueDoubleStats>>>();
						aggregatedBranchingStatistics.put(playerType, playerTypeBranchingMap);
					}

					playerRolesDirs = playerTypesDirs[i].listFiles();

					// Iterate over all the folders corresponding to the different roles the player played
					for(int j = 0; j < playerRolesDirs.length; j++){

						if(playerRolesDirs[j].isDirectory()) {
							playerRole = playerRolesDirs[j].getName();

							// Get the map corresponding to this role
							roleBranchingMap = playerTypeBranchingMap.get(playerRole);
							if(roleBranchingMap == null){
								roleBranchingMap =  new HashMap<String, Map<String, SingleValueDoubleStats>>();
								playerTypeBranchingMap.put(playerRole, roleBranchingMap);
							}
							// Get the map corresponding to all roles
							allRoleBranchingMap = playerTypeBranchingMap.get("AllRoles");
							if(allRoleBranchingMap == null){
								allRoleBranchingMap =  new HashMap<String, Map<String, SingleValueDoubleStats>>();
								playerTypeBranchingMap.put("AllRoles", allRoleBranchingMap);
							}

							branchingLogs = playerRolesDirs[j].listFiles();

							// For each stats file...
							for(int k = 0; k < branchingLogs.length; k++){

								String[] splittedName = branchingLogs[k].getName().split("\\.");

								// If it's a .csv file, compute and log the statistics
								if(!(splittedName[splittedName.length-1].equalsIgnoreCase("csv"))){
									System.out.println("Found file with no .csv extension when summarizing branching statistics.");
									rejectFile(branchingLogs[k], rejectedBranchingLogsFolder + "/" + playerType + "/" + playerRole);
								}else{

									// If the stats are referring to a match that was rejected, reject them too

									if(!(acceptedMatches.containsKey(branchingLogs[k].getName().substring(0, branchingLogs[k].getName().length()-18)))){

										System.out.println("Found Branching Statistics file for a match that was previously rejected from statistics.");
										rejectFile(branchingLogs[k], rejectedBranchingLogsFolder + "/" + playerType + "/" + playerRole);
									}else{

										extractBranchingStats(branchingLogs[k], allRoleBranchingMap, roleBranchingMap);

									}
								}
							}
						}
					}
				}
			}

			// Log all the aggregated statistics

			for(Entry<String, Map<String, Map<String, Map<String, SingleValueDoubleStats>>>> playerTypeStats: aggregatedBranchingStatistics.entrySet()){

				for(Entry<String, Map<String, Map<String, SingleValueDoubleStats>>> roleStats: playerTypeStats.getValue().entrySet()){

					for(Entry<String, Map<String, SingleValueDoubleStats>> stepStats: roleStats.getValue().entrySet()){

						StatsUtils.writeToFileMkParentDir(branchingStatsFolderPath + "/" + playerTypeStats.getKey() + "/" + roleStats.getKey() + "/" +
								"Step" + stepStats.getKey() + "-Branching-AggrStats.csv", "StatType;#Samples;Min;Max;Median;SD;SEM;Avg;CI;");

						for(Entry<String, SingleValueDoubleStats> statTypeStats: stepStats.getValue().entrySet()){

							String toLog = statTypeStats.getKey() + ";" + statTypeStats.getValue().getNumSamples() + ";" +
									statTypeStats.getValue().getMinValue() + ";" + statTypeStats.getValue().getMaxValue() + ";" +
									statTypeStats.getValue().getMedian() + ";" + statTypeStats.getValue().getValuesStandardDeviation() + ";" +
									statTypeStats.getValue().getValuesSEM() + ";" + statTypeStats.getValue().getAvgValue() + ";" +
									statTypeStats.getValue().get95ConfidenceInterval() + ";";

							StatsUtils.writeToFileMkParentDir(branchingStatsFolderPath + "/" + playerTypeStats.getKey() + "/" + roleStats.getKey() + "/" +
									"Step" + stepStats.getKey() + "-Branching-AggrStats.csv", toLog);

						}

					}

				}

			}

		}else {
			//System.out.println("Impossible to find the speed logs directory to summarize: " + speedLogsFolder.getPath());
		}

		/****************** Compute depth statistics of the matches that were considered in the previous statistics *******************/

		String depthLogsFolderPath;

		if(simpleFolderFormat) {
			depthLogsFolderPath = mainFolderPath + "/DepthLogs";
		}else{
			depthLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".DepthLogs";
		}

		//System.out.println("speedLogsFolderPath= " + speedLogsFolderPath);

		File depthLogsFolder = new File(depthLogsFolderPath);

		if(depthLogsFolder.isDirectory()){
			// Create (or empty if it already exists) the folder where to move all the speed log files
			// that have been rejected and haven't been considered when computing the statistics.
			String rejectedDepthLogsFolderPath;
			if(simpleFolderFormat){
				rejectedDepthLogsFolderPath = mainFolderPath + "/RejectedDepthLogs";
			}else{
				rejectedDepthLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedDepthLogs";
			}

			//System.out.println("RejectedSpeedFilesFolderPath= " + rejectedSpeedFilesFolderPath);


			File rejectedDepthLogsFolder = new File(rejectedDepthLogsFolderPath);
			if(rejectedDepthLogsFolder.isDirectory()){
				if(!emptyFolder(rejectedDepthLogsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the RejectedDepthLogs folder: " + rejectedDepthLogsFolder.getPath());
					return;
				}
			}else{
				if(!rejectedDepthLogsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the RejectedDepthLogs folder: " + rejectedDepthLogsFolder.getPath());
					return;
				}
			}

			// Create (or empty if it already exists) the folder where to save all the speed statistics.
			String depthStatsFolderPath;
			if(simpleFolderFormat){
				depthStatsFolderPath = mainFolderPath + "/DepthStatistics";
			}else{
				depthStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".DepthStatistics";
			}
			File depthStatsFolder = new File(depthStatsFolderPath);
			if(depthStatsFolder.isDirectory()){
				if(!emptyFolder(depthStatsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the DepthStatistics folder: " + depthStatsFolder.getPath());
					return;
				}
			}else{
				if(!depthStatsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the DepthStatistics folder: " + depthStatsFolder.getPath());
					return;
				}
			}

			//List<String> acceptedMatches = new ArrayList<String>();

			File[] playerTypesDirs;

			File[] playerRolesDirs = null;

			File[] depthLogs = null;

			//File randomSpeedLog;

			String playerType;

			String playerRole;

			// Prepare all the SingleValueStats that will compute the aggregated statistics for each player-role combination over all the matches
			Map<String, Map<String, Map<String, Map<String, SingleValueDoubleStats>>>> aggregatedDepthStatistics =
					new HashMap<String, Map<String, Map<String, Map<String, SingleValueDoubleStats>>>>();

			Map<String, Map<String, Map<String, SingleValueDoubleStats>>> playerTypeDepthMap =
					new HashMap<String, Map<String, Map<String, SingleValueDoubleStats>>>();

			Map<String, Map<String, SingleValueDoubleStats>> roleDepthMap =
					new HashMap<String, Map<String, SingleValueDoubleStats>>();
			Map<String, Map<String, SingleValueDoubleStats>> allRoleDepthMap =
					new HashMap<String, Map<String, SingleValueDoubleStats>>();

			// Iterate over the directories containing the matches logs for each player's type.
			playerTypesDirs = depthLogsFolder.listFiles();

			// For the folder of each player type...
			for(int i = 0; i < playerTypesDirs.length; i++){

				if(playerTypesDirs[i].isDirectory()){

					playerType = playerTypesDirs[i].getName();

					// Get the map corresponding to this player type
					playerTypeDepthMap = aggregatedDepthStatistics.get(playerType);
					if(playerTypeDepthMap == null){
						playerTypeDepthMap = new HashMap<String, Map<String, Map<String, SingleValueDoubleStats>>>();
						aggregatedDepthStatistics.put(playerType, playerTypeDepthMap);
					}

					playerRolesDirs = playerTypesDirs[i].listFiles();

					// Iterate over all the folders corresponding to the different roles the player played
					for(int j = 0; j < playerRolesDirs.length; j++){

						if(playerRolesDirs[j].isDirectory()) {
							playerRole = playerRolesDirs[j].getName();

							// Get the map corresponding to this role
							roleDepthMap = playerTypeDepthMap.get(playerRole);
							if(roleDepthMap == null){
								roleDepthMap =  new HashMap<String, Map<String, SingleValueDoubleStats>>();
								playerTypeDepthMap.put(playerRole, roleDepthMap);
							}
							// Get the map corresponding to all roles
							allRoleDepthMap = playerTypeDepthMap.get("AllRoles");
							if(allRoleDepthMap == null){
								allRoleDepthMap =  new HashMap<String, Map<String, SingleValueDoubleStats>>();
								playerTypeDepthMap.put("AllRoles", allRoleDepthMap);
							}

							depthLogs = playerRolesDirs[j].listFiles();

							// For each stats file...
							for(int k = 0; k < depthLogs.length; k++){

								String[] splittedName = depthLogs[k].getName().split("\\.");

								// If it's a .csv file, compute and log the statistics
								if(!(splittedName[splittedName.length-1].equalsIgnoreCase("csv"))){
									System.out.println("Found file with no .csv extension when summarizing depth statistics.");
									rejectFile(depthLogs[k], rejectedDepthLogsFolder + "/" + playerType + "/" + playerRole);
								}else{

									// If the stats are referring to a match that was rejected, reject them too

									if(!(acceptedMatches.containsKey(depthLogs[k].getName().substring(0, depthLogs[k].getName().length()-14)))){

										System.out.println("Found Depth Statistics file for a match that was previously rejected from statistics.");
										rejectFile(depthLogs[k], rejectedDepthLogsFolder + "/" + playerType + "/" + playerRole);
									}else{

										extractDepthStats(depthLogs[k], allRoleDepthMap, roleDepthMap);

									}
								}
							}
						}
					}
				}
			}

			// Log all the aggregated statistics

			for(Entry<String, Map<String, Map<String, Map<String, SingleValueDoubleStats>>>> playerTypeStats: aggregatedDepthStatistics.entrySet()){

				for(Entry<String, Map<String, Map<String, SingleValueDoubleStats>>> roleStats: playerTypeStats.getValue().entrySet()){

					for(Entry<String, Map<String, SingleValueDoubleStats>> stepStats: roleStats.getValue().entrySet()){

						StatsUtils.writeToFileMkParentDir(depthStatsFolderPath + "/" + playerTypeStats.getKey() + "/" + roleStats.getKey() + "/" +
								"Step" + stepStats.getKey() + "-Depth-AggrStats.csv", "StatType;#Samples;Min;Max;Median;SD;SEM;Avg;CI;");

						for(Entry<String, SingleValueDoubleStats> statTypeStats: stepStats.getValue().entrySet()){

							String toLog = statTypeStats.getKey() + ";" + statTypeStats.getValue().getNumSamples() + ";" +
									statTypeStats.getValue().getMinValue() + ";" + statTypeStats.getValue().getMaxValue() + ";" +
									statTypeStats.getValue().getMedian() + ";" + statTypeStats.getValue().getValuesStandardDeviation() + ";" +
									statTypeStats.getValue().getValuesSEM() + ";" + statTypeStats.getValue().getAvgValue() + ";" +
									statTypeStats.getValue().get95ConfidenceInterval() + ";";

							StatsUtils.writeToFileMkParentDir(depthStatsFolderPath + "/" + playerTypeStats.getKey() + "/" + roleStats.getKey() + "/" +
									"Step" + stepStats.getKey() + "-Depth-AggrStats.csv", toLog);

						}

					}

				}

			}

		}else {
			//System.out.println("Impossible to find the speed logs directory to summarize: " + speedLogsFolder.getPath());
		}

		/****************** Compute entropy statistics of the matches that were considered in the previous statistics *******************/

		String entropyLogsFolderPath;

		if(simpleFolderFormat) {
			entropyLogsFolderPath = mainFolderPath + "/EntropyLogs";
		}else{
			entropyLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".EntropyLogs";
		}

		//System.out.println("speedLogsFolderPath= " + speedLogsFolderPath);

		File entropyLogsFolder = new File(entropyLogsFolderPath);

		if(entropyLogsFolder.isDirectory()){
			// Create (or empty if it already exists) the folder where to move all the speed log files
			// that have been rejected and haven't been considered when computing the statistics.
			String rejectedEntropyLogsFolderPath;
			if(simpleFolderFormat){
				rejectedEntropyLogsFolderPath = mainFolderPath + "/RejectedEntropyLogs";
			}else{
				rejectedEntropyLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedEntropyLogs";
			}

			//System.out.println("RejectedSpeedFilesFolderPath= " + rejectedSpeedFilesFolderPath);


			File rejectedEntropyLogsFolder = new File(rejectedEntropyLogsFolderPath);
			if(rejectedEntropyLogsFolder.isDirectory()){
				if(!emptyFolder(rejectedEntropyLogsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the RejectedEntropyLogs folder: " + rejectedEntropyLogsFolder.getPath());
					return;
				}
			}else{
				if(!rejectedEntropyLogsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the RejectedEntropyLogs folder: " + rejectedEntropyLogsFolder.getPath());
					return;
				}
			}

			// Create (or empty if it already exists) the folder where to save all the speed statistics.
			String entropyStatsFolderPath;
			if(simpleFolderFormat){
				entropyStatsFolderPath = mainFolderPath + "/EntropyStatistics";
			}else{
				entropyStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".EntropyStatistics";
			}
			File entropyStatsFolder = new File(entropyStatsFolderPath);
			if(entropyStatsFolder.isDirectory()){
				if(!emptyFolder(entropyStatsFolder)){
					System.out.println("Summarization interrupted. Cannot empty the EntropyStatistics folder: " + entropyStatsFolder.getPath());
					return;
				}
			}else{
				if(!entropyStatsFolder.mkdir()){
					System.out.println("Summarization interrupted. Cannot create the EntropyStatistics folder: " + entropyStatsFolder.getPath());
					return;
				}
			}

			//List<String> acceptedMatches = new ArrayList<String>();

			File[] playerTypesDirs;

			File[] playerRolesDirs = null;

			File[] entropyLogs = null;

			//File randomSpeedLog;

			String playerType;

			String playerRole;

			// Prepare all the SingleValueStats that will compute the aggregated statistics for each player-role combination over all the matches
			Map<String, Map<String, Map<String, Map<String, SingleValueDoubleStats>>>> aggregatedEntropyStatistics =
					new HashMap<String, Map<String, Map<String, Map<String, SingleValueDoubleStats>>>>();

			Map<String, Map<String, Map<String, SingleValueDoubleStats>>> playerTypeEntropyMap =
					new HashMap<String, Map<String, Map<String, SingleValueDoubleStats>>>();

			Map<String, Map<String, SingleValueDoubleStats>> roleEntropyMap =
					new HashMap<String, Map<String, SingleValueDoubleStats>>();
			Map<String, Map<String, SingleValueDoubleStats>> allRoleEntropyMap =
					new HashMap<String, Map<String, SingleValueDoubleStats>>();

			// Iterate over the directories containing the matches logs for each player's type.
			playerTypesDirs = entropyLogsFolder.listFiles();

			// For the folder of each player type...
			for(int i = 0; i < playerTypesDirs.length; i++){

				if(playerTypesDirs[i].isDirectory()){

					playerType = playerTypesDirs[i].getName();

					// Get the map corresponding to this player type
					playerTypeEntropyMap = aggregatedEntropyStatistics.get(playerType);
					if(playerTypeEntropyMap == null){
						playerTypeEntropyMap = new HashMap<String, Map<String, Map<String, SingleValueDoubleStats>>>();
						aggregatedEntropyStatistics.put(playerType, playerTypeEntropyMap);
					}

					playerRolesDirs = playerTypesDirs[i].listFiles();

					// Iterate over all the folders corresponding to the different roles the player played
					for(int j = 0; j < playerRolesDirs.length; j++){

						if(playerRolesDirs[j].isDirectory()) {
							playerRole = playerRolesDirs[j].getName();

							// Get the map corresponding to this role
							roleEntropyMap = playerTypeEntropyMap.get(playerRole);
							if(roleEntropyMap == null){
								roleEntropyMap =  new HashMap<String, Map<String, SingleValueDoubleStats>>();
								playerTypeEntropyMap.put(playerRole, roleEntropyMap);
							}
							// Get the map corresponding to all roles
							allRoleEntropyMap = playerTypeEntropyMap.get("AllRoles");
							if(allRoleEntropyMap == null){
								allRoleEntropyMap =  new HashMap<String, Map<String, SingleValueDoubleStats>>();
								playerTypeEntropyMap.put("AllRoles", allRoleEntropyMap);
							}

							entropyLogs = playerRolesDirs[j].listFiles();

							// For each stats file...
							for(int k = 0; k < entropyLogs.length; k++){

								String[] splittedName = entropyLogs[k].getName().split("\\.");

								// If it's a .csv file, compute and log the statistics
								if(!(splittedName[splittedName.length-1].equalsIgnoreCase("csv"))){
									System.out.println("Found file with no .csv extension when summarizing entropy statistics.");
									rejectFile(entropyLogs[k], rejectedEntropyLogsFolder + "/" + playerType + "/" + playerRole);
								}else{

									// If the stats are referring to a match that was rejected, reject them too

									if(!(acceptedMatches.containsKey(entropyLogs[k].getName().substring(0, entropyLogs[k].getName().length()-18)))){

										System.out.println("Found Entropy Statistics file for a match that was previously rejected from statistics.");
										rejectFile(entropyLogs[k], rejectedEntropyLogsFolder + "/" + playerType + "/" + playerRole);
									}else{

										extractEntropyStats(entropyLogs[k], allRoleEntropyMap, roleEntropyMap);

									}
								}
							}
						}
					}
				}
			}

			// Log all the aggregated statistics

			for(Entry<String, Map<String, Map<String, Map<String, SingleValueDoubleStats>>>> playerTypeStats: aggregatedEntropyStatistics.entrySet()){

				for(Entry<String, Map<String, Map<String, SingleValueDoubleStats>>> roleStats: playerTypeStats.getValue().entrySet()){

					for(Entry<String, Map<String, SingleValueDoubleStats>> stepStats: roleStats.getValue().entrySet()){

						StatsUtils.writeToFileMkParentDir(entropyStatsFolderPath + "/" + playerTypeStats.getKey() + "/" + roleStats.getKey() + "/" +
								"Step" + stepStats.getKey() + "-Entropy-AggrStats.csv", "StatType;#Samples;Min;Max;Median;SD;SEM;Avg;CI;");

						for(Entry<String, SingleValueDoubleStats> statTypeStats: stepStats.getValue().entrySet()){

							String toLog = statTypeStats.getKey() + ";" + statTypeStats.getValue().getNumSamples() + ";" +
									statTypeStats.getValue().getMinValue() + ";" + statTypeStats.getValue().getMaxValue() + ";" +
									statTypeStats.getValue().getMedian() + ";" + statTypeStats.getValue().getValuesStandardDeviation() + ";" +
									statTypeStats.getValue().getValuesSEM() + ";" + statTypeStats.getValue().getAvgValue() + ";" +
									statTypeStats.getValue().get95ConfidenceInterval() + ";";

							StatsUtils.writeToFileMkParentDir(entropyStatsFolderPath + "/" + playerTypeStats.getKey() + "/" + roleStats.getKey() + "/" +
									"Step" + stepStats.getKey() + "-Entropy-AggrStats.csv", toLog);

						}

					}

				}

			}

		}else {
			//System.out.println("Impossible to find the speed logs directory to summarize: " + speedLogsFolder.getPath());
		}

	}

	private static void summarizeBestCombos(File theBestComboStatsFile, MatchInfo matchInfo,
			String playerType, String playerRole,
			Map<String,Map<String,Map<String,ParamsComboInfo>>> playerRoleComboStatsMap,
			Map<String,Map<String,Map<String,ParamsComboInfo>>> playerAllRolesComboStatsMap){

		//System.out.println("Best combo of " + theBestComboStatsFile.getName());

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		String role = null;
		String parameter = null;
		String parameterValue = null;

		Map<String,Map<String,ParamsComboInfo>> roleCombosStatsMap; // <parameterOrder, combinationsOfValuesForParameters>
		Map<String,Map<String,ParamsComboInfo>> roleCombosStatsMapAllRoles; // <parameterOrder, combinationsOfValuesForParameters>
		Map<String,ParamsComboInfo> paramCombosStatsMap; // <Combination, nuberOfTimeTheCombinationWasSelectedAsBest>
		Map<String,ParamsComboInfo> paramCombosStatsMapAllRoles; // <Combination, nuberOfTimeTheCombinationWasSelectedAsBest>
		ParamsComboInfo comboInfo;

		try {
			br = new BufferedReader(new FileReader(theBestComboStatsFile));

			// Read first line
			theLine = br.readLine();

			while(theLine != null){

				splitLine = theLine.split(";");

				// For each line, parse the parameters and add them to their statistic

				// Changing role map
				role = splitLine[1];
				roleCombosStatsMap = playerRoleComboStatsMap.get(role);
				if(roleCombosStatsMap == null){
					roleCombosStatsMap = new HashMap<String,Map<String,ParamsComboInfo>>();
					playerRoleComboStatsMap.put(role, roleCombosStatsMap);
				}
				roleCombosStatsMapAllRoles = playerAllRolesComboStatsMap.get(role);
				if(roleCombosStatsMapAllRoles == null){
					roleCombosStatsMapAllRoles = new HashMap<String,Map<String,ParamsComboInfo>>();
					playerAllRolesComboStatsMap.put(role, roleCombosStatsMapAllRoles);
				}

				String[] reordered = reorderParameters(splitLine[3], splitLine[5]);
				if(reordered == null){
					System.out.println("Wrong formatting of the .csv file " + theBestComboStatsFile.getPath() + ".");
					System.out.println("Stopping summarization of the file. Partial summarization.");
				   	if(br != null){
				   		try {
				   			br.close();
						} catch (IOException ioe) {
							System.out.println("Exception when closing the .csv file " + theBestComboStatsFile.getPath() + ".");
							ioe.printStackTrace();
						}
				   	}
				    return;
				}

				parameter = reordered[0];
				parameterValue = reordered[1];

				// Get maps for parameter
				paramCombosStatsMap = roleCombosStatsMap.get(parameter);
				if(paramCombosStatsMap == null){
					paramCombosStatsMap = new HashMap<String,ParamsComboInfo>();
					roleCombosStatsMap.put(parameter, paramCombosStatsMap);
				}

				paramCombosStatsMapAllRoles = roleCombosStatsMapAllRoles.get(parameter);
				if(paramCombosStatsMapAllRoles == null){
					paramCombosStatsMapAllRoles = new HashMap<String,ParamsComboInfo>();
					roleCombosStatsMapAllRoles.put(parameter, paramCombosStatsMapAllRoles);
				}

				// Update stats of parameters combo
				comboInfo = paramCombosStatsMap.get(parameterValue);
				if(comboInfo == null){
					comboInfo = new ParamsComboInfo();
					paramCombosStatsMap.put(parameterValue, comboInfo);
				}
				comboInfo.increaseNumCommits();
				double outcome = matchInfo.getFinalOutcome(playerType, playerRole);
				if(outcome == 1.0){ // Win
					comboInfo.increaseNumWins();
				}else if(outcome == 0){ // Loss
					comboInfo.increaseNumLosses();
				}else{ // Tie
					comboInfo.increaseNumTies();
				}

				// Update stats of parameters combo for all roles
				comboInfo = paramCombosStatsMapAllRoles.get(parameterValue);
				if(comboInfo == null){
					comboInfo = new ParamsComboInfo();
					paramCombosStatsMapAllRoles.put(parameterValue, comboInfo);
				}
				comboInfo.increaseNumCommits();
				if(outcome == 1.0){ // Win
					comboInfo.increaseNumWins();
				}else if(outcome == 0){ // Loss
					comboInfo.increaseNumLosses();
				}else{ // Tie
					comboInfo.increaseNumTies();
				}

				theLine = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + theBestComboStatsFile.getPath() + ".");
			System.out.println("Stopping summarization of the file. Partial summarization.");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + theBestComboStatsFile.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
        	return;
		}

	}


	// Read each line

	// Get the map for the step

	// for each stat type...

	// ..get corresponding maps and add the value

	private static void extractParamsStats(File theParamsStatsFile, String mabType, Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>> mabTypeMap,
			Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>> mabTypeMapAllRoles){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		String role = null;
		String parameter = null;
		String parameterValue = null;

		Map<String,Map<String,Map<String,SingleValueStats>>> roleMap = null;
		Map<String,Map<String,Map<String,SingleValueStats>>> roleMapAllRoles = null;

		Map<String,Map<String,SingleValueStats>> parameterMap = null;
		Map<String,Map<String,SingleValueStats>> parameterMapAllRoles = null;

		Map<String,SingleValueStats> parameterValueMap = null;
		Map<String,SingleValueStats> parameterValueMapAllRoles = null;

		SingleValueStats valueStats;
		SingleValueStats valueStatsAllRoles;

		try {
			br = new BufferedReader(new FileReader(theParamsStatsFile));

			// Read first line
			theLine = br.readLine();

			while(theLine != null){

				splitLine = theLine.split(";");

				if(splitLine.length == 14){
					// For each line, parse the parameters and add them to their statistic

					// Changing role map
					role = splitLine[1];
					roleMap = mabTypeMap.get(role);
					if(roleMap == null){
						roleMap = new HashMap<String,Map<String,Map<String,SingleValueStats>>>();
						mabTypeMap.put(role, roleMap);
					}
					roleMapAllRoles = mabTypeMapAllRoles.get(role);
					if(roleMapAllRoles == null){
						roleMapAllRoles = new HashMap<String,Map<String,Map<String,SingleValueStats>>>();
						mabTypeMapAllRoles.put(role, roleMapAllRoles);
					}

					// Get next parameter(s) and value(s)
					if(mabType.equals("Global")){
						String[] reordered = reorderParameters(splitLine[3], splitLine[5]);
						if(reordered == null){
							System.out.println("Wrong formatting of the .csv file " + theParamsStatsFile.getPath() + ".");
							System.out.println("Stopping summarization of the file. Partial summarization: the number of samples of the statistics might not be equal anymore.");
				        	if(br != null){
					        	try {
									br.close();
								} catch (IOException ioe) {
									System.out.println("Exception when closing the .csv file " + theParamsStatsFile.getPath() + ".");
									ioe.printStackTrace();
								}
				        	}
				        	return;
						}

						parameter = reordered[0];
						parameterValue = reordered[1];
					}else{
						parameter = splitLine[3];
						parameterValue = splitLine[5];
					}

					// Changing parameters maps
					parameterMap = roleMap.get(parameter);
					if(parameterMap == null){
						parameterMap = new LinkedHashMap<String,Map<String,SingleValueStats>>();
						roleMap.put(parameter, parameterMap);
					}
					parameterMapAllRoles = roleMapAllRoles.get(parameter);
					if(parameterMapAllRoles == null){
						parameterMapAllRoles = new LinkedHashMap<String,Map<String,SingleValueStats>>();
						roleMapAllRoles.put(parameter, parameterMapAllRoles);
					}

					// Get maps for parameter value
					parameterValueMap = parameterMap.get(parameterValue);
					if(parameterValueMap == null){
						parameterValueMap = new HashMap<String,SingleValueStats>();
						parameterMap.put(parameterValue, parameterValueMap);
					}
					parameterValueMapAllRoles = parameterMapAllRoles.get(parameterValue);
					if(parameterValueMapAllRoles == null){
						parameterValueMapAllRoles = new HashMap<String,SingleValueStats>();
						parameterMapAllRoles.put(parameterValue, parameterValueMapAllRoles);
					}

					// For each statistic get the name, the corresponding SingleValueStats and add the value to it.
					for(int i = 6; i < 13; i=i+2){
						valueStats = parameterValueMap.get(splitLine[i]);
						if(valueStats == null){
							if(i==8){
								valueStats = new SingleValueLongStats();
							}else{
								valueStats = new SingleValueDoubleStats();
							}
							parameterValueMap.put(splitLine[i], valueStats);
						}
						valueStatsAllRoles = parameterValueMapAllRoles.get(splitLine[i]);
						if(valueStatsAllRoles == null){
							if(i==8){
								valueStatsAllRoles = new SingleValueLongStats();
							}else{
								valueStatsAllRoles = new SingleValueDoubleStats();
							}
							parameterValueMapAllRoles.put(splitLine[i], valueStatsAllRoles);
						}
						if(i==8){
							((SingleValueLongStats)valueStats).addValue(Long.parseLong(splitLine[i+1]));
							((SingleValueLongStats)valueStatsAllRoles).addValue(Long.parseLong(splitLine[i+1]));
						}else if(i==6){
							((SingleValueDoubleStats)valueStats).addValue((splitLine[i+1].equals("null") ? -1 : Double.parseDouble(splitLine[i+1])));
							((SingleValueDoubleStats)valueStatsAllRoles).addValue((splitLine[i+1].equals("null") ? -1 : Double.parseDouble(splitLine[i+1])));
						}else{
							((SingleValueDoubleStats)valueStats).addValue(Double.parseDouble(splitLine[i+1]));
							((SingleValueDoubleStats)valueStatsAllRoles).addValue(Double.parseDouble(splitLine[i+1]));
						}

					}

				}

				theLine = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + theParamsStatsFile.getPath() + ".");
			System.out.println("Stopping summarization of the file. Partial summarization: the number of samples of the statistics might not be equal anymore.");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + theParamsStatsFile.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
        	return;
		}

	}

	/**
	 * Given a string representing parameters names in an arbitrary order and another string representing the
	 * values of such parameters, this method reorders both strings so that the parameters names are ordered
	 * alphabetically and the corresponding values are moved to the correct place.
	 *
	 * e.g. given the String of parameters names [K C Epsilon Ref] with values String [100 0.2 0.4 50], this
	 * method returns [C Epsilon K Ref] and [0.2 0.4 100 50]
	 *
	 * @param parameters
	 * @param values
	 * @return
	 */
	private static String[] reorderParameters(String parameters, String values){

		String[] splitParameters = parameters.split(" ");
		String[] splitValues = values.split(" ");

		if(splitParameters.length == splitValues.length){
			if(splitParameters[0].equals("[") && splitParameters[splitParameters.length-1].equals("]") &&
					splitValues[0].equals("[") && splitValues[splitValues.length-1].equals("]")){

				// Order alphabetically
				for(int i = 2; i < splitParameters.length-1; i++){
					int j = i;
					while(j > 1 && splitParameters[j].compareTo(splitParameters[j-1]) < 0 ){
						// Switch parameters names
						String tmp = splitParameters[j];
						splitParameters[j] = splitParameters[j-1];
						splitParameters[j-1] = tmp;
						// Switch values
						tmp = splitValues[j];
						splitValues[j] = splitValues[j-1];
						splitValues[j-1] = tmp;
						j--;
					}
				}

				// Re-build the strings
				String parametersToReturn = splitParameters[0];
				String valuesToReturn = splitValues[0];
				for(int i = 1; i < splitParameters.length; i++){
					parametersToReturn += (" " + splitParameters[i]);
					valuesToReturn += (" " + splitValues[i]);
				}

				return new String[]{parametersToReturn, valuesToReturn};
			}
		}

		return null;
	}

	private static void addStatisticsValues(Map<String, Map<String, Map<String, SingleValueDoubleStats>>> aggregatedStatistics, String playerRoleType, String statisticHeader, String[] statisticsNames, double[] statisticsValues){

		Map<String, Map<String, SingleValueDoubleStats>> thePlayerRoleTypeStats = aggregatedStatistics.get(playerRoleType);

		if(thePlayerRoleTypeStats == null){

			thePlayerRoleTypeStats = new HashMap<String, Map<String, SingleValueDoubleStats>>();
			aggregatedStatistics.put(playerRoleType, thePlayerRoleTypeStats);

		}

		Map<String, SingleValueDoubleStats> theStatisticHeaderStats = thePlayerRoleTypeStats.get(statisticHeader);

		if(theStatisticHeaderStats == null){

			theStatisticHeaderStats = new HashMap<String, SingleValueDoubleStats>();
			thePlayerRoleTypeStats.put(statisticHeader, theStatisticHeaderStats);

		}

		for(int i = 0; i < statisticsNames.length; i++){

			SingleValueDoubleStats statNameStats = theStatisticHeaderStats.get(statisticsNames[i]);

			if(statNameStats == null){

				statNameStats = new SingleValueDoubleStats();
				theStatisticHeaderStats.put(statisticsNames[i], statNameStats);

			}

			statNameStats.addValue(statisticsValues[i]);
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
        		 || !matchJSONObject.has("playerNamesFromHost")|| !matchJSONObject.has("roles")){

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
        String[] playersRoles;
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

        try{
        	JSONArray roles = matchJSONObject.getJSONArray("roles");
        	playersRoles = new String[roles.length()];
        	for(int j = 0; j < roles.length(); j++){
        		playersRoles[j] = roles.getString(j);
        	}
        }catch(JSONException e){
        	System.out.println("Information (\"roles\" array) improperly formatted in the JSON file.");
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

        if(playersNames.length <= 0 || playersRoles.length <= 0 || playersGoals.length <= 0){
        	System.out.println("Error: found no players names and/or no roles and/or no players goals.");
        	return null;
        }

        if(!(playersNames.length == playersGoals.length && playersNames.length == playersRoles.length)){
        	System.out.println("Error: found " + playersGoals.length + " goal values, " + playersNames.length + " players and " + playersRoles.length + " roles.");
        	return null;
        }

        return new MatchInfo(playersNames, playersRoles, playersGoals, file);

	}

	private static void rejectFile(File theRejectedFile, String rejectionDestinationPath){

		File rejectionDestinationFolder = new File(rejectionDestinationPath);
		if(!rejectionDestinationFolder.exists() || !rejectionDestinationFolder.isDirectory()){
			rejectionDestinationFolder.mkdirs();
		}

		System.out.println("Rejecting file " + theRejectedFile.getPath());

		//System.out.println("Rejection destination= " + rejectionDestinationPath + "/" + theRejectedFile.getName());

		boolean success = theRejectedFile.renameTo(new File(rejectionDestinationPath + "/" + theRejectedFile.getName()));
    	if(!success){
    		System.out.println("Failed to move file " + theRejectedFile.getPath() + " to the RejectedFiles folder. Excluding it from the summary anyway.");
    	}
	}



	private static void preprocessTreeStats(String mainFolderPath, String tourneyType, String gameKey){

		String treeLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".TreeLogs";

		String treeLogsEndFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".TreeLogsEnd";

		//System.out.println("matchesLogsFolderPath= " + matchesLogsFolderPath);

		File treeLogsFolder = new File(treeLogsFolderPath);

		if(!treeLogsFolder.isDirectory()){
			//System.out.println("Impossible to find the tree logs directory to process: " + treeLogsFolder.getPath());
			return;
		}

		File treeLogsEndFolder = new File(treeLogsEndFolderPath);
		if(treeLogsEndFolder.isDirectory()){
			if(!emptyFolder(treeLogsEndFolder)){
				System.out.println("Summarization interrupted. Cannot empty the TreeLogsEnd folder: " +treeLogsEndFolder.getPath());
				return;
			}
		}else{
			if(!treeLogsEndFolder.mkdir()){
				System.out.println("Summarization interrupted. Cannot create the TreeLogsEnd folder: " + treeLogsEndFolder.getPath());
				return;
			}
		}

		// Iterate over the directories containing the matches logs for each player's type.
		File[] playerTypesDirs = treeLogsFolder.listFiles();

		String playerType;

		File[] playerRolesDirs;

		String playerRole;

		File[] speedLogs;

		// For the folder of each player type...
		for(int i = 0; i < playerTypesDirs.length; i++){

			if(playerTypesDirs[i].isDirectory()){

				playerType = playerTypesDirs[i].getName();

				playerRolesDirs = playerTypesDirs[i].listFiles();

				// Iterate over all the folder corresponding to the different roles the player played
				for(int j = 0; j < playerRolesDirs.length; j++){

					if(playerRolesDirs[j].isDirectory()){
						playerRole = playerRolesDirs[j].getName();

						// Create the tree stats files for the player with only the stats at the end of each step.
						speedLogs = playerRolesDirs[j].listFiles();

						for(int k = 0; k <speedLogs.length; k++){

							if(speedLogs[k].isFile()){// If it's a .csv file, log the end statistics

								String[] splittedName = speedLogs[k].getName().split("\\.");

								if(!(splittedName[splittedName.length-1].equalsIgnoreCase("csv"))){
									System.out.println("Found file with no .csv extension when creating tree end statistics.");
								}else{

									String filename = speedLogs[k].getName();

									String destFile = treeLogsEndFolderPath + "/" + playerType + "/" + playerRole + "/" + filename;

									BufferedReader br = null;
									String theLine;
									String[] splitLine;

									try {
										br = new BufferedReader(new FileReader(speedLogs[k]));

										// Read header
										theLine = br.readLine();

										if(theLine != null){
											StatsUtils.writeToFileMkParentDir(destFile,theLine);

											theLine = br.readLine();

											while(theLine != null){
												// For each line, parse the parameters and add them to their statistic
												splitLine = theLine.split(";");

												if(splitLine.length >= 2 && splitLine[1].equals("End")){
													StatsUtils.writeToFileMkParentDir(destFile,theLine);
												}

												theLine = br.readLine();
											}
										}

										br.close();
									} catch (IOException e) {
										System.out.println("Exception when reading the .csv file " + speedLogs[k].getName() + ".");
										System.out.println("Corresponding tree end statistics file incomplete!");
							        	e.printStackTrace();
							        	if(br != null){
								        	try {
												br.close();
											} catch (IOException ioe) {
												System.out.println("Exception when closing the .csv file " + speedLogs[k].getName() + ".");
												ioe.printStackTrace();
											}
							        	}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private static void extractBranchingStats(File theBranchingStatsFile,
			Map<String, Map<String, SingleValueDoubleStats>> allRolesBranchingMap ,
			Map<String, Map<String, SingleValueDoubleStats>> roleBranchingMap ){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		Map<String, SingleValueDoubleStats> allRolesStepBranchingMap =
				new HashMap<String, SingleValueDoubleStats>();
		Map<String, SingleValueDoubleStats> stepBranchingMap =
				new HashMap<String, SingleValueDoubleStats>();

		SingleValueDoubleStats allRolesStatTypeBranchingStat;
		SingleValueDoubleStats statTypeBranchingStat;

		String step;

		// Statistics for which to compute the aggregated statistics
		String[] statisticsNames = new String[]{"#Samples","Min","Max","Median","SD","SEM","Avg","CI"};

		try {
			br = new BufferedReader(new FileReader(theBranchingStatsFile));

			// Skip first line
			theLine = br.readLine();
			theLine = br.readLine();

			while(theLine != null){

				splitLine = theLine.split(";");

				if(splitLine.length != 9){
					System.out.println("Found wrong line when computing branching statistics: " + theLine);
				}else {
					// For each line, parse the parameters and add them to their statistic

					// Changing role map
					step = splitLine[0];

					allRolesStepBranchingMap = allRolesBranchingMap.get(step);
					if(allRolesStepBranchingMap == null){
						allRolesStepBranchingMap = new HashMap<String, SingleValueDoubleStats>();
						allRolesBranchingMap.put(step, allRolesStepBranchingMap);
					}
					stepBranchingMap = roleBranchingMap.get(step);
					if(stepBranchingMap == null){
						stepBranchingMap = new HashMap<String, SingleValueDoubleStats>();
						roleBranchingMap.put(step, stepBranchingMap);
					}

					for(int i = 0; i < statisticsNames.length; i++) {

						allRolesStatTypeBranchingStat = allRolesStepBranchingMap.get(statisticsNames[i]);
						if(allRolesStatTypeBranchingStat == null){
							allRolesStatTypeBranchingStat = new SingleValueDoubleStats();
							allRolesStepBranchingMap.put(statisticsNames[i], allRolesStatTypeBranchingStat);
						}
						statTypeBranchingStat = stepBranchingMap.get(statisticsNames[i]);
						if(statTypeBranchingStat == null){
							statTypeBranchingStat = new SingleValueDoubleStats();
							stepBranchingMap.put(statisticsNames[i], statTypeBranchingStat);
						}

						Double theValue = Double.parseDouble(splitLine[i+1]);

						allRolesStatTypeBranchingStat.addValue(theValue);
						statTypeBranchingStat.addValue(theValue);

					}

				}

				theLine = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + theBranchingStatsFile.getPath() + ".");
			System.out.println("Stopping summarization of the file. Partial summarization: the number of samples of the statistics might not be equal anymore.");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + theBranchingStatsFile.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
        	return;
		}

	}

	private static void extractDepthStats(File theDepthStatsFile,
			Map<String, Map<String, SingleValueDoubleStats>> allRolesDepthMap ,
			Map<String, Map<String, SingleValueDoubleStats>> roleDepthMap ){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		Map<String, SingleValueDoubleStats> allRolesStepDepthMap =
				new HashMap<String, SingleValueDoubleStats>();
		Map<String, SingleValueDoubleStats> stepDepthMap =
				new HashMap<String, SingleValueDoubleStats>();

		SingleValueDoubleStats allRolesStatTypeDepthStat;
		SingleValueDoubleStats statTypeDepthStat;

		String step;

		// Statistics for which to compute the aggregated statistics
		String[] statisticsNames = new String[]{"#Samples","Min","Max","Median","SD","SEM","Avg","CI"};

		try {
			br = new BufferedReader(new FileReader(theDepthStatsFile));

			// Skip first line
			theLine = br.readLine();
			theLine = br.readLine();

			while(theLine != null){

				splitLine = theLine.split(";");

				if(splitLine.length != 9){
					System.out.println("Found wrong line when computing depth statistics: " + theLine);
				}else {
					// For each line, parse the parameters and add them to their statistic

					// Changing role map
					step = splitLine[0];

					allRolesStepDepthMap = allRolesDepthMap.get(step);
					if(allRolesStepDepthMap == null){
						allRolesStepDepthMap = new HashMap<String, SingleValueDoubleStats>();
						allRolesDepthMap.put(step, allRolesStepDepthMap);
					}
					stepDepthMap = roleDepthMap.get(step);
					if(stepDepthMap == null){
						stepDepthMap = new HashMap<String, SingleValueDoubleStats>();
						roleDepthMap.put(step, stepDepthMap);
					}

					for(int i = 0; i < statisticsNames.length; i++) {

						allRolesStatTypeDepthStat = allRolesStepDepthMap.get(statisticsNames[i]);
						if(allRolesStatTypeDepthStat == null){
							allRolesStatTypeDepthStat = new SingleValueDoubleStats();
							allRolesStepDepthMap.put(statisticsNames[i], allRolesStatTypeDepthStat);
						}
						statTypeDepthStat = stepDepthMap.get(statisticsNames[i]);
						if(statTypeDepthStat == null){
							statTypeDepthStat = new SingleValueDoubleStats();
							stepDepthMap.put(statisticsNames[i], statTypeDepthStat);
						}

						Double theValue = Double.parseDouble(splitLine[i+1]);

						allRolesStatTypeDepthStat.addValue(theValue);
						statTypeDepthStat.addValue(theValue);

					}

				}

				theLine = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + theDepthStatsFile.getPath() + ".");
			System.out.println("Stopping summarization of the file. Partial summarization: the number of samples of the statistics might not be equal anymore.");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + theDepthStatsFile.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
        	return;
		}

	}

	private static void extractEntropyStats(File theEntropyStatsFile,
			Map<String, Map<String, SingleValueDoubleStats>> allRolesEntropyMap ,
			Map<String, Map<String, SingleValueDoubleStats>> roleEntropyMap ){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		Map<String, SingleValueDoubleStats> allRolesStepEntropyMap =
				new HashMap<String, SingleValueDoubleStats>();
		Map<String, SingleValueDoubleStats> stepEntropyMap =
				new HashMap<String, SingleValueDoubleStats>();

		SingleValueDoubleStats allRolesStatTypeEntropyStat;
		SingleValueDoubleStats statTypeEntropyStat;

		String step;

		// Statistics for which to compute the aggregated statistics
		String[] statisticsNames = new String[]{"#Samples","Min","Max","Median","SD","SEM","Avg","CI"};

		try {
			br = new BufferedReader(new FileReader(theEntropyStatsFile));

			// Skip first line
			theLine = br.readLine();
			theLine = br.readLine();

			while(theLine != null){

				splitLine = theLine.split(";");

				if(splitLine.length != 9){
					System.out.println("Found wrong line when computing entropy statistics: " + theLine);
				}else {
					// For each line, parse the parameters and add them to their statistic

					// Changing role map
					step = splitLine[0];

					allRolesStepEntropyMap = allRolesEntropyMap.get(step);
					if(allRolesStepEntropyMap == null){
						allRolesStepEntropyMap = new HashMap<String, SingleValueDoubleStats>();
						allRolesEntropyMap.put(step, allRolesStepEntropyMap);
					}
					stepEntropyMap = roleEntropyMap.get(step);
					if(stepEntropyMap == null){
						stepEntropyMap = new HashMap<String, SingleValueDoubleStats>();
						roleEntropyMap.put(step, stepEntropyMap);
					}

					for(int i = 0; i < statisticsNames.length; i++) {

						allRolesStatTypeEntropyStat = allRolesStepEntropyMap.get(statisticsNames[i]);
						if(allRolesStatTypeEntropyStat == null){
							allRolesStatTypeEntropyStat = new SingleValueDoubleStats();
							allRolesStepEntropyMap.put(statisticsNames[i], allRolesStatTypeEntropyStat);
						}
						statTypeEntropyStat = stepEntropyMap.get(statisticsNames[i]);
						if(statTypeEntropyStat == null){
							statTypeEntropyStat = new SingleValueDoubleStats();
							stepEntropyMap.put(statisticsNames[i], statTypeEntropyStat);
						}

						Double theValue = Double.parseDouble(splitLine[i+1]);

						allRolesStatTypeEntropyStat.addValue(theValue);
						statTypeEntropyStat.addValue(theValue);

					}

				}

				theLine = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + theEntropyStatsFile.getPath() + ".");
			System.out.println("Stopping summarization of the file. Partial summarization: the number of samples of the statistics might not be equal anymore.");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + theEntropyStatsFile.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
        	return;
		}

	}
}

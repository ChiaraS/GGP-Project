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

		/** New instructions that work only if the main folder path contains in the last folder name
		 *  the tourney type and the game key surrounded by "." in the 2nd and 3rd position respectively.
		 *
		 *  E.g.: something.something.gameKey.somethingElse (usually 354658372535.Tourney.gameKey)
		 */
		File mainFolder = new File(mainFolderPath);
		//String[] splitMainFolderPath = mainFolderPath.split("/"); // Works ony if the psth usesthe "/" separator and not
		// Get the name of the tourney folder
		String tourneyName = mainFolder.getName();
		// Split the tourney folder name
		String[] splitTourneyName = tourneyName.split("\\.");
		String tourneyType = splitTourneyName[1];
		String gameKey = splitTourneyName[2];

		//System.out.println("mainFolderPath= " + mainFolderPath);

		String matchesLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".MatchesLogs";

		//System.out.println("matchesLogsFolderPath= " + matchesLogsFolderPath);

		File matchesLogsFolder = new File(matchesLogsFolderPath);

		if(!matchesLogsFolder.isDirectory()){
			System.out.println("Impossible to find the log directory to summarize: " + matchesLogsFolder.getPath());
			return;
		}

		// Create (or empty if it already exists) the folder where to move all the match log files
		// that have been rejected and haven't been considered when computing the statistics.
		String rejectedFilesFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedFiles";

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
		String statsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".Statistics";
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
        int[] playersGoals;

		for(List<MatchInfo> infoList : matchInfo){

			toWrite = "Match number;";

			playersNames = infoList.get(0).getPlayersNames();

			for(int i = 0; i < playersNames.length; i++){
				toWrite += playersNames[i] + ";";
			}

			writeToFile(statsFolder + "/Combination" + infoList.get(0).getCombination() + ".csv", toWrite);

			for(MatchInfo mi : infoList){

				toWrite = mi.getMatchNumber() + ";";

				playersGoals = mi.getplayersGoals();

				for(int i = 0; i < playersGoals.length; i++){
					toWrite += playersGoals[i] + ";";
				}
				writeToFile(statsFolder + "/Combination" + mi.getCombination() + ".csv", toWrite);
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
	            	}else{
	            		theStats.addWins(1, mi.getCombination(), mi.getMatchNumber());
	            	}
	            }
			}
		}

		String scoresStatsFilePath = statsFolder + "/ScoreStats.csv";

		String winsStatsFilePath = statsFolder + "/WinsStats.csv";

		writeToFile(scoresStatsFilePath, "Player;#Samples;MinScore;MaxScore;StandardDeviation;StdErrMean;AvgScore;ConfidenceInterval;MinAvgScore;MaxAvgScore;");

		writeToFile(winsStatsFilePath, "Player;#Samples;MinWins;MaxWins;StandardDeviation;StdErrMean;AvgWin%;ConfidenceInterval;MinAvgWin%;MaxAvgWin%");


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

			writeToFile(statsFolder + "/" + entry.getKey() + "-ScoreSamples.csv", "Combination;Match number;Score;");

			for(int i = 0; i < scores.size(); i++){
				writeToFile(statsFolder + "/" + entry.getKey() + "-ScoreSamples.csv", "C" + combinations.get(i) + ";" + matchNumbers.get(i) + ";" + scores.get(i) + ";");
			}

			wins = stats.getWins();
			combinations = stats.getWinsCombinations();
			matchNumbers = stats.getWinsMatchNumbers();

			writeToFile(statsFolder + "/" + entry.getKey() + "-WinsSamples.csv", "Combination;Match number;Win percentage;");

			for(int i = 0; i < wins.size(); i++){
				writeToFile(statsFolder + "/" + entry.getKey() + "-WinsSamples.csv", "C" + combinations.get(i) + ";" + matchNumbers.get(i) + ";" + wins.get(i) + ";");
			}

			double avgScore = stats.getAvgScore();
			double scoreCi = (stats.getScoresSEM() * 1.96);

			writeToFile(scoresStatsFilePath, entry.getKey() + ";" + scores.size() + ";" + stats.getMinScore() + ";"
					+ stats.getMaxScore() + ";" + stats.getScoresStandardDeviation() + ";" + stats.getScoresSEM() + ";"
					+ avgScore + ";" + scoreCi + ";" + (avgScore - scoreCi) + ";" + (avgScore + scoreCi) + ";");

			double avgWinPerc = (stats.getAvgWins()*100);
			double winCi = (stats.getWinsSEM() * 1.96 * 100);

			writeToFile(winsStatsFilePath, entry.getKey() + ";" + wins.size() + ";" + stats.getMinWinPercentage() + ";"
					+ stats.getMaxWinPercentage() + ";" + stats.getWinsStandardDeviation() + ";" + stats.getWinsSEM() + ";"
					+ avgWinPerc + ";" + winCi + ";" + (avgWinPerc - winCi) + ";" + (avgWinPerc + winCi) + ";");
		}

		/****************** Compute speed statistics of the matches that were considered in the previous statistics *******************/

		String speedLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".SpeedLogs";

		//System.out.println("matchesLogsFolderPath= " + matchesLogsFolderPath);

		File speedLogsFolder = new File(speedLogsFolderPath);

		if(!speedLogsFolder.isDirectory()){
			System.out.println("Impossible to find the speed logs directory to summarize: " + speedLogsFolder.getPath());
			return;
		}

		// Create (or empty if it already exists) the folder where to move all the speed log files
		// that have been rejected and haven't been considered when computing the statistics.
		String rejectedSpeedFilesFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedSpeedFiles";

		//System.out.println("rejectedFilesFolderPath= " + rejectedFilesFolderPath);


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
		String speedStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".SpeedStatistics";
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

		List<String> acceptedMatches = new ArrayList<String>();

		for(List<MatchInfo> infoList : matchInfo){
			for(MatchInfo mi : infoList){
				acceptedMatches.add(mi.getCorrespondingFile().getName().substring(0, mi.getCorrespondingFile().getName().length()-5));
			}
		}

		int numRoles = matchInfo.get(0).get(0).getplayersGoals().length;

		int summarizedFiles = 0;

		File[] playerTypesDirs;

		File[] playerRolesDirs;

		String[] columnHeaders = new String[]{"Thinking time(ms)", "Search time(ms)", "Iterations/second", "Nodes/second"};

		ColumnType[] columnTypes = new ColumnType[]{ColumnType.LONG, ColumnType.LONG, ColumnType.DOUBLE, ColumnType.DOUBLE};

		int[] columnIndices = new int[]{1,2,5,6};

		String playerType;

		String playerRole;

		File[] speedLogs;

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

					playerRole = playerRolesDirs[j].getName();

					// Create the cumulative speed stats files for the player
					for(int k = 0; k < columnHeaders.length; k++){

						//String acceptableHeader = columnHeaders[i].replaceAll(" ", "_");

						String acceptableHeader = headerToFile.get(columnHeaders[k]);

						if(acceptableHeader == null){
							acceptableHeader = columnHeaders[k];
						}

						writeToFile(speedStatsFolderPath + "/" + playerType + "/" + acceptableHeader + "-AllMatches-" + playerRole + ".csv", "MatchID;#Samples;Min;Max;Median;SD;SEM;Avg;CI");

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

							if(!(acceptedMatches.contains(speedLogs[k].getName().substring(0, speedLogs[k].getName().length()-10)))){

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

											writeToFile(speedStatsFolderPath + "/" + playerType + "/" + acceptableHeader + "-AllMatches-" + playerRole + ".csv",
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

		// Log all the aggregated statistics

		SingleValueDoubleStats theStatsToLog = null;

		for(Entry<String, Map<String, Map<String, SingleValueDoubleStats>>> playerRoleStats: aggregatedStatistics.entrySet()){

			for(Entry<String, Map<String, SingleValueDoubleStats>> statHeaderStats: playerRoleStats.getValue().entrySet()){

				writeToFile(speedStatsFolderPath + "/" + playerRoleStats.getKey() + "-" + statHeaderStats.getKey() + "-AggrStats.csv", "StatType;#Samples;Min;Max;Median;SD;SEM;Avg;CI");

				for(int j = 0; j< statisticsNames.length; j++){
					theStatsToLog = statHeaderStats.getValue().get(statisticsNames[j]);

					if(theStatsToLog != null){
						writeToFile(speedStatsFolderPath + "/" + playerRoleStats.getKey() + "-" + statHeaderStats.getKey() + "-AggrStats.csv", statisticsNames[j] +
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

			System.out.println("!!! Not all the Speed Stats files have been included in the Speed statistics due to some error. Speed stats are not completely clean!");

		}

		/****************** Compute tree size statistics of the matches that were considered in the previous statistics *******************/


		preprocessTreeStats(mainFolderPath, tourneyType, gameKey);

		String treeLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".TreeLogsEnd";

		//System.out.println("matchesLogsFolderPath= " + matchesLogsFolderPath);

		File treeLogsFolder = new File(treeLogsFolderPath);

		if(!treeLogsFolder.isDirectory()){
			//System.out.println("Impossible to find the tree logs directory to summarize: " + treeLogsFolder.getPath());
			return;
		}

		// Create (or empty if it already exists) the folder where to move all the speed log files
		// that have been rejected and haven't been considered when computing the statistics.
		String rejectedTreeFilesFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedTreeFiles";

		//System.out.println("rejectedFilesFolderPath= " + rejectedFilesFolderPath);


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
		String treeStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".TreeStatistics";
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

		columnHeaders = new String[]{"#Nodes", "#ActionsStats", "#RAVE_AMAFStats", "#GRAVE_AMAFStats", "ActionsStats/Node", "RAVE_AMAFStats/Node", "GRAVE_AMAFStats/Node"};

		columnTypes = new ColumnType[]{ColumnType.LONG, ColumnType.LONG, ColumnType.LONG, ColumnType.LONG, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE};

		columnIndices = new int[]{2,3,4,5,6,7,8};

		File[] treeLogs;

		// Prepare all the SingleValueStats that will compute the aggregated statistics for each player-role combination over all the matches
		aggregatedStatistics = new HashMap<String, Map<String, Map<String, SingleValueDoubleStats>>>();

		// Iterate over the directories containing the matches logs for each player's type.
		playerTypesDirs = treeLogsFolder.listFiles();

		// For the folder of each player type...
		for(int i = 0; i < playerTypesDirs.length; i++){

			if(playerTypesDirs[i].isDirectory()){

				playerType = playerTypesDirs[i].getName();

				playerRolesDirs = playerTypesDirs[i].listFiles();

				// Iterate over all the folder corresponding to the different roles the player played
				for(int j = 0; j < playerRolesDirs.length; j++){

					playerRole = playerRolesDirs[j].getName();

					// Create the cumulative speed stats files for the player
					for(int k = 0; k < columnHeaders.length; k++){

						//String acceptableHeader = columnHeaders[i].replaceAll(" ", "_");

						String acceptableHeader = headerToFile.get(columnHeaders[k]);

						if(acceptableHeader == null){
							acceptableHeader = columnHeaders[k];
						}

						writeToFile(treeStatsFolderPath + "/" + playerType + "/" + acceptableHeader + "-AllMatches-" + playerRole + ".csv", "MatchID;#Samples;Min;Max;Median;SD;SEM;Avg;CI");

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

							if(!(acceptedMatches.contains(treeLogs[k].getName().substring(0, treeLogs[k].getName().length()-23)))){
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

											writeToFile(treeStatsFolderPath + "/" + playerType + "/" + acceptableHeader + "-AllMatches-" + playerRole + ".csv",
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

		theStatsToLog = null;

		for(Entry<String, Map<String, Map<String, SingleValueDoubleStats>>> playerRoleStats: aggregatedStatistics.entrySet()){

			for(Entry<String, Map<String, SingleValueDoubleStats>> statHeaderStats: playerRoleStats.getValue().entrySet()){

				writeToFile(treeStatsFolderPath + "/" + playerRoleStats.getKey() + "-" + statHeaderStats.getKey() + "-AggrStats.csv", "StatType;#Samples;Min;Max;Median;SD;SEM;Avg;CI");

				for(int j = 0; j< statisticsNames.length; j++){
					theStatsToLog = statHeaderStats.getValue().get(statisticsNames[j]);

					if(theStatsToLog != null){
						writeToFile(treeStatsFolderPath + "/" + playerRoleStats.getKey() + "-" + statHeaderStats.getKey() + "-AggrStats.csv", statisticsNames[j] +
								";" + theStatsToLog.getNumSamples() + ";" + theStatsToLog.getMinValue() + ";" + theStatsToLog.getMaxValue() + ";" +
								theStatsToLog.getMedian() + ";" + theStatsToLog.getValuesStandardDeviation() + ";" + theStatsToLog.getValuesSEM() +
								";" + theStatsToLog.getAvgValue() + ";" + theStatsToLog.get95ConfidenceInterval() + ";");
					}
				}
			}
		}

		//System.out.println();

		//System.out.println();

		//System.out.println("SummarizedFiles = " + summarizedTreeFiles);

		//System.out.println("NumRoles = " + numRoles);

		//System.out.println("AcceptedMatchesSize = " + acceptedMatches.size());

		/****************** Compute tree size statistics of the matches that were considered in the previous statistics *******************/

		String paramLogsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".ParamsLogs";

		//System.out.println("paramLogsFolderPath= " + paramLogsFolderPath);

		File paramLogsFolder = new File(paramLogsFolderPath);

		if(!paramLogsFolder.isDirectory()){
			System.out.println("Impossible to find the tree logs directory to summarize: " + treeLogsFolder.getPath());
			return;
		}

		// Create (or empty if it already exists) the folder where to move all the speed log files
		// that have been rejected and haven't been considered when computing the statistics.
		String rejectedParamsFilesFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".RejectedParamsFiles";

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
		String paramsStatsFolderPath = mainFolderPath + "/" + tourneyType + "." + gameKey + ".ParamsStatistics";
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
		Map<String,Map<String,Map<String,Map<String,Map<String,Integer>>>>> aggregatedBestComboStats = new HashMap<String,Map<String,Map<String,Map<String,Map<String,Integer>>>>>();
		Map<String,Map<String,Map<String,Map<String,Integer>>>> playerTypeComboStatsMap; // <playerType,comboStatsPerPlayedRole>
		Map<String,Map<String,Map<String,Integer>>> playerRoleComboStatsMap; //<MyPlayedRole,comboStatsPerRoleInGame>
		Map<String,Map<String,Map<String,Integer>>> playerAllRolesComboStatsMap; // <AllRoles,comboStatsPerRoleInGame>

		// Iterate over the directories containing the matches logs for each player's type.
		playerTypesDirs = paramLogsFolder.listFiles();

		// For the folder of each player type...
		for(int i = 0; i < playerTypesDirs.length; i++){

			if(playerTypesDirs[i].isDirectory()){

				playerType = playerTypesDirs[i].getName();

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
					playerTypeComboStatsMap = new HashMap<String,Map<String,Map<String,Map<String,Integer>>>>();
					aggregatedBestComboStats.put(playerType, playerTypeComboStatsMap);
				}

				playerRolesDirs = playerTypesDirs[i].listFiles();

				// Get the map for all the roles
				playerAllRolesMap = playerTypeMap.get("AllRoles");
				if(playerAllRolesMap == null){
					playerAllRolesMap = new HashMap<String,Map<String,Map<String,Map<String,Map<String,SingleValueStats>>>>>();
					playerTypeMap.put("AllRoles", playerAllRolesMap);
				}
				// Get the comboStats for all the roles
				playerAllRolesComboStatsMap = playerTypeComboStatsMap.get("AllRoles");
				if(playerAllRolesComboStatsMap == null){
					playerAllRolesComboStatsMap = new HashMap<String,Map<String,Map<String,Integer>>>();
					playerTypeComboStatsMap.put("AllRoles", playerAllRolesComboStatsMap);
				}

				// Iterate over all the folders corresponding to the different roles the player played
				for(int j = 0; j < playerRolesDirs.length; j++){

					if(playerRolesDirs[j].isDirectory()){

						playerRole = playerRolesDirs[j].getName();

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
							playerRoleComboStatsMap = new HashMap<String,Map<String,Map<String,Integer>>>();
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

								if(!(acceptedMatches.contains(paramsStatsFiles[k].getName().substring(0, paramsStatsFiles[k].getName().length()-26))) &&
										!(acceptedMatches.contains(paramsStatsFiles[k].getName().substring(0, paramsStatsFiles[k].getName().length()-25))) &&
										!(acceptedMatches.contains(paramsStatsFiles[k].getName().substring(0, paramsStatsFiles[k].getName().length()-20)))){
									System.out.println("Found Params Statistics file for a match that was previously rejected from statistics.");
									rejectFile(paramsStatsFiles[k], rejectedParamsFilesFolderPath + "/" + playerType + "/" + playerRole);
								}else{

									if(paramsStatsFiles[k].getName().endsWith("GlobalParamTunerStats.csv")){
										mabType = "Global";
									}else if(paramsStatsFiles[k].getName().endsWith("LocalParamTunerStats.csv")){
										mabType = "Local";
									}else if(paramsStatsFiles[k].getName().endsWith("BestParamsCombo.csv")){
										summarizeBestCombos(paramsStatsFiles[k], playerRoleComboStatsMap, playerAllRolesComboStatsMap);
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

						writeToFile(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
								"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "ROLE = " + roleStats.getKey());
						writeToFile(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
								"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "");

						for(Entry<String,Map<String,Map<String,SingleValueStats>>> parameterStats: roleStats.getValue().entrySet()){

							writeToFile(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
									"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "PARAM = " + parameterStats.getKey());
							writeToFile(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
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


								writeToFile(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
										"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", statisToLog);

							}

							writeToFile(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
									"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "");

						}

						writeToFile(paramsStatsFolderPath + "/" + playerTypeStats.getKey() + "-" + playerRoleStats.getKey() +
								"-" + mabTypeStats.getKey() + "ParamTuner-AggrStats.csv", "");

					}
				}
			}
		}

		// Log all the best combos statistics
		for(Entry<String,Map<String,Map<String,Map<String,Map<String,Integer>>>>> playerTypeComboStats : aggregatedBestComboStats.entrySet()){
			for(Entry<String,Map<String,Map<String,Map<String,Integer>>>> playerRoleComboStats : playerTypeComboStats.getValue().entrySet()){
				for(Entry<String,Map<String,Map<String,Integer>>> roleCombosStats : playerRoleComboStats.getValue().entrySet()){

					writeToFile(paramsStatsFolderPath + "/" + playerTypeComboStats.getKey() + "-" + playerRoleComboStats.getKey() +
							"-BestParamsCombo-AggrStats.csv", "");
					writeToFile(paramsStatsFolderPath + "/" + playerTypeComboStats.getKey() + "-" + playerRoleComboStats.getKey() +
							"-BestParamsCombo-AggrStats.csv", "ROLE = " + roleCombosStats.getKey());

					for(Entry<String,Map<String,Integer>> paramCombosStats : roleCombosStats.getValue().entrySet()){

						writeToFile(paramsStatsFolderPath + "/" + playerTypeComboStats.getKey() + "-" + playerRoleComboStats.getKey() +
								"-BestParamsCombo-AggrStats.csv", "PARAMS = " + paramCombosStats.getKey() + ";NUM_COMMITS;");

						for(Entry<String,Integer> comboStat : paramCombosStats.getValue().entrySet()){
							writeToFile(paramsStatsFolderPath + "/" + playerTypeComboStats.getKey() + "-" + playerRoleComboStats.getKey() +
									"-BestParamsCombo-AggrStats.csv", comboStat.getKey() + ";" + comboStat.getValue() + ";");
						}
					}
				}
			}
		}

		//System.out.println();

		//System.out.println();

		//System.out.println("SummarizedFiles = " + summarizedTreeFiles);

		//System.out.println("NumRoles = " + numRoles);

		//System.out.println("AcceptedMatchesSize = " + acceptedMatches.size());


	}

	private static void summarizeBestCombos(File theBestComboStatsFile,
			Map<String,Map<String,Map<String,Integer>>> playerRoleComboStatsMap,
			Map<String,Map<String,Map<String,Integer>>> playerAllRolesComboStatsMap){

		//System.out.println("Best combo of " + theBestComboStatsFile.getName());

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		String role = null;
		String parameter = null;
		String parameterValue = null;

		Map<String,Map<String,Integer>> roleCombosStatsMap; // <parameterOrder, combinationsOfValuesForParameters>
		Map<String,Map<String,Integer>> roleCombosStatsMapAllRoles; // <parameterOrder, combinationsOfValuesForParameters>
		Map<String,Integer> paramCombosStatsMap; // <Combination, nuberOfTimeTheCombinationWasSelectedAsBest>
		Map<String,Integer> paramCombosStatsMapAllRoles; // <Combination, nuberOfTimeTheCombinationWasSelectedAsBest>
		Integer visits;

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
					roleCombosStatsMap = new HashMap<String,Map<String,Integer>>();
					playerRoleComboStatsMap.put(role, roleCombosStatsMap);
				}
				roleCombosStatsMapAllRoles = playerAllRolesComboStatsMap.get(role);
				if(roleCombosStatsMapAllRoles == null){
					roleCombosStatsMapAllRoles = new HashMap<String,Map<String,Integer>>();
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
					paramCombosStatsMap = new HashMap<String,Integer>();
					roleCombosStatsMap.put(parameter, paramCombosStatsMap);
				}

				paramCombosStatsMapAllRoles = roleCombosStatsMapAllRoles.get(parameter);
				if(paramCombosStatsMapAllRoles == null){
					paramCombosStatsMapAllRoles = new HashMap<String,Integer>();
					roleCombosStatsMapAllRoles.put(parameter, paramCombosStatsMapAllRoles);
				}

				// Increase visits of parameters combo
				visits = paramCombosStatsMap.get(parameterValue);
				if(visits == null){
					paramCombosStatsMap.put(parameterValue, new Integer(1));
				}else{
					paramCombosStatsMap.put(parameterValue, new Integer(visits.intValue()+1));
				}
				// Increase visits of parameters combo
				visits = paramCombosStatsMapAllRoles.get(parameterValue);
				if(visits == null){
					paramCombosStatsMapAllRoles.put(parameterValue, new Integer(1));
				}else{
					paramCombosStatsMapAllRoles.put(parameterValue, new Integer(visits.intValue()+1));
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

	private static void writeToFile(String filename, String message){

		File destinationFile = new File(filename);
		if(!destinationFile.getParentFile().isDirectory()){
			destinationFile.getParentFile().mkdirs();
		}

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
											writeToFile(destFile,theLine);

											theLine = br.readLine();

											while(theLine != null){
												// For each line, parse the parameters and add them to their statistic
												splitLine = theLine.split(";");

												if(splitLine.length >= 2 && splitLine[1].equals("End")){
													writeToFile(destFile,theLine);
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
}

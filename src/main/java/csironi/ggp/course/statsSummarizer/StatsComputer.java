package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import csironi.ggp.course.experiments.propnet.SingleValueDoubleStats;

public class StatsComputer {

	/**
	 * Given the path of a folder containing one folder for each game with statistics for multiple runs of the game [mainFolderPath] and
	 * the type of statistic that we want to aggregate [statisticType], this method creates a single file with a table that for each game
	 * reports the average over the game runs of the given statisticType for each game turn. For each aggregated statistic in the table
	 * the 95% confidence interval and the number of samples (i.e. game runs that reached the corresponding game turn) for the statistic
	 * are also reported.
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length != 2) {
			System.out.println("Wrong number of arguments. Expecting the following two inputs: [mainFolderPath] [statisticType].");
			return;
		}

		File mainLogFolder = new File(args[0]);

		logAveragePerStep(mainLogFolder.getPath(),averagePerStepForAllGames(mainLogFolder, args[1]),args[1]);



	}

	/**
	 * Given a file that contains certain statistics for each step of a game,
	 * extracts the statistic with name statName for each game step.
	 * @param file
	 * @param statName
	 * @return an array where each entry corresponds to the statistic for the
	 */
	public static Map<Integer,Double> extractStepStatistic(File file, String statisticType) {

		BufferedReader br = null;
		String theLine;
		String[] splitLine;
		int statIndex = -1;
		Map<Integer,Double> statistics = new HashMap<Integer,Double>();
		int step;
		double statistic;


		try {
			br = new BufferedReader(new FileReader(file));

			// Read header
			theLine = br.readLine();

			if(theLine != null){

				splitLine = theLine.split(";");

				if(!splitLine[0].equals("Game step")) {

					try {
						br.close();
					} catch (IOException ioe) {
						System.out.println("Exception when closing the .csv file " + file.getPath() + ".");
						ioe.printStackTrace();
					}

					return null;

				}

				for(int i = 1; i < splitLine.length; i++){
					if(splitLine[i].equalsIgnoreCase(statisticType)){
						statIndex = i;
					}
				}

				if(statIndex != -1){

					theLine = br.readLine();

					while(theLine != null){
						// For each line, parse the statistic and add it to the map
						splitLine = theLine.split(";");

						if(splitLine.length > statIndex){
							step = Integer.parseInt(splitLine[0]);

							if(step >= 0) {

								statistic = Double.parseDouble(splitLine[statIndex]);
								if(statistics.get(step) != null) {
									System.out.println("Found multiple entries for step " + step + " in Stats file " + file.getPath() + "! Ignoring all entries after the first!");
								}else {

									if(statistic < 0) {
										System.out.println("Found negative statistic for step " + step + " in Stats file " + file.getPath() + "! Considering the value as 0!");
										statistic = 0;
									}

									statistics.put(step, statistic);
								}
							}else {
								System.out.println("Found negative step value (" + step + ") in Stats file " + file.getPath() + "! Ignoring entry!");
							}
						}

						theLine = br.readLine();
					}

				}

			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + file.getPath() + ".");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + file.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
		}

		if(statistics.isEmpty()) {
			return null;
		}

		return statistics;

	}


	/**
	 * Given the folder of a game containing one folder for each game run, each of which contains a Stats.csv file,
	 * this method aggregates the given statisticType over each game run for each step of the game.
	 *
	 * @param gameFolder
	 * @param statisticType
	 * @return a map where each entry corresponds to a game step for the given game associated with the corresponding aggregated
	 * statistics over all the game run for the given 'statisticType.
	 */
	public static Map<Integer, SingleValueDoubleStats> averagePerStep(File gameFolder, String statisticType) {

		Map<Integer, SingleValueDoubleStats> averagePerStep = new HashMap<Integer, SingleValueDoubleStats>();

		SingleValueDoubleStats currentStats;

		File[] gameRunFolders = gameFolder.listFiles();

		for(File gameRunFolder : gameRunFolders) {

			File[] gameRunLogs = gameRunFolder.listFiles();

			for(File gameRunLog : gameRunLogs) {
				if(gameRunLog.getName().equals("Stats.csv")) {

					for(Entry<Integer,Double> stepStatistic : extractStepStatistic(gameRunLog, statisticType).entrySet()) {

						currentStats = averagePerStep.get(stepStatistic.getKey());

						if(currentStats == null) {
							currentStats = new SingleValueDoubleStats();
							averagePerStep.put(stepStatistic.getKey(), currentStats);
						}

						currentStats.addValue(stepStatistic.getValue());

					}

				}
			}

		}

		return averagePerStep;

	}

	/**
	 * Given a folder containing a folder for all games, this method aggregates the given statisticType for each turn for each
	 * game in the folder over all the game runs.
	 *
	 * @param mainFolder
	 * @param statisticType
	 * @return a map where each game is associated with a map that associates to each game turn the average of the given statisticType.
	 */
	public static Map<String,Map<Integer,SingleValueDoubleStats>> averagePerStepForAllGames(File mainFolder, String statisticType) {

		File[] gameFolders = mainFolder.listFiles();

		Map<String,Map<Integer,SingleValueDoubleStats>> allGamesStats = new HashMap<String,Map<Integer,SingleValueDoubleStats>>();

		String gameKey;

		for(File gameFolder : gameFolders) {

			if(gameFolder.isDirectory()) {

				gameKey = gameFolder.getName();

				if(allGamesStats.containsKey(gameKey)) {
					System.out.println("Found duplicate game folder for game " + gameKey + ".");
				}else {
					allGamesStats.put(gameKey, averagePerStep(gameFolder, statisticType));
				}
			}

		}

		return allGamesStats;

	}

	/**
	 * Given the Map created by the method averagePerStepForAllGames, logs its entries in a table in a single file with the following format:
	 *
	 * 			  |	Game1 	   			  | Game2				| Game3      		  | Game4      			|
	 *  GameTurn  | avg	  | ci | #samples | avg | ci | #samples | avg | ci | #samples | avg | ci | #samples |
	 *         0  |       |    |		  |     |    |       	|     |    |    	  |     |	 |			|
	 *         1  |       |    |		  |     |    |       	|     |    |    	  |     |	 |			|
	 *         2  |       |    |		  |     |    |       	|     |    |    	  |     |	 |			|
	 *         3  |       |    |		  |     |    |       	|     |    |    	  |     |	 |			|
	 *         4  |       |    |		  |     |    |       	|     |    |    	  |     |	 |			|
	 *
	 * @param mainFolder
	 * @param allGamesStats
	 * @param statisticType
	 */
	public static void logAveragePerStep(String mainFolder, Map<String,Map<Integer,SingleValueDoubleStats>> allGamesStats, String statisticType) {

		String fileName = statisticType.replace("/", "Per");

		boolean done = false;

		List<String> gameKeys = new ArrayList<String>(allGamesStats.keySet());

		String toLog = ";";
		String toLog2 = "GameTurn;";
		for(String gameKey : gameKeys) {
			toLog += (gameKey + ";;;;;");
			toLog2 += "avg;ci;lowerBound;upperBound;#samples;";
		}
		StatsUtils.writeToFile(mainFolder + "/" + fileName + "-StatsPerGame.csv", toLog);
		StatsUtils.writeToFile(mainFolder + "/" + fileName + "-StatsPerGame.csv", toLog2);

		int gameStep = 0;

		while(!done) {

			done = true;

			toLog = gameStep + ";";

			for(String gameKey : gameKeys) {

				Map<Integer,SingleValueDoubleStats> gameStats = allGamesStats.get(gameKey);
				SingleValueDoubleStats stats = gameStats.remove(gameStep);

				if(stats == null) {
					toLog += ";;;;;";
				}else {
					toLog += stats.getAvgValue() + ";" + stats.get95ConfidenceInterval() + ";" +
							(stats.getAvgValue() - stats.get95ConfidenceInterval()) + ";" +
							(stats.getAvgValue() + stats.get95ConfidenceInterval()) + ";" + stats.getNumSamples() + ";";
				}

				done = done && gameStats.isEmpty();
			}

			StatsUtils.writeToFile(mainFolder + "/" + fileName + "-StatsPerGame.csv", toLog);


			gameStep++;

		}


	}

}

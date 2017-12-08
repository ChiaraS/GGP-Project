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

/**
 * This class expects as input a folder containing a sub-folder for each game,
 * where each sub-folder contains scores and wins statistics of the tournament
 * for the game (usually this structure is created by the StatsSummarizer class).
 * The class aggregates wins and scores statistics of all games into single files.
 * It also creates a .csv file for each player algorithm in the tourney containing
 * the latex representations of the win percentage (with confidence interval) of
 * the algorithm for each game.
 *
 * As optional extra arguments, ALIAS names for players can be specified together
 * with the players types for which the alias must be substituted. E.g. if we want
 * PlayerA and PlayerB to be considered as the same player with name PlayerAB we
 * would have to specify the following as argument:
 *
 * PlayerAB=PlayerA;PlayerB (with no spaces!)
 *
 * We can specify as many arguments as the previous one as we want. If we also
 * want PlayerC, PlayerD and PlayerE to be aliased as PlayerCDE we can specify
 * another argument as follows:
 *
 * PlayerCDE=PlayerC;PlayerD;PlayerE
 *
 * This is useful when we want to put together results for different games where we
 * accidentally gave different names to the same player, or we repeat the same experiment
 * (i.e. with the same games) for two different players and then we want to aggregate the
 * wins for each game only of the best of the two players.
 *
 * @author C.Sironi
 *
 */
public class WinScoreStatsAggregator {

	public WinScoreStatsAggregator() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		/************************************ Prepare the folders *********************************/

		if(args.length != 2 && args.length != 3){
			System.out.println("Impossible to aggregate statistics. Specify both the absolute path of the folder containing statistics and the name of the aggragate statistics file.");
			System.out.println("This code will create two aggragated statistics files: [NameYouProvide]ScoreStatistics.csv and [NameYouProvide]WinsStatistics.csv.");
			return;
		}

		String sourceFolderPath = args[0];
		String resultFile = sourceFolderPath + "/" + args[1];

		// Create a map that maps each player to its alias
		Map<String,String> aliases = new HashMap<String,String>();

		// Fill the map
		String[] aliasSpecification;
		String alias;
		String[] playersWithSameAlias;
		for(int i = 2; i < args.length; i++){
			aliasSpecification = args[i].split("=");

			//System.out.println(aliasSpecification);

			alias = aliasSpecification[0];

			//System.out.println();

			playersWithSameAlias = aliasSpecification[1].split(";");
			for(int j = 0; j < playersWithSameAlias.length; j++){

				//System.out.println(playersWithSameAlias[j]);

				aliases.put(playersWithSameAlias[j], alias);
			}
		}

		System.out.println(sourceFolderPath);
		System.out.println(resultFile);

		System.out.println("ALIASES:");
		if(aliases.isEmpty()){
			System.out.println("none");
		}else{
			for(Entry<String,String> entry : aliases.entrySet()){
				System.out.println(entry.getKey() + " = " + entry.getValue());
			}
		}

		File sourceFolder = new File(sourceFolderPath);

		if(!sourceFolder.isDirectory()){
			System.out.println("Impossible to find the directory with the statistics to aggragate.");
			return;
		}

		File[] gamesDirs = sourceFolder.listFiles();

		File[] statsDirs;

		File[] statsFiles;

		String gameKey;

		String scoresFile = resultFile + "ScoreStatistics.csv";

		System.out.println(scoresFile);

		String winsFile = resultFile + "WinsStatistics.csv";

		System.out.println(winsFile);

		writeToFile(scoresFile, "Game;Player;#Samples;MinScore;MaxScore;StandardDeviation;StdErrMean;AvgScore;ConfidenceInterval;MinExtreme;MaxExtreme;Robustness;");

		writeToFile(winsFile, "Game;Player;#Samples;MinPoints;MaxPoints;StandardDeviation;StdErrMean;AvgWin%;ConfidenceInterval;MinExtreme;MaxExtreme;Robustness;");

		BufferedReader br;
		String theLine;

		Map<String,ExperimentsStats> totalStatistics = new HashMap<String,ExperimentsStats>();
		ExperimentsStats expStats;
		String playerName;

		// For the folder of each game...
		for(int i = 0; i < gamesDirs.length; i++){

			if(gamesDirs[i].isDirectory() && gamesDirs[i].getName().endsWith("-Stats")){

				// ...scan the content until you find the ".Statistics" folder of the game.
				statsDirs = gamesDirs[i].listFiles();

				for(int j = 0; j < statsDirs.length; j++){


					if(statsDirs[j].isDirectory() && statsDirs[j].getName() != null && (statsDirs[j].getName().equals("Statistics") || statsDirs[j].getName().endsWith("-Stats") || statsDirs[j].getName().endsWith("-stats") || statsDirs[j].getName().endsWith(".Statistics") || statsDirs[j].getName().endsWith(".statistics"))){

						writeToFile(scoresFile, ";");
						writeToFile(winsFile, ";");

						if(statsDirs[j].getName().endsWith("-Stats") || statsDirs[j].getName().endsWith("-stats")){
							gameKey = statsDirs[j].getName().substring(0, statsDirs[j].getName().length()-6);
						}else if(statsDirs[j].getName().endsWith(".Statistics") || statsDirs[j].getName().endsWith(".statistics")){
							String[] splitStatsDir = statsDirs[j].getName().split("\\.");
							gameKey = splitStatsDir[1];
						}else {
							gameKey = statsDirs[j].getParentFile().getName().split("\\.")[2];
						}

						System.out.println(gameKey);

						statsFiles = statsDirs[j].listFiles();

						for(int k = 0; k < statsFiles.length; k++){

							if(statsFiles[k].getName().equals("ScoreStats.csv")){

								try {
									br = new BufferedReader(new FileReader(statsFiles[k]));
									theLine = br.readLine(); // First line is headers
									theLine = br.readLine();

									// Forgot to do this when computing statistics, adding this temporary fix
									theLine = addCIExtremes(theLine);

									// Check if the player name must be substituted by its alias
									theLine = setPlayerAlias(theLine, aliases);

									//System.out.println("4: " + theLine);

									writeToFile(scoresFile, gameKey + ";" + theLine);
									theLine = br.readLine();

									// Forgot to do this when computing statistics, adding this temporary fix
									theLine = addCIExtremes(theLine);

									// Check if the player name must be substituted by its alias
									theLine = setPlayerAlias(theLine, aliases);

									writeToFile(scoresFile, gameKey + ";" + theLine);
									br.close();
								} catch (IOException e) {
									System.out.println("Exception when reading a file while aggregating the statistics.");
						        	e.printStackTrace();
								}

							}

							if(statsFiles[k].getName().equals("WinsStats.csv")){

								try {
									br = new BufferedReader(new FileReader(statsFiles[k]));
									theLine = br.readLine(); // First line is headers
									theLine = br.readLine();

									// Forgot to do this when computing statistics, adding this temporary fix
									theLine = addCIExtremes(theLine);

									// Check if the player name must be substituted by its alias
									theLine = setPlayerAlias(theLine, aliases);

									writeToFile(winsFile, gameKey + ";" + theLine);
									theLine = br.readLine();

									// Forgot to do this when computing statistics, adding this temporary fix
									theLine = addCIExtremes(theLine);

									// Check if the player name must be substituted by its alias
									theLine = setPlayerAlias(theLine, aliases);

									writeToFile(winsFile, gameKey + ";" + theLine);
									br.close();
								} catch (IOException e) {
									System.out.println("Exception when reading a file while aggregating the statistics.");
						        	e.printStackTrace();
								}

							}

							if(statsFiles[k].getName().endsWith("ScoreSamples.csv")){

								playerName = statsFiles[k].getName().substring(0, statsFiles[k].getName().length()-17);

								// Check if the player name must be substituted by its alias
								alias = aliases.get(playerName);

								if(alias != null){
									playerName = alias;
								}

								expStats = totalStatistics.get(playerName);

								if(expStats == null){
									expStats = new ExperimentsStats();
									totalStatistics.put(playerName, expStats);
								}

								try {
									br = new BufferedReader(new FileReader(statsFiles[k]));
									theLine = br.readLine(); // First line is headers
									theLine = br.readLine();

									while(theLine != null){
										String[] splitLine = theLine.split(";");

										if(splitLine.length == 3){

											int sample;
											try{
												sample = Integer.parseInt(splitLine[2]);
												expStats.addScore(sample);
											}catch(NumberFormatException e){
												System.out.println("Impossible read score sample. Skipping.");
												e.printStackTrace();
											}
										}

										theLine = br.readLine();

									}

									br.close();
								} catch (IOException e) {
									System.out.println("Exception when reading a file while aggregating the statistics of complete experiment.");
						        	e.printStackTrace();
								}

							}

							if(statsFiles[k].getName().endsWith("WinsSamples.csv")){

								playerName = statsFiles[k].getName().substring(0, statsFiles[k].getName().length()-16);

								// Check if the player name must be substituted by its alias
								alias = aliases.get(playerName);

								if(alias != null){
									playerName = alias;
								}

								expStats = totalStatistics.get(playerName);

								if(expStats == null){
									expStats = new ExperimentsStats();
									totalStatistics.put(playerName, expStats);
								}

								try {
									br = new BufferedReader(new FileReader(statsFiles[k]));
									theLine = br.readLine(); // First line is headers
									theLine = br.readLine();

									while(theLine != null){
										String[] splitLine = theLine.split(";");

										if(splitLine.length == 3){

											double sample;
											try{
												sample = Double.parseDouble(splitLine[2]);
												expStats.addWins(sample);
											}catch(NumberFormatException e){
												System.out.println("Impossible read win sample. Skipping.");
												e.printStackTrace();
											}
										}

										theLine = br.readLine();

									}

									br.close();
								} catch (IOException e) {
									System.out.println("Exception when reading a file while aggregating the statistics of complete experiment.");
						        	e.printStackTrace();
								}

							}
						}
					}
				}
			}
		}

		writeToFile(scoresFile, ";");
		writeToFile(winsFile, ";");

		// Add line to aggregated file with statistics over all the game
		for(Entry<String,ExperimentsStats> entry : totalStatistics.entrySet()){

			double avgWinPerc = entry.getValue().getAvgWins()*100;
			double winCi = entry.getValue().getWinsSEM() * 1.96 * 100;

			writeToFile(winsFile, "OverallStats;" + entry.getKey() + ";" + entry.getValue().getWins().size() + ";" +
					entry.getValue().getMinWinPercentage() + ";" + entry.getValue().getMaxWinPercentage() + ";" +
					entry.getValue().getWinsStandardDeviation() + ";" + entry.getValue().getWinsSEM() + ";" +
					avgWinPerc + ";" + winCi + ";" + (avgWinPerc - winCi) + ";" + (avgWinPerc + winCi) + ";");

			double avgScore =  entry.getValue().getAvgScore();
			double scoreCi = entry.getValue().getScoresSEM() * 1.96;

			writeToFile(scoresFile, "OverallStats;" + entry.getKey() + ";" + entry.getValue().getScores().size() + ";" +
					entry.getValue().getMinScore() + ";" + entry.getValue().getMaxScore() + ";" +
					entry.getValue().getScoresStandardDeviation() + ";" + entry.getValue().getScoresSEM() + ";" +
					avgScore + ";" + scoreCi + ";" + (avgWinPerc - winCi) + ";" + (avgWinPerc + winCi) + ";");

		}

		// Once the wins and scores have been aggregated, create the LATEX code to put the results in a table

		// For the wins:

		List<String> orderedPlayerTypes = new ArrayList<String>();
		//Map<String, Double> averagesSum = new HashMap<String, Double>();
		//Map<String, Integer> numGames = new HashMap<String, Integer>();
		//Map<String, Integer> rubustness = new HashMap<String, Integer>();

		Map<String, String> latexData = new HashMap<String, String>();
		//Map<String, Double> latexAvg = new HashMap<String, Double>();

		try {
			br = new BufferedReader(new FileReader(winsFile));
			theLine = br.readLine(); // First line is headers
			theLine = br.readLine(); // Second line is empty
			theLine = br.readLine();

			while(theLine != null){
				while(theLine != null && !theLine.equals(";")){
					String[] split = theLine.split(";");
					String playerType = split[1];
					if(!(orderedPlayerTypes.contains(playerType))){
						 orderedPlayerTypes.add(playerType);
						 //averagesSum.put(playerType, new Double(0));
						 //numGames.put(playerType, new Integer(0));
						 latexData.put(playerType, "");
						 //latexAvg.put(playerType, new Double(0));
						 //rubustness.put(playerType, new Integer(0));
					}

					String stringAvg = split[7];
					double avg = Double.parseDouble(stringAvg);
					//averagesSum.put(playerType, new Double(averagesSum.get(playerType).doubleValue() + avg));
					//numGames.put(playerType, new Integer(numGames.get(playerType).intValue() + 1));

					String game = split[0];
					double ci = Double.parseDouble(split[8]);
					latexData.put(playerType, (latexData.get(playerType) + game + ";$" + round(avg,1) + "(\\pm" + round(ci,2) + ")$;\n"));
					//latexAvg.put(playerType, new Double(latexAvg.get(playerType).doubleValue() + round(avg,1)));

					theLine = br.readLine();
				}

				theLine = br.readLine();

			}

			br.close();

			//writeToFile(winsFile, ";");


			for(String s : orderedPlayerTypes){
				//double overallAvg = (averagesSum.get(s).doubleValue() / numGames.get(s).intValue());
				//writeToFile(winsFile, "OverallAvg;" + s + ";" + ";;;;;" + overallAvg);
				String latexFile = resultFile + s + "-Latex.csv";
				//double overallLatexAvg = (latexAvg.get(s).doubleValue() / numGames.get(s).intValue());
				writeToFile(latexFile, latexData.get(s));
			}

		} catch (IOException e) {
			System.out.println("Exception when reading a file while aggregating the statistics.");
        	e.printStackTrace();
		}

	}

	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

	/**
	 * Checks if the line already contains the extremes of the confidence interval, if not adds them to the line.
	 *
	 * The extremes of the confidence interval are [avg-CI, avg+CI].
	 *
	 * @param theLine
	 * @return
	 */
	private static String addCIExtremes(String theLine){

		//System.out.println("1: " + theLine);

		String[] splitLine = theLine.split(";");

		if(splitLine.length <= 8){

			try{
				double avg = Double.parseDouble(splitLine[6]);
				double ci = Double.parseDouble(splitLine[7]);

				theLine += ((avg-ci) + ";" + (avg+ci) + ";");

				//System.out.println("2: " + theLine);
			}catch(NumberFormatException e){
				System.out.println("Impossible to add CI extremes. Skipping.");
				e.printStackTrace();
			}
		}

		//System.out.println("3: " + theLine);

		return theLine;

	}

	/**
	 * Given the line of a win/score summary for a player, checks if the player name must be changed with its alias.
	 *
	 * @param theLine
	 * @return
	 */
	private static String setPlayerAlias(String theLine, Map<String,String> aliases){

		String[] splitLine = theLine.split(";");

		String alias = aliases.get(splitLine[0]);

		if(alias != null){
			splitLine[0] = alias;

			String toReturn = "";

			for(int i = 0; i < splitLine.length; i++){
				toReturn += (splitLine[i] + ";");
			}

			return toReturn;
		}else{
			return theLine;
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

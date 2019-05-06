package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import csironi.ggp.course.experiments.propnet.SingleValueDoubleStats;

public class StatsForChart {

	public static void main(String[] args) {

		/************************************ Prepare the folders *********************************/

		if(args.length != 3){
			System.out.println("Impossible to aggregate statistics. Specify the absolute path of the folder containing statistics, the absolute path of the aggragated statistics file and the minimum number of samples per game to be considered.");
			//System.out.println("This code will create two aggragated statistics files: [NameYouProvide]ScoreStatistics.csv and [NameYouProvide]WinsStatistics.csv.");
			return;
		}

		String sourceFolderPath = args[0];
		String resultFileName = args[1];
		int minNumSamples = Integer.MAX_VALUE;
		try{
			minNumSamples = Integer.parseInt(args[2]);
		}catch(NumberFormatException e){
			System.out.println("Wrong format for the matchNumberLimit. Using all matches.");
			e.printStackTrace();
			minNumSamples = Integer.MAX_VALUE;
		}

		System.out.println(sourceFolderPath);
		System.out.println(resultFileName);
		System.out.println(minNumSamples);

		File sourceFolder = new File(sourceFolderPath);

		if(!sourceFolder.isDirectory()){
			System.out.println("Impossible to find the directory with the statistics to aggragate.");
			return;
		}

		File[] kFolders = sourceFolder.listFiles();

		for(File kFolder : kFolders){
			if(kFolder.isDirectory()){
				scanKFolder(kFolder, resultFileName, minNumSamples);
			}
		}
	}

	private static void scanKFolder(File kFolder, String resultFileName, int minNumSamples){

		String kString = kFolder.getName().substring(1, kFolder.getName().length());

		try{
			int k = Integer.parseInt(kString);

			File[] tourneyFolders = kFolder.listFiles();

			for(File tourneyFolder : tourneyFolders){
				if(tourneyFolder.isDirectory()){
					scanTourneyFolder(tourneyFolder, resultFileName, minNumSamples, k);
				}
			}

		}catch(NumberFormatException e){
			System.out.println("Folder non corresponding to any K.");
			e.printStackTrace();
		}

	}

	private static void scanTourneyFolder(File tourneyFolder, String resultFileName, int minNumSamples, int k){

		File[] gamesDirs = tourneyFolder.listFiles();

		Map<String, SingleValueDoubleStats> allCumulativeStats = new HashMap<String, SingleValueDoubleStats>();

		// For the folder of each game...
		for(int i = 0; i < gamesDirs.length; i++){

			if(gamesDirs[i].isDirectory()){

				scanGameFolder(gamesDirs[i], allCumulativeStats, minNumSamples);

			}

		}

		File resultFile = new File(resultFileName);

		if(!resultFile.exists()){
			if(!resultFile.getParentFile().exists()){
				resultFile.getParentFile().mkdirs();
			}

			try {
				resultFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Impossible to create the results file.");
				e.printStackTrace();
			}

			StatsUtils.writeToFile(resultFileName, "PlayerType;K;#Samples;Min;Max;StdDev;StdErrMean;Avg;ConfInt;");
		}

		for(Entry<String, SingleValueDoubleStats> entry : allCumulativeStats.entrySet()){

			StatsUtils.writeToFile(resultFileName, entry.getKey() + ";" + k + ";" + entry.getValue().getNumSamples() + ";" +
					entry.getValue().getMinValue() + ";" + entry.getValue().getMaxValue() + ";" +
					entry.getValue().getValuesStandardDeviation() + ";" + entry.getValue().getValuesSEM() + ";" +
					entry.getValue().getAvgValue() + ";" + entry.getValue().get95ConfidenceInterval() + ";");

		}
	}

	private static void scanGameFolder(File gameFolder, Map<String, SingleValueDoubleStats> allCumulativeStats, int minNumSamples){

		File[] statsFolders = gameFolder.listFiles();

		SingleValueDoubleStats statsToFill;

		String gamerType;

		File[] statsFiles;

		for(File statFolder : statsFolders){

			if(statFolder.isDirectory() && statFolder.getName() != null && (statFolder.getName().endsWith(".Statistics") || statFolder.getName().endsWith(".statistics"))){

				statsFiles = statFolder.listFiles();

				for(int j = 0; j < statsFiles.length; j++){

					if(statsFiles[j].getName().endsWith("-WinsSamples.csv")){

						gamerType = statsFiles[j].getName().split("-")[0];

						statsToFill = allCumulativeStats.get(gamerType);

						if(statsToFill == null){
							statsToFill = new SingleValueDoubleStats();
							allCumulativeStats.put(gamerType, statsToFill);
						}

						addStats(statsFiles[j].getPath(), statsToFill, minNumSamples);

					}

				}
			}
		}
	}

	/**
	 *
	 * @param filename name of the .csv file containing one line for each match with the following format: Combination;MatchNumber;[Win|Score].
	 * @param cumulativeStats the statistics collector where to add the values extracted from the given file (one value for each line in the file).
	 * @param minNumSamples minimum number of samples per game to include in the cumulativeStats (to be used, for example, when the file contains
	 * more samples than the ones we want to consider). This method will add to cumulativeStats only the results of the matches that have an ID lower
	 * than the matchNumberLimit computed using minNumSamples so that the number of used samples per game is the closest integer to minNumSamples that
	 * allows to get the same number of samples per combination of players in the game.
	 */
	private static void addStats(String filename, SingleValueDoubleStats cumulativeStats, int minNumSamples){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;
		int matchNumber;
		double value;
		int matchNumberLimit = -1;

		try{
			br = new BufferedReader(new FileReader(filename));
			theLine = br.readLine(); // First line is headers
			theLine = br.readLine();

			// Compute the matchNumberLimit
			if(minNumSamples == Integer.MAX_VALUE){
				matchNumberLimit = Integer.MAX_VALUE;
			}else{
				splitLine = theLine.split(";");
				int roles = splitLine[0].length()-1;
				double numCombo = Math.pow(2, roles) - 2;
				matchNumberLimit = (int) Math.ceil((double)minNumSamples/numCombo);
			}

			while(theLine != null){

				splitLine = theLine.split(";");

				try{
					matchNumber = Integer.parseInt(splitLine[1]);

					if(matchNumber < matchNumberLimit){
						value = Double.parseDouble(splitLine[2]);

						cumulativeStats.addValue(value);
					}

				}catch(NumberFormatException e){
					System.out.println("Exception reading file line due to wrong number format. Skipping line.");
		        	e.printStackTrace();
				}

				theLine = br.readLine();

			}
		}catch(IOException e){
			System.out.println("Exception when reading file " + filename + ". Stopping file parsing.");
        	e.printStackTrace();
		}

		if(br != null){
			try {
				br.close();
			} catch (IOException e) {
				System.out.println("Cannot properly close file " + filename + ".");
				e.printStackTrace();
			}
		}
	}

}

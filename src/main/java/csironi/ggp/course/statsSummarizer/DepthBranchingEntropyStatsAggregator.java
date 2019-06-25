package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import csironi.ggp.course.utils.MyPair;

/**
 * This class gets a folder with the stats of a tournament and for each game in the folder
 * aggregates statistics in the specified statistics folder (i.e. BranchingStatistics,
 * DepthStatistics or EntropyStatistics). More precisely, it creates for each agent and role
 * in such folder a table with an entry for each game turn, associated with the number of
 * games that reached such game turn and the specified statistic type (e.g. Avg, Median, ...)
 * for the specified statistic name (e.g. Avg Avg will report the average of the average
 * branching factor or depth for the game turn, Median Avg will report the average of the
 * median branching factor or depth for the game turn).
 *
 * Expected input:
 * [tourneyFlder]
 * [theStatsFolder]
 * [theStatName]
 * [theStatType]
 *
 * @author C.Sironi
 *
 */
public class DepthBranchingEntropyStatsAggregator {

	public static void main(String args[]) {

		if(args.length != 4) {
			System.out.println("Wrong input! Expecting the following 4 inputs: [tourneyFlder][statsFolder][statName][statType]!");
			return;
		}

		File tourneyFolder = new File(args[0]);
		String theStatsFolder = args[1];
		String theStatName = args[2];
		String theStatType = args[3];

		if(!(tourneyFolder.exists() && tourneyFolder.isDirectory())){
			System.out.println("The specified tourney folder does not exist or is not a folder!");
			return;
		}

		File[] gameFolders = tourneyFolder.listFiles();
		for(File gameFolder : gameFolders) {
			if(gameFolder.isDirectory() && gameFolder.getName().endsWith("-Stats")){
				File[] statsFolders = gameFolder.listFiles();
				for(File statsFolder : statsFolders) {
					if(statsFolder.isDirectory() && statsFolder.getName().equals(theStatsFolder)){
						File[] playerTypeFolders = statsFolder.listFiles();
						for(File playerTypeFolder : playerTypeFolders) {
							if(playerTypeFolder.isDirectory()){
								File[] roleFolders = playerTypeFolder.listFiles();
								for(File roleFolder : roleFolders) {
									if(roleFolder.isDirectory()){

										String playerType = playerTypeFolder.getName();
										String playerRole = roleFolder.getName();
										String statisticFolderType = theStatsFolder.substring(0, theStatsFolder.length()-10);
										String resultFilePath = statsFolder + "/" + playerType + "-" + playerRole + "-" +
										theStatName + "-" + theStatType + "-" + statisticFolderType + "Statistics.csv";
										String latexFilePathSamples = statsFolder + "/" + playerType + "-" + playerRole + "-" +
												theStatName + "-" + theStatType + "-" + statisticFolderType + "Samples-Latex.csv";
										String latexFilePathStats = statsFolder + "/" + playerType + "-" + playerRole + "-" +
												theStatName + "-" + theStatType + "-" + statisticFolderType + "Statistics-Latex.csv";
										StatsUtils.writeToFile(resultFilePath, "Step;#Samples;Statistic;");

										// Pair of step-statistics, where statistics is a pair of numSamples-statisticValue
										List<MyPair<Integer,MyPair<String,String>>> stepStatistics = new ArrayList<MyPair<Integer,MyPair<String,String>>>();

										File[] stepFiles = roleFolder.listFiles();
										for(File stepFile : stepFiles) {
											if(stepFile.isFile()){
												stepStatistics.add(extractStatistics(stepFile, theStatName, theStatType));
											}
										}

										// Order by step
										Collections.sort(stepStatistics,
												new Comparator<MyPair<Integer,MyPair<String,String>>>(){
											@Override
											public int compare(MyPair<Integer,MyPair<String,String>> o1, MyPair<Integer,MyPair<String,String>> o2) {
												return o1.getFirst() - o2.getFirst();
											}
										});

										for(MyPair<Integer,MyPair<String,String>> theStatsToLog : stepStatistics) {

											StatsUtils.writeToFile(resultFilePath, theStatsToLog.getFirst() + ";" + theStatsToLog.getSecond().getFirst() +
													";" + theStatsToLog.getSecond().getSecond() + ";");

											StatsUtils.writeToFile(latexFilePathSamples, theStatsToLog.getFirst() + ";" + theStatsToLog.getSecond().getFirst() + ";");

											StatsUtils.writeToFile(latexFilePathStats, theStatsToLog.getFirst() + ";" + theStatsToLog.getSecond().getSecond() + ";");

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

	public static MyPair<Integer,MyPair<String,String>> extractStatistics(File stepFile, String theStatName, String theStatType){

		int step = Integer.parseInt(stepFile.getName().split("-")[0].substring(4));

		BufferedReader br = null;
		int samplesIndex = -1;
		int statisticIndex = -1;
		String samples = null;
		String statistic = null;

		String theLine;
		String[] splitLine;

		try {
			br = new BufferedReader(new FileReader(stepFile));

			// Read header
			theLine = br.readLine();

			if(theLine != null) {
				String[] splitHeader = theLine.split(";");

				for(int i = 0; i < splitHeader.length; i++) {

					if(splitHeader[i].equals("#Samples")) {
						samplesIndex = i;
					}

					if(splitHeader[i].equals(theStatType)) {
						statisticIndex = i;
					}

				}
			}

			if(samplesIndex == -1 || statisticIndex == -1) {
				try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + stepFile.getPath() + ".");
					ioe.printStackTrace();
				}
				throw new RuntimeException("Cannot find either #Samples or the statistic type " + theStatType + " for the file " + stepFile.getPath());
			}

			theLine = br.readLine();

			while(theLine != null){

				splitLine = theLine.split(";");

				if(splitLine[0].equals(theStatName)) {
					samples = splitLine[samplesIndex];
					statistic = splitLine[statisticIndex];
				}

				theLine = br.readLine();

			}

			if(samples == null || statistic == null) {
				try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + stepFile.getPath() + ".");
					ioe.printStackTrace();
				}
				throw new RuntimeException("Cannot find either #Samples or the statistic type " + theStatType + " for the file " + stepFile.getPath());
			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + stepFile.getPath() + ".");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + stepFile.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
		}

		return new MyPair<Integer,MyPair<String,String>>(step, new MyPair<String,String>(samples,statistic));


	}

}

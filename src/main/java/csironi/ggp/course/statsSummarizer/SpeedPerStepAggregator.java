package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import csironi.ggp.course.experiments.propnet.SingleValueDoubleStats;

/**
 * This class, given the folder of a tournament, for each game creates for each agent type and each role a file
 * with the average speed (nodes/second) of the agent in each game turn over all the matches performed for the game.
 * NOte that this code has been written quickly and is guaranteed to work only on statistics with the old format (i.e.
 * the format used for the PropNet experiments when matching PropNet and Prover against their cached versions).
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
public class SpeedPerStepAggregator {

	public static void main(String args[]) {

		if(args.length != 1) {
			System.out.println("Wrong input! Expecting the path of the folder for the tourney as input!");
			return;
		}

		File tourneyFolder = new File(args[0]);

		if(!(tourneyFolder.exists() && tourneyFolder.isDirectory())){
			System.out.println("The specified tourney folder does not exist or is not a folder!");
			return;
		}

		// Maps
		Map<String,Map<String,Map<Integer,SingleValueDoubleStats>>> gameMap;
		Map<String,Map<Integer,SingleValueDoubleStats>> playerTypeMap;
		Map<Integer,SingleValueDoubleStats> roleMap;
		Map<Integer,SingleValueDoubleStats> allRolesMap;

		File[] gameFolders = tourneyFolder.listFiles();
		for(File gameFolder : gameFolders) {
			if(gameFolder.isDirectory() && gameFolder.getName().endsWith("-Stats")){

				gameMap = new HashMap<String,Map<String,Map<Integer,SingleValueDoubleStats>>>();

				File[] statsFolders = gameFolder.listFiles();
				for(File statsFolder : statsFolders) {
					if(statsFolder.isDirectory() && statsFolder.getName().endsWith("SpeedLogs")){

						File[] playerTypeFolders = statsFolder.listFiles();
						for(File playerTypeFolder : playerTypeFolders) {
							if(playerTypeFolder.isDirectory()){

								String playerType = playerTypeFolder.getName();
								playerTypeMap = gameMap.get(playerType);
								if(playerTypeMap == null) {
									playerTypeMap = new HashMap<String,Map<Integer,SingleValueDoubleStats>>();
									gameMap.put(playerType, playerTypeMap);
								}

								File[] roleFolders = playerTypeFolder.listFiles();
								for(File roleFolder : roleFolders) {
									if(roleFolder.isDirectory()){

										String playerRole = roleFolder.getName();
										roleMap = playerTypeMap.get(playerRole);
										if(roleMap == null) {
											roleMap = new HashMap<Integer,SingleValueDoubleStats>();
											playerTypeMap.put(playerRole, roleMap);
										}
										allRolesMap = playerTypeMap.get("AllRoles");
										if(allRolesMap == null) {
											allRolesMap = new HashMap<Integer,SingleValueDoubleStats>();
											playerTypeMap.put("AllRoles", allRolesMap);
										}

										File[] matchFiles = roleFolder.listFiles();
										for(File matchFile : matchFiles) {
											if(matchFile.isFile()){
												extractStatistics(matchFile, roleMap, allRolesMap);
											}
										}
									}
								}
							}
						}
					}

					for(Entry<String,Map<String,Map<Integer,SingleValueDoubleStats>>> playerTypeEntry : gameMap.entrySet()) {

						String playerType = playerTypeEntry.getKey();

						for(Entry<String,Map<Integer,SingleValueDoubleStats>> roleEntry : playerTypeEntry.getValue().entrySet()) {

							String playerRole = roleEntry.getKey();

							String resultFilePath = statsFolder.getPath() + "/" + playerType + "-" + playerRole +
									"-NodesPerSecond-AvgPerTurn.csv";
							String latexFilePathSamples = statsFolder.getPath() + "/" + playerType + "-" + playerRole +
									"-NodesPerSecond-AvgPerTurnSamples-Latex.csv";
							String latexFilePathStats = statsFolder.getPath() + "/" + playerType + "-" + playerRole +
									"-NodesPerSecond-AvgPerTurn-Latex.csv";

							StatsUtils.writeToFile(resultFilePath, "Step;#Samples;Avg speed (nodes/second);");

							List<Entry<Integer,SingleValueDoubleStats>> stepEntries =
									new ArrayList<Entry<Integer,SingleValueDoubleStats>>(roleEntry.getValue().entrySet());

							// Order by step
							Collections.sort(stepEntries,
									new Comparator<Entry<Integer,SingleValueDoubleStats>>(){
								@Override
								public int compare(Entry<Integer,SingleValueDoubleStats> o1, Entry<Integer,SingleValueDoubleStats> o2) {
									return o1.getKey() - o2.getKey();
								}
							});

							for(Entry<Integer,SingleValueDoubleStats> stepEntry : stepEntries) {

								StatsUtils.writeToFile(resultFilePath, stepEntry.getKey() + ";" + stepEntry.getValue().getNumSamples() +
										";" + stepEntry.getValue().getAvgValue() + ";");

								StatsUtils.writeToFile(latexFilePathSamples, stepEntry.getKey() + ";" + stepEntry.getValue().getNumSamples() + ";");

								StatsUtils.writeToFile(latexFilePathStats, stepEntry.getKey() + ";" + stepEntry.getValue().getAvgValue() + ";");

							}
						}
					}
				}
			}
		}
	}

	public static void extractStatistics(File matchFile, Map<Integer,SingleValueDoubleStats> roleMap, Map<Integer,SingleValueDoubleStats> allRolesMap){

		BufferedReader br = null;

		double searchTimeTurn1 = 0;
		double visitedNodesTurn1 = 0;

		int gameStep;
		double nodesPerSecond;

		SingleValueDoubleStats stepStats;
		SingleValueDoubleStats allRolesStepStats;

		String theLine;
		String[] splitLine;

		try {
			br = new BufferedReader(new FileReader(matchFile));

			// Read header
			theLine = br.readLine();

			if(theLine != null) {
				String[] splitHeader = theLine.split(";");

				if(!splitHeader[0].equals("Game step") || !splitHeader[2].equals("Search time(ms)") ||
						!splitHeader[4].equals("Visited nodes") || !splitHeader[6].equals("Nodes/second")) {
					try {
						br.close();
					} catch (IOException ioe) {
						System.out.println("Exception when closing the .csv file " + matchFile.getPath() + ".");
						ioe.printStackTrace();
					}
					throw new RuntimeException("Wrong format of the header!");
				}
			}


			theLine = br.readLine();

			boolean addStep0and1 = false;

			while(theLine != null){

				splitLine = theLine.split(";");

				gameStep = Integer.parseInt(splitLine[0]);

				// Game step 0 and 1 correspond to the same turn in the old statistics.
				// ATTENTION! If applying this method to the new statistics, step 1 already included the samples
				// in step 0 (i.e. the metagame), therefore step 1 must be considered and step 0 ignored.
				if(gameStep == 0 || gameStep == 1) {
					searchTimeTurn1 += Integer.parseInt(splitLine[2]);
					visitedNodesTurn1 += Integer.parseInt(splitLine[4]);

					addStep0and1 = true;
				}else {
					nodesPerSecond = Double.parseDouble(splitLine[6]);

					stepStats = roleMap.get(gameStep);
					if(stepStats == null) {
						stepStats = new SingleValueDoubleStats();
						roleMap.put(gameStep, stepStats);
					}
					allRolesStepStats = allRolesMap.get(gameStep);
					if(allRolesStepStats == null) {
						allRolesStepStats = new SingleValueDoubleStats();
						allRolesMap.put(gameStep, allRolesStepStats);
					}

					stepStats.addValue(nodesPerSecond);
					allRolesStepStats.addValue(nodesPerSecond);

				}

				theLine = br.readLine();

			}

			if(addStep0and1) {
				nodesPerSecond = (visitedNodesTurn1/searchTimeTurn1)*1000.0;

				stepStats = roleMap.get(1);
				if(stepStats == null) {
					stepStats = new SingleValueDoubleStats();
					roleMap.put(1, stepStats);
				}
				allRolesStepStats = allRolesMap.get(1);
				if(allRolesStepStats == null) {
					allRolesStepStats = new SingleValueDoubleStats();
					allRolesMap.put(1, allRolesStepStats);
				}

				stepStats.addValue(nodesPerSecond);
				allRolesStepStats.addValue(nodesPerSecond);
			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + matchFile.getPath() + ".");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + matchFile.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
		}

	}


}

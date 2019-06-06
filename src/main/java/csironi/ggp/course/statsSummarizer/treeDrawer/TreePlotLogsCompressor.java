package csironi.ggp.course.statsSummarizer.treeDrawer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import csironi.ggp.course.statsSummarizer.StatsUtils;
import csironi.ggp.course.utils.MyPair;

public class TreePlotLogsCompressor {

	/**
	 * Given the folder of a tourney containing the Stats files of each game, this class creates a compressed
	 * copy of each TreePlot log file, where edges of the same turn are all logged on the same line. It also
	 * creates a file that for each game in the tournament reports the max values for the x and y
	 * coordinates found over all the TreePlot logs of the game.
	 *
	 * @param args
	 */
	public static void main(String args[]) {

		if(args.length != 1){
			System.out.println("Impossible to compress tree plot logs and compute min and max values of the coordinates. Specify the absolute path of the folder containing statistics for the tournament.");
			return;
		}

		String sourceFolderPath = args[0];

		File sourceFolder = new File(sourceFolderPath);

		if(!sourceFolder.isDirectory()){
			System.out.println("Impossible to find the directory with the statistics to compress and format.");
			return;
		}

		// Map that memorizes for each game the maximum value for the x axis over all the tourneys and the maximum value of
		// the y axis over all the tourneys.
		Map<String,MyPair<Integer,Double>> axisMap = new HashMap<String,MyPair<Integer,Double>>();

		MyPair<Integer,Double> axisLimits;

		File[] gamesDirs = sourceFolder.listFiles();

		String gameKey;

		File[] statsDirs;

		File[] playerDirs;

		File[] roleDirs;

		File[] comboDirs;

		File[] treePlotFiles;

		// For the folder of each game...
		for(int i = 0; i < gamesDirs.length; i++){

			if(gamesDirs[i].isDirectory() && gamesDirs[i].getName().endsWith("-Stats")){

				String[] splitGameFolder = gamesDirs[i].getName().split("\\.");
				if(splitGameFolder.length == 4) {
					gameKey = splitGameFolder[2];
				}else {
					gameKey = splitGameFolder[1];
				}

				//System.out.println(gameKey);

				// ...scan the content until you find the "TreePlotLogs" folder of the game.
				statsDirs = gamesDirs[i].listFiles();

				// Check each folder of statistics, until the TreePlotLogs folder is found...
				for(int j = 0; j < statsDirs.length; j++){

					if(statsDirs[j].isDirectory() && statsDirs[j].getName() != null && statsDirs[j].getName().equals("TreePlotLogs")){

						playerDirs = statsDirs[j].listFiles();

						// For the folder of each player type...
						for(int k = 0; k < playerDirs.length; k++){

							//System.out.println(playerType);

							roleDirs = playerDirs[k].listFiles();

							// For the folder of each game role...
							for(int l = 0; l < roleDirs.length; l++){

								//System.out.println(roleName);

								comboDirs = roleDirs[l].listFiles();

								// For the folder of each combo...
								for(int m = 0; m < comboDirs.length; m++){

									treePlotFiles = comboDirs[m].listFiles();

									// For each treePlot file...
									for(int n = 0; n < treePlotFiles.length; n++){

										if(treePlotFiles[n].isFile()) {

											axisLimits = axisMap.get(gameKey);

											if(axisLimits == null) {
												axisLimits = new MyPair<Integer,Double>(-1,-1.0);
												axisMap.put(gameKey, axisLimits);
											}

											MyPair<Integer,Double> fileAxisValues = getAxisValuesAndCompressLogs(treePlotFiles[n]);

											if(fileAxisValues != null) {

												int newX;
												double newY;
												if(fileAxisValues.getFirst() > axisLimits.getFirst()) {
													newX = fileAxisValues.getFirst();
												}else {
													newX = axisLimits.getFirst();
												}
												if(fileAxisValues.getSecond() > axisLimits.getSecond()) {
													newY = fileAxisValues.getSecond();
												}else {
													newY = axisLimits.getSecond();
												}

												MyPair<Integer,Double> newAxisValues = new MyPair<Integer,Double>(newX,newY);
												axisMap.put(gameKey, newAxisValues);

											}else {
												System.out.println("Skipping file!");
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

		// Save the max values for x and y axis (note that minY=-maxY and minX=0)
		String newline = "\n";
		String toLog = "GameKey;maxX;maxY;" + newline;

		for(Entry<String,MyPair<Integer,Double>> gameValues: axisMap.entrySet()) {
			toLog += gameValues.getKey() + ";" + gameValues.getValue().getFirst() + ";" + gameValues.getValue().getSecond() + ";" + newline;
		}

		StatsUtils.writeToFile(sourceFolderPath + "/MaxCoordVals.csv", toLog);

	}

	private static MyPair<Integer,Double> getAxisValuesAndCompressLogs(File statsFile){

		String newline = "\n";

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		int x;
		double y;

		String toLog = "";

		int turnIterations = 0;

		int numEmptyLines = 0;

		int iterations;

		try {
			br = new BufferedReader(new FileReader(statsFile));

			// Read first line
			theLine = br.readLine();

			splitLine = theLine.split(" ");

			try {
				x = Integer.parseInt(splitLine[3]);
				y = Double.parseDouble(splitLine[5]);
			}catch(NumberFormatException nfe) {
				System.out.println("Error parsing file " + statsFile.getPath() + ". Cannot parse x = " + splitLine[3] + " and y = " + splitLine[5]);
				try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + statsFile.getPath() + ".");
					ioe.printStackTrace();
				}
				return null;
			}

			toLog += (theLine + newline);

			while(theLine != null) {

				//System.out.println(theLine);

				if(theLine.isEmpty()) {
					numEmptyLines++;
				}else {

					splitLine = theLine.split(" ");

					if(splitLine.length == 1) {

						try {
							iterations = Integer.parseInt(splitLine[0]);
						}catch(NumberFormatException nfe) {
							System.out.println("Error parsing file " + statsFile.getPath() + ". Cannot parse iterations = " + splitLine[0] + ".");
							try {
								br.close();
							} catch (IOException ioe) {
								System.out.println("Exception when closing the .csv file " + statsFile.getPath() + ".");
								ioe.printStackTrace();
							}
							return null;
						}

						turnIterations += iterations;

					}else if((splitLine.length % 4) == 0) {

						// ATTENTION! Here we don't add a space between the previously added lines
						// and the new one because of how logs are saved in the original file.
						// Each line with coordinates in the original file always ends with a space.
						toLog += theLine;
						turnIterations++;

					}else if(splitLine.length == 5) {
						toLog += newline;
						toLog += turnIterations + " " + theLine + newline;
						//turnIterations = 0;
					}
				}

				theLine = br.readLine();

			}

			if(numEmptyLines > 1) {
				System.out.println("More than a single empty line in file " + statsFile.getPath() + "!");
			}

			br.close();

		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + statsFile.getPath() + ".");
			System.out.println("This file won't be compressed correctly.");
	       	e.printStackTrace();
	       	if(br != null){
		       	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + statsFile.getPath() + ".");
					ioe.printStackTrace();
				}
	       	}
	       	return null;
		}

		String resultFolderPath = statsFile.getParent();
		String outputFileName = "C-" + statsFile.getName();

		StatsUtils.writeToFile(resultFolderPath + "/" + outputFileName, toLog);

		return new MyPair<Integer,Double>(x, y);

	}

}
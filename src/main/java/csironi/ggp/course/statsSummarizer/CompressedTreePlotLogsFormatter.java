package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.io.Files;

import csironi.ggp.course.utils.MyPair;

/**
 *
 * This class expects as input a folder where there is a folder for each performed series of
 * experiments, as created by the TreePlotLogsExtractor. E.g. we could have a folder for the
 * experiments matching 4PNTBEAvs4PRND, the experiments matching 4PNTBEAvsCGMD and the
 * experiments matching 4PRNDvsCGMD. Each folder contains the tree statistics of a set of games.
 *
 * This class creates a single folder, with a subfolder for each game, each of which has a subfolder
 * for each role in the game, each of which has a subfolder for each agent that appears in any of the
 * considered experiments, each of which has a subfolder for each match 0 where the agent played the
 * game with that role. This subfolder contains a 'compressed' version of the TreeSizeStatistics.csv
 * file for the match. This compressed version has all the edges added in a game turn reported on the
 * same line (they will be plotted all at the same time disregarding the iteration number). Moreover,
 * the first line will report the axis limits for x and y not for the single match only but as the
 * minimum min values and maximum max values over all the matches for the game. In this way the plots
 * will all have the same size and be comparable, otherwise MATLAB shrinks/stretches them according
 * to their own axis limits to fit them in the figure.
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
 * @param args
 */
public class CompressedTreePlotLogsFormatter {

	public static void main(String args[]) {

		/************************************ Prepare the folders *********************************/

		if(args.length < 2){
			System.out.println("Impossible to compress and modify logs to create comparable tree plots. Specify both the absolute path of the folder containing a folder with statistics for each experiment and the name of the folder that will contain the compressed logs.");
			return;
		}

		String sourceFolderPath = args[0];
		String resultFolderPath = sourceFolderPath + "/" + args[1];

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
		System.out.println(resultFolderPath);

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
			System.out.println("Impossible to find the directory with the statistics to compress and format.");
			return;
		}

		File resultFolder = new File(resultFolderPath);

		if(resultFolder.isDirectory()){
			System.out.println("The folder where to move the tree log files already exists! Delete folder first!");
			return;
		}

		// Map that memorizes for each game the maximum value for the x axis over all the tourneys and the maximum value of
		// the y axis over all the tourneys.
		Map<String,MyPair<Integer,Double>> axisMap = new HashMap<String,MyPair<Integer,Double>>();

		File[] tourneyDirs = sourceFolder.listFiles();

		File[] gamesDirs;

		String gameKey;

		File[] playerDirs;

		String playerType;

		File[] roleDirs;

		String roleName;

		File[] treePlotFiles;

		String matchAndTourneyID;

		String[] splitLogFileName;

		String outputFileName;

		MyPair<Integer,Double> axisLimits;

		// Fill the map with the maximum axis for each game
		for(int i = 0; i < tourneyDirs.length; i++){

			if(tourneyDirs[i].isDirectory()){

				gamesDirs = tourneyDirs[i].listFiles();

				for(int j = 0; j < gamesDirs.length; j++){

					if(gamesDirs[j].isDirectory()){

						gameKey = gamesDirs[j].getName();

						playerDirs = gamesDirs[j].listFiles();

						for(int k = 0; k < playerDirs.length; k++){
							if(playerDirs[k].isDirectory()){

								roleDirs = playerDirs[k].listFiles();

								for(int l = 0; l < roleDirs.length; l++){

									if(roleDirs[l].isDirectory()){

										treePlotFiles = roleDirs[l].listFiles();

										for(int m = 0; m < treePlotFiles.length; m++){

											if(treePlotFiles[m].isFile()) {

												axisLimits = axisMap.get(gameKey);

												if(axisLimits == null) {
													axisLimits = new MyPair<Integer,Double>(-1,-1.0);
													axisMap.put(gameKey, axisLimits);
												}

												MyPair<Integer,Double> fileAxisValues = getAxisValues(treePlotFiles[m]);

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


		// Format all files with the computed maximum axis values.

		! Count the iterations !

		for(int i = 0; i < tourneyDirs.length; i++){

			if(tourneyDirs[i].isDirectory()){

				gamesDirs = tourneyDirs[i].listFiles();

				for(int j = 0; j < gamesDirs.length; j++){

					if(gamesDirs[j].isDirectory()){

						gameKey = gamesDirs[j].getName();

						axisLimits = axisMap.get(gameKey);

						if(axisLimits == null) {
							System.out.println("Cannot find axis limits for game " + gameKey + ". This shouldn't be possible. Check for errors in the code!");
						}

						playerDirs = gamesDirs[j].listFiles();

						for(int k = 0; k < playerDirs.length; k++){

							if(playerDirs[k].isDirectory()){

								playerType = playerDirs[k].getName();

								roleDirs = playerDirs[k].listFiles();

								for(int l = 0; l < roleDirs.length; l++){

									if(roleDirs[l].isDirectory()){

										roleName = roleDirs[l].getName();

										treePlotFiles = roleDirs[l].listFiles();

										for(int m = 0; m < treePlotFiles.length; m++){

											if(treePlotFiles[m].isFile()) {

												splitLogFileName = treePlotFiles[m].getName().split("\\.");

												matchAndTourneyID = splitLogFileName[0] + "-" + splitLogFileName[1] + "-" + splitLogFileName[2];

												outputFileName = matchAndTourneyID + "-" + roleName + "-" + playerType;

												compressAndSaveLogs(treePlotFiles[m], resultFolderPath, outputFileName, axisLimits);

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


	private static MyPair<Integer,Double> getAxisValues(File statsFile){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		int x;
		double y;

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

			br.close();

		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + statsFile.getPath() + ".");
			System.out.println("Its coordinates are not considered when computing the maximum axis limit.");
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

		return new MyPair<Integer,Double>(x,y);
	}

	private static void compressAndSaveLogs(File statsFile, String resultFolderPath, String outputFileName, MyPair<Integer,Double> axisLimits){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

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

			br.close();

		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + statsFile.getPath() + ".");
			System.out.println("Its coordinates are not considered when computing the maximum.");
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

		return new MyPair<Integer,Double>(x,y);
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

}

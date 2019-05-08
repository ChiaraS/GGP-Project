package csironi.ggp.course.statsSummarizer.treeDrawer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import csironi.ggp.course.statsSummarizer.StatsUtils;
import csironi.ggp.course.utils.MyPair;

/**
 * Given a folder, looks for all the .csv files with compressed logs for the tree and makes sure to remove
 * from the edges added in the turns the coordinates of the edges that correspond to the moves selected for
 * each turn, so that the same edge is only printed once at the end of the plotting for a turn instead of
 * being re-printed with a different color.
 *
 * @author C.Sironi
 *
 */
public class TurnActionDuplicateEdgeRemover {


	public static void main(String args[]) {

		if(args.length < 1){
			System.out.println("Impossible to remove duplicate edges. Specify the absolute path of the folder containing the logs for each game as created by the CompressedTreePlotLogsFormatter class.");
			return;
		}

		String sourceFolderPath = args[0];

		System.out.println(sourceFolderPath);

		File sourceFolder = new File(sourceFolderPath);

		if(!sourceFolder.isDirectory()){
			System.out.println("Impossible to find the directory with the statistics to compress and format.");
			return;
		}

		File[] gamesDirs = sourceFolder.listFiles();

		File[] roleDirs;

		File[] playerDirs;

		File[] matchDirs;

		File[] treePlotFiles;

		for(int i = 0; i < gamesDirs.length; i++){

			if(gamesDirs[i].isDirectory()){

				roleDirs = gamesDirs[i].listFiles();

				for(int j = 0; j < roleDirs.length; j++){

					if(roleDirs[j].isDirectory()){

						playerDirs = roleDirs[j].listFiles();

						for(int k = 0; k < playerDirs.length; k++){

							if(playerDirs[k].isDirectory()){

								matchDirs = playerDirs[k].listFiles();

								for(int l = 0; l < matchDirs.length; l++){

									if(matchDirs[l].isDirectory()){

										treePlotFiles = matchDirs[l].listFiles();

										for(int m = 0; m < treePlotFiles.length; m++){

											if(treePlotFiles[m].isFile()) {

												/*
												String[] splitFileName = treePlotFiles[m].getName().split("\\.");
												String newNameForOldFile = treePlotFiles[m].getParent() + "/";
												for(int index = 0; index < splitFileName.length-1; index++) {
													newNameForOldFile += splitFileName[index];
												}
												newPathForOldFile += ("-old." + splitFileName[splitFileName.length-1]);
												String outputFilePath = treePlotFiles[m].getPath();

												treePlotFiles[m].renameTo(new File(newPathForOldFile));*/

												String outputFilePath = treePlotFiles[m].getParent() + "/NR" + treePlotFiles[m].getName();

												System.out.println(outputFilePath); // CHECK!

												//if(!removeDuplicates(treePlotFiles[m], outputFilePath)) {
												//	System.out.println("Error removing redundant edges form file " + treePlotFiles[m].getPath() + ".");
												//}

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



	private static boolean removeDuplicates(File statsFile, String outputFilePath){

		String firstLine = "";
		List<MyPair<List<Edge>,String>> extractedTurns = new ArrayList<MyPair<List<Edge>,String>>();
		List<Edge> turnMovesEdges = new ArrayList<Edge>();

		BufferedReader br = null;
		String theLine;
		String theNextLine;

		List<Edge> turnEdges;
		Edge turnMoveEdge;

		try {
			br = new BufferedReader(new FileReader(statsFile));

			// Read first line
			firstLine = br.readLine();

			theLine = br.readLine();
			theNextLine = br.readLine();

			while(theLine != null && theNextLine != null && !theNextLine.isEmpty()) {

				turnEdges = extractTurnEdges(theLine);

				if(turnEdges == null) {
					System.out.println("Error when reading turn edges. Canceling duplicate removal for file " + statsFile.getPath() + ".");
					return false;
				}

				extractedTurns.add(new MyPair<List<Edge>,String>(turnEdges,theNextLine));

				turnMoveEdge = extractTurnMoveEdge(theNextLine);

				if(turnMoveEdge == null) {
					System.out.println("Error when reading turn move edge. Canceling duplicate removal for file " + statsFile.getPath() + ".");
					return false;
				}

				turnMovesEdges.add(turnMoveEdge);

				theLine = br.readLine();
				theNextLine = br.readLine();

			}

			br.close();

		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + statsFile.getPath() + ".");
			System.out.println("This file won't be processed correctly.");
	       	e.printStackTrace();
	       	if(br != null){
		       	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + statsFile.getPath() + ".");
					ioe.printStackTrace();
				}
	       	}
	       	return false;
		}

		String newline = "\n";

		String toLog = firstLine + newline;

		for(MyPair<List<Edge>,String> extractedTurnEdges : extractedTurns) {

			extractedTurnEdges.getFirst().removeAll(turnMovesEdges);

			for(Edge e : extractedTurnEdges.getFirst()) {
				toLog += e.toString() + " ";
			}

			toLog += newline;
			toLog += extractedTurnEdges.getSecond() + newline;

		}

		StatsUtils.writeToFileMkParentDir(outputFilePath, toLog);

		return true;

	}

	private static Edge extractTurnMoveEdge(String string) {
		String[] splitString = string.split(" ");

		if(splitString.length != 6) {
			System.out.println("Wrong line format. Expected 6 entries, found " + splitString.length + " entries.");
			return null;
		}

		try {
			return new Edge(Double.parseDouble(splitString[2]), Double.parseDouble(splitString[3]), Double.parseDouble(splitString[4]), Double.parseDouble(splitString[5]));
		}catch(NumberFormatException nfe) {
			System.out.println("Wrong line format. Impossible to parse coordinates from line " + string + ".");
			nfe.printStackTrace();
			return null;
		}
	}

	private static List<Edge> extractTurnEdges(String string) {
		String[] splitString = string.split(" ");

		if(splitString.length%4 != 0) {
			System.out.println("Wrong line format. Expected a number of entries multiple of 4, found " + splitString.length + " entries.");
			return null;
		}

		List<Edge> edges = new ArrayList<Edge>();

		try {

			int i = 0;
			while(i < splitString.length-3) {
				edges.add(new Edge(Double.parseDouble(splitString[i]), Double.parseDouble(splitString[i+1]), Double.parseDouble(splitString[i+2]), Double.parseDouble(splitString[i+3])));
			}

		}catch(NumberFormatException nfe) {
			System.out.println("Wrong line format. Impossible to parse coordinates from line " + string + ".");
			nfe.printStackTrace();
			return null;
		}

		return edges;
	}


}

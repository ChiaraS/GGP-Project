package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NewSpeedStatisticComputer {

	public static void main(String[] args){

		if(args.length != 1){
			System.out.println("Specify the path of the folder containing one stats folder for each game.");
			// The stats folder for each game is the one created by the StatsSummarizer

			return;
		}

		String mainFolder = args[0];

		File mainFolderFile = new File(mainFolder);

		if(!mainFolderFile.isDirectory()){
			System.out.println("Couldn't find specified folder.");
			return;
		}

		File[] gameFolders = mainFolderFile.listFiles();

		for(File gameFolder : gameFolders){
			if(gameFolder.isDirectory()){
				scanGameFolder(mainFolder, gameFolder);
			}
		}

	}

	private static void scanGameFolder(String mainFolder, File gameFolder){
		File[] statFolders = gameFolder.listFiles();

		String game = gameFolder.getName().split("\\.")[2];

		for(File statFolder : statFolders){
			if(statFolder.getName().endsWith(".SpeedLogs")){
				for(File playerFolder : statFolder.listFiles()){
					scanPlayerFolder(mainFolder, game, playerFolder);
				}
			}
		}

	}

	private static void scanPlayerFolder(String mainFolder, String game, File playerFolder){

		String playerCumulativeFilePath = playerFolder.getPath() + "\\CumulativeStatsFile.csv";
		File playerCumulativeFile = new File(playerCumulativeFilePath);

		if(playerCumulativeFile.isFile()){
			playerCumulativeFile.delete();
		}

		writeToFile(playerCumulativeFilePath, "Role;Stats file;Average speed");

		String playerName = playerFolder.getName();

		double sumOfAverages = 0.0;
		int parsedFiles = 0;
		double average;

		File[] roleFolders = playerFolder.listFiles();

		File[] statFiles;

		String role;

		for(File roleFolder : roleFolders){

			if(roleFolder.isDirectory()){

				role = roleFolder.getName();

				statFiles = roleFolder.listFiles();

				for(File statFile : statFiles){
					average = getAverageSpeedFromStatsFile(statFile.getAbsolutePath());

					if(average >= 0){

						writeToFile(playerCumulativeFilePath, role + ";" + statFile.getName().substring(0, statFile.getName().length()-10) + ";" + average + ";");

						sumOfAverages += average;
						parsedFiles++;
					}
				}
			}
		}

		double averageOfAverages = sumOfAverages / ((double)parsedFiles);

		File destination = new File(mainFolder + "\\" + playerName + "-Stats.csv");

		if(!destination.exists()){
			writeToFile(mainFolder + "\\" + playerName + "-Stats.csv", "Game;Samples;Avg of avg speed;");
		}

		writeToFile(mainFolder + "\\" + playerName + "-Stats.csv", game + ";" + parsedFiles + ";" + averageOfAverages + ";");

		if(averageOfAverages < 100){
			writeToFile(mainFolder + "\\" + playerName + "-Latex.csv", game + ";" + round(averageOfAverages, 1) + ";");
		}else{
			writeToFile(mainFolder + "\\" + playerName + "-Latex.csv", game + ";" + round(averageOfAverages, 0) + ";");
		}


	}

	private static double getAverageSpeedFromStatsFile(String filename){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		int searchTime;
		int visitedNodes;
		int searchTimeSum = 0;
		int visitedNodesSum = 0;
		double averageValue = -1.0;

		try{
			br = new BufferedReader(new FileReader(filename));
			theLine = br.readLine(); // First line is headers
			theLine = br.readLine();

			while(theLine != null){

				splitLine = theLine.split(";");

				try{
					searchTime = Integer.parseInt(splitLine[2]);

					visitedNodes = Integer.parseInt(splitLine[4]);

					searchTimeSum += searchTime;
					visitedNodesSum += visitedNodes;

				}catch(NumberFormatException e){
					System.out.println("Exception reading file line due to wrong number format. Skipping line.");
		        	e.printStackTrace();
				}

				theLine = br.readLine();

			}

			averageValue = ((double)visitedNodesSum)/(((double)searchTimeSum)/1000.0);
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

		return averageValue;

	}

	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
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

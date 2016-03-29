package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StatsAggregator {

	public StatsAggregator() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		/************************************ Prepare the folders *********************************/

		if(args.length != 2){
			System.out.println("Impossible to aggragate statistics. Specify both the absolute path of the folder containing statistics and the name of the aggragate statistics file.");
			System.out.println("This code will create two aggragated statistics files: [NameYouProvide]ScoreStatistics.csv and [NameYouProvide]WinsStatistics.csv.");
			return;
		}

		String sourceFolderPath = args[0];
		String resultFile = sourceFolderPath + "/" + args[1];

		System.out.println(sourceFolderPath);
		System.out.println(resultFile);

		File sourceFolder = new File(sourceFolderPath);

		if(!sourceFolder.isDirectory()){
			System.out.println("Impossible to find the directory with the statistics to aggragate.");
			return;
		}

		File[] statsDirs = sourceFolder.listFiles();

		File[] statsFiles;

		String gameKey;

		String scoresFile = resultFile + "ScoreStatistics.csv";

		System.out.println(scoresFile);

		String winsFile = resultFile + "WinsStatistics.csv";

		System.out.println(winsFile);

		writeToFile(scoresFile, "Game;Player;#Samples;MinScore;MaxScore;StandardDeviation;StdErrMean;AvgScore;ConfidenceInterval;");

		writeToFile(winsFile, "Game;Player;#Samples;MinPoints;MaxPoints;StandardDeviation;StdErrMean;AvgWin%;ConfidenceInterval;");

		BufferedReader br;
		String theLine;

		// For the folder of each game...
		for(int i = 0; i < statsDirs.length; i++){

			if(statsDirs[i].isDirectory() && statsDirs[i].getName() != null && (statsDirs[i].getName().endsWith("-Stats") || statsDirs[i].getName().endsWith("-stats"))){

				writeToFile(scoresFile, ";");
				writeToFile(winsFile, ";");

				gameKey = statsDirs[i].getName().substring(0, statsDirs[i].getName().length()-6);

				System.out.println(gameKey);

				statsFiles = statsDirs[i].listFiles();

				for(int j = 0; j < statsFiles.length; j++){

					if(statsFiles[j].getName().equals("ScoreStats.csv")){

						try {
							br = new BufferedReader(new FileReader(statsFiles[j]));
							theLine = br.readLine(); // First line is headers
							theLine = br.readLine();
							writeToFile(scoresFile, gameKey + ";" + theLine);
							theLine = br.readLine();
							writeToFile(scoresFile, gameKey + ";" + theLine);
							br.close();
						} catch (IOException e) {
							System.out.println("Exception when reading a file while aggregating the statistics.");
				        	e.printStackTrace();
						}

					}

					if(statsFiles[j].getName().equals("WinsStats.csv")){

						try {
							br = new BufferedReader(new FileReader(statsFiles[j]));
							theLine = br.readLine(); // First line is headers
							theLine = br.readLine();
							writeToFile(winsFile, gameKey + ";" + theLine);
							theLine = br.readLine();
							writeToFile(winsFile, gameKey + ";" + theLine);
							br.close();
						} catch (IOException e) {
							System.out.println("Exception when reading a file while aggregating the statistics.");
				        	e.printStackTrace();
						}

					}
				}
			}
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

package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PlayerStatsAggregator {



	/**
	 * This method takes as input the folder that contains, for each game that we want to consider, either the
	 * TreeStatistics folder or the SpeedStatistics folder created by the StatsSummarizer. Moreover, takes as
	 * input the file name of the stats files for which we want to gather the stats in a single file for all
	 * games. E.g.: if the name "CadiaPhRaveDuctMctsGamer-AllRoles-NumNodes-AggrStats.csv" is given, this method
	 * will aggregate the results present in all the files with that name for all the games.
	 *
	 * Example:
	 * [folder] [The type of statistic folder where to find the file to summarize (e.g. SpeedStatistics, TreeStatistics)] [file to summarize for each game] [name of the statistic to summarize] [type of statistic to compute for the statistic name]
	 * C:\Users\c.sironi\Dropbox\RES\!UcbNaiveTune\NB\SR SpeedStatistics CadiaGraveMastDuctMctsGamer-AllRoles-IterationsPerSecond-AggrStats median avg
	 * (this will compute the average of all the medians in the given file to summarize for each game in the folder)
	 *
	 * @param args
	 */
	public static void main(String[] args){


		if(args.length != 5){
			System.out.println("Give as input the name of the folder containing the statistics folder for each game, the name of the file from which to get the statistics and the name and type of statistic to extract (e.g. for the average of the medians: (median, avg))");
			return;
		}

		String theStatsFolderPath = args[0];
		String theStatsFolderType = args[1];
		String theStatsFileName = args[2];
		String thaStatName = args[3];
		String theStatType = args[4];

		File theStatsFolder = new File(theStatsFolderPath);

		if(!theStatsFolder.isDirectory()){
			System.out.println("Couldn't find specified folder for which to aggregate statistics.");
			return;
		}

		String[] splittedString;

		File[] gamesFolders = theStatsFolder.listFiles();
		File[] statsFolders;
		File[] statsFiles;

		String gameKey;
		double theValue;

		for(int i = 0; i < gamesFolders.length; i++){

			if(gamesFolders[i].isDirectory() && gamesFolders[i].getName().endsWith("-Stats")){

				System.out.println(gamesFolders[i].getName());

				splittedString = gamesFolders[i].getName().split("\\.");

				if(splittedString.length == 4 || splittedString.length == 3){
					if(splittedString.length == 4) {
						gameKey = splittedString[2];
					}else {
						gameKey = splittedString[1];
					}

					System.out.println(gameKey);

					statsFolders = gamesFolders[i].listFiles();

					for(int j = 0; j < statsFolders.length; j++){

						if(statsFolders[j].getName().endsWith(theStatsFolderType)){

							System.out.println(statsFolders[j].getName());

							statsFiles = statsFolders[j].listFiles();

							for(int k = 0; k < statsFiles.length; k++){
								if(statsFiles[k].getName().startsWith(theStatsFileName)){

									System.out.println(statsFiles[k].getName());

									theValue = extractStatFomFile(statsFiles[k].getPath(), thaStatName, theStatType);
									if(theValue != -1){
										StatsUtils.writeToFile(theStatsFolderPath + "/" + theStatsFileName + "-AllGames.csv", gameKey + ";" + theValue + ";");
										StatsUtils.writeToFile(theStatsFolderPath + "/" + theStatsFileName + "-AllGames-Latex.csv", gameKey + "; $" + round(theValue, 2) + "$;");
									}
								}
							}
						}
					}
				}
			}
		}
	}



	/**
	 * E.G.: give (filename,avg,avg) to extract from the file the value that represents the average
	 * of the average values of each game.
	 *
	 * @param theFileName
	 * @param statisticName
	 * @param statisticType
	 */
	private static double extractStatFomFile(String theFileName, String statisticName, String statisticType){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;
		int statIndex = -1;
		double returnValue = -1;

		try {
			br = new BufferedReader(new FileReader(theFileName));

			// Read header
			theLine = br.readLine();

			if(theLine != null){

				splitLine = theLine.split(";");

				for(int i = 0; i < splitLine.length; i++){
					if(splitLine[i].equalsIgnoreCase(statisticType)){
						statIndex = i;
					}
				}

				if(statIndex != -1){

					theLine = br.readLine();

					while(theLine != null){
						// For each line, parse the parameters and add them to their statistic
						splitLine = theLine.split(";");

						if(splitLine.length >= 1 && splitLine[0].equalsIgnoreCase(statisticName)){

							if(splitLine.length > statIndex){
								returnValue = Double.parseDouble(splitLine[statIndex]);
								break;
							}
						}

						theLine = br.readLine();
					}
				}
			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + theFileName + ".");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + theFileName + ".");
					ioe.printStackTrace();
				}
        	}
		}

		return returnValue;

	}

	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

}

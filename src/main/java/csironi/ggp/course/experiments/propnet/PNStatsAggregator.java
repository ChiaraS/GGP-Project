package csironi.ggp.course.experiments.propnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import csironi.ggp.course.statsSummarizer.ColumnType;

public class PNStatsAggregator {

	/**
	 * Gets as input the path of the folder for the tested StateMahcine containing a folder for each game, each of which contains
	 * a folder for each repetition of the test for the game and a file with one entry per repetition showing the statistics of the
	 * repetition (i.e. PN building time, nodes/sec, iterations/sec, num components,...). This method, for each game computes the
	 * statistics over all the repetitions of the test saving them in a file in the folder of the game. Moreover, it aggregates all
	 * the statistics for all games in another cumulative file, if its path is specified as input.
	 * @param args
	 */
	public static void main(String[] args){

		if(args.length < 1 || args.length > 2){
			System.out.println("At least one argument expected: specify the name of the folder that contains the game statistic files to be aggregated and optionally the file where to save the cumulative statistics.");
			return;
		}

		String theMainFolderPath = args[0];
		String theCumulativeFilePath = null;

		if(args.length == 2){
			theCumulativeFilePath = args[1];
		}

		File theMainFolder = new File(theMainFolderPath);

		if(!theMainFolder.exists() || !theMainFolder.isDirectory()){
			System.out.println("Couldn't find main folder to summarize.");
			return;
		}

		// Check if the file with the aggregated statistics already exists. If so, delete it since we have to re-compute it
		File theDestinationFile = new File(theMainFolderPath + "/" + theMainFolder.getName() + "-AggregatedStatistics.csv");
		if(theDestinationFile.exists() && theDestinationFile.isFile()){
			theDestinationFile.delete();
		}

		File[] theSubFiles = theMainFolder.listFiles();

		SingleValueDoubleStats[] statsOfAverages = null;
		SingleValueDoubleStats[] statsOfConfidenceIntervals = null;

		String gameKey;
		File[] theCSVFile;
		FilenameFilter theFilter = new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".csv");
			}
		};
		String theHeader = "";
		ColumnType[] columnsFormat = null;
		String emptyLine = "";

		for(File currentFile : theSubFiles){
			if(currentFile.isDirectory()){
				gameKey = currentFile.getName();
				theCSVFile = currentFile.listFiles(theFilter);
				// If we find more than one .csv file we don't know which one to summarize
				if(theCSVFile.length > 1){
					System.out.println("Found more than one .csv file for game " + gameKey + ". Don't know which one to summmarize. Skipping game summarization.");
					continue;
				}else if(theCSVFile.length == 0){
					System.out.println("Found no .csv file for game " + gameKey + ". Skipping game summarization.");
					continue;
				}

				// If the destination file for the statistics doesn't exist yet, we must create it and insert the header line
				if(!theDestinationFile.exists()){
					BufferedReader br;
					try {
						br = new BufferedReader(new FileReader(theCSVFile[0]));
						// Read header
						theHeader = br.readLine();
					} catch (IOException e) {
						System.out.println("Exception when reading the header of the .csv file for the game " + gameKey + ".");
						System.out.println("Skipping game summarization.");
			        	e.printStackTrace();
			        	continue;
					}

					try {
						br.close();
					} catch (IOException e) {
						System.out.println("Exception when closing the .csv file reader for the game " + gameKey + ".");
						e.printStackTrace();
					}

					String[] theSplitHeader = theHeader.split(";");

					if(theSplitHeader.length < 1){
						System.out.println("Wrong format of the header of the .csv file for the game " + gameKey + ".");
						System.out.println("Skipping game summarization.");
			        	continue;
					}

					columnsFormat = new ColumnType[theSplitHeader.length];

					statsOfAverages = new SingleValueDoubleStats[theSplitHeader.length];
					statsOfConfidenceIntervals = new SingleValueDoubleStats[theSplitHeader.length];

					for(int i = 0; i < theSplitHeader.length; i++){
						emptyLine += ";";
						if(theSplitHeader[i].equals("MCSIterationsPerSecond") ||
								theSplitHeader[i].equals("MCSNodesPerSecond") ||
								theSplitHeader[i].equals("MCTSIterationsPerSecond") ||
								theSplitHeader[i].equals("MCTSNodesPerSecond")){
							columnsFormat[i] = ColumnType.DOUBLE;
						}else{
							columnsFormat[i] = ColumnType.LONG;
						}

						statsOfAverages[i] = new SingleValueDoubleStats();
						statsOfConfidenceIntervals[i] = new SingleValueDoubleStats();
					}

					try {
						theDestinationFile.createNewFile();
					} catch (IOException e) {
						System.out.println("Couldn't create the file where to aggregate the statistics.");
						System.out.println("Interrupting summarization.");
						e.printStackTrace();
						return;
					}

					// Write the header on the file
					writeToFile(theDestinationFile.getAbsolutePath(), "GameKey;Statistic;"+theHeader);
					writeToFile(theDestinationFile.getAbsolutePath(), ";;" + emptyLine);
				}

				if(summarizeSingleFile(gameKey, theCSVFile[0], theHeader, columnsFormat, theDestinationFile.getAbsolutePath(), statsOfAverages, statsOfConfidenceIntervals)){
					writeToFile(theDestinationFile.getAbsolutePath(), ";;" + emptyLine);
				}
			}
		}

		// Log in one general file the averages of the averages and the confidence interval for all games
		if(statsOfAverages != null && statsOfConfidenceIntervals != null && theCumulativeFilePath != null){

			try{
				File theCumulativeFile = new File(theCumulativeFilePath);

				if(!theCumulativeFile.exists()){
					theCumulativeFile.createNewFile();
					writeToFile(theCumulativeFilePath, "SMType;Statistic;" + theHeader);
				}else{
					BufferedReader br = null;
					String theLine;
					try {
						br = new BufferedReader(new FileReader(theCumulativeFile));
						// Read header
						theLine = br.readLine();
						if(!theLine.equals("SMType;Statistic;" + theHeader)){
							System.out.println("This statistics don't have the same header as the file where to write the cumulative statistics.");
							System.out.println("Dropping cumulative statistics.");
							if(br != null){
					        	try {
									br.close();
								} catch (IOException ioe) {
									System.out.println("Exception when closing the cumulative statistics file.");
									ioe.printStackTrace();
								}
				        	}
							return;
						}
						if(br != null){
				        	try {
								br.close();
							} catch (IOException ioe) {
								System.out.println("Exception when closing the cumulative statistics file.");
								ioe.printStackTrace();
							}
			        	}
					} catch (IOException e) {
						System.out.println("Exception when reading the header of the cumulative statistics file.");
						System.out.println("Dropping cumulative statistics.");
			        	e.printStackTrace();
			        	if(br != null){
				        	try {
								br.close();
							} catch (IOException ioe) {
								System.out.println("Exception when closing the cumulative statistics file.");
								ioe.printStackTrace();
							}
			        	}
			        	return;
					}

				}

				writeToFile(theCumulativeFilePath, ";;" + emptyLine);

				String smType = theMainFolder.getName();

				String[] toWrite = new String[]{smType+";AVG_AVG;",smType+";AVG_CI;"};

				for(int i = 0; i < statsOfAverages.length; i++){
					toWrite[0] += statsOfAverages[i].getAvgValue() + ";";
					toWrite[1] += statsOfConfidenceIntervals[i].getAvgValue() + ";";
				}

				for(int i = 0; i < toWrite.length; i++){
					writeToFile(theCumulativeFilePath, toWrite[i]);
				}
			}catch(IOException e){
				System.out.println("Couldn't create the file where to write the cumulative statistics.");
				System.out.println("Dropping cumulative statistics.");
				e.printStackTrace();
				return;
			}

		}

	}

	private static boolean summarizeSingleFile(String gameKey, File theCSVFile, String theHeader, ColumnType[] columnsFormat, String destinationFileName, SingleValueDoubleStats[] statsOfAverages, SingleValueDoubleStats[] statsOfConfidenceIntervals){

		if(gameKey == null || theCSVFile == null || theHeader == null || columnsFormat == null ||destinationFileName == null || statsOfAverages == null || statsOfConfidenceIntervals == null){
			System.out.println("Detected some null input parameters for the summarization of the .csv file for the game " + gameKey + ".");
			System.out.println("Skipping summarization of the file.");
         	return false;
		}

		BufferedReader br = null;
		String theLine;
		try {
			br = new BufferedReader(new FileReader(theCSVFile));
			// Read header
			theLine = br.readLine();
		} catch (IOException e) {
			System.out.println("Exception when reading the header of the .csv file for the game " + gameKey + ".");
			System.out.println("Skipping summarization of the file.");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file reader for the game " + gameKey + ".");
					ioe.printStackTrace();
				}
        	}
        	return false;
		}

		// Check if the file contains the appropriate stats (comparing the header with the one of the main aggregated stats file)
		if(!theLine.equals(theHeader)){

			System.out.println("The .csv file for the game " + gameKey + " doesn't contain statistics in the correct format.");
			System.out.println("Skipping game summarization.");
			try {
				br.close();
			} catch (IOException e) {
				System.out.println("Exception when closing the .csv file reader for the game " + gameKey + ".");
				e.printStackTrace();
			}
        	return false;

		}

		theLine = "";
		// Get the first readable line (if any)
		while(theLine != null && theLine.equals("")){
			try {
				theLine = br.readLine();
			} catch (IOException e) {
				System.out.println("Exception when reading a line in the .csv file for the game " + gameKey + ".");
				System.out.println("Skipping to the next line.");
				e.printStackTrace();
				theLine = "";
			}
		}

		if(theLine == null){ // No readable line found in the file
			System.out.println("No readable lines in the .csv file for the game " + gameKey + ".");
			System.out.println("Skipping game summarization.");
			try {
				br.close();
			} catch (IOException e) {
				System.out.println("Exception when closing the .csv file reader for the game " + gameKey + ".");
				e.printStackTrace();
			}
			return false;
		}

		// Create the statistics class for each column
		SingleValueStats[] allStats = new SingleValueStats[columnsFormat.length];

		for(int i = 0; i < allStats.length; i++){
			switch(columnsFormat[i]){
				case LONG:
					allStats[i] = new SingleValueLongStats();
					break;
				case DOUBLE:
					allStats[i] = new SingleValueDoubleStats();
					break;
				default:
					System.out.println("Detected invalid column format for game " + gameKey + ".");
					System.out.println("Skipping game summarization.");
					try {
						br.close();
					} catch (IOException e) {
						System.out.println("Exception when closing the .csv file reader for the game " + gameKey + ".");
						e.printStackTrace();
					}
		        	return false;
			}
		}

		String[] columnsStringValues;
		Number[] columnsValues = new Number[columnsFormat.length];

		while(theLine != null){

			columnsStringValues = theLine.split(";");

			if(columnsStringValues.length != columnsFormat.length){
				System.out.println("Detected line in the .csv file for the game " + gameKey + " that doesn't contain statistics in the correct format.");
				System.out.println("Skipping to the next line.");
			}else{

				try{
					// Read and parse the next line.
					for(int i = 0; i < columnsStringValues.length; i++){

						switch(columnsFormat[i]){
						case LONG:
							columnsValues[i] = Long.parseLong(columnsStringValues[i]);
							break;
						case DOUBLE:
							columnsValues[i] = Double.parseDouble(columnsStringValues[i]);
							break;
						default:
							System.out.println("Detected invalid column format for game " + gameKey + ".");
							System.out.println("Skipping game summarization.");
							try {
								br.close();
							} catch (IOException e) {
								System.out.println("Exception when closing the .csv file reader for the game " + gameKey + ".");
								e.printStackTrace();
							}
				        	return false;
						}
					}

					// If the line was read and parsed successfully, add its values to the corresponding statistics
					for(int i = 0; i < columnsValues.length; i++){

						switch(columnsFormat[i]){
						case LONG:
							((SingleValueLongStats)allStats[i]).addValue((Long)columnsValues[i]);
							break;
						case DOUBLE:
							((SingleValueDoubleStats)allStats[i]).addValue((Double)columnsValues[i]);
							break;
						default:
							System.out.println("Detected invalid column format for game " + gameKey + ".");
							System.out.println("Skipping game summarization.");
							try {
								br.close();
							} catch (IOException e) {
								System.out.println("Exception when closing the .csv file reader for the game " + gameKey + ".");
								e.printStackTrace();
							}
				        	return false;
						}
					}

				}catch(NumberFormatException e){
					System.out.println("Detected line in the .csv file for the game " + gameKey + " that doesn't contain statistics in the correct format.");
					System.out.println("Skipping to the next line.");
					e.printStackTrace();
				}

			}

			theLine = "";
			// Get the next readable line (if any)
			while(theLine != null && theLine.equals("")){
				try {
					theLine = br.readLine();
				} catch (IOException e) {
					System.out.println("Exception when reading a line in the .csv file for the game " + gameKey + ".");
					System.out.println("Skipping to the next line.");
					e.printStackTrace();
					theLine = "";
				}
			}
		}

		try {
			br.close();
		} catch (IOException e) {
			System.out.println("Exception when closing the .csv file reader for the game " + gameKey + ".");
			e.printStackTrace();
		}

		// Check if any of the statistics class has no samples (if so, all of them have no samples)
		if(allStats[0].isEmpty()){
			System.out.println("Couldn't collect any statistic for the game " + gameKey + ".");
			System.out.println("Skipping game summarization..");
			return false;
		}

		//printValues(allStats);

		// Write everything on file
		String[] toWrite = new String[]{gameKey+";"+"#Samples;",gameKey+";"+"MIN;",gameKey+";"+"MAX;",gameKey+";"+"SD;",gameKey+";"+"SEM;",gameKey+";"+"AVG;",gameKey+";"+"CI;"};

		for(int i = 0; i < allStats.length; i++){
			toWrite[0] += allStats[i].getNumSamples() + ";";
			toWrite[1] += allStats[i].getMinValue() + ";";
			toWrite[2] += allStats[i].getMaxValue() + ";";
			toWrite[3] += allStats[i].getValuesStandardDeviation() + ";";
			toWrite[4] += allStats[i].getValuesSEM() + ";";
			toWrite[5] += allStats[i].getAvgValue() + ";";
			toWrite[6] += allStats[i].get95ConfidenceInterval() + ";";


			// Save average of the statistic for all games
			statsOfAverages[i].addValue(allStats[i].getAvgValue());
			statsOfConfidenceIntervals[i].addValue(allStats[i].get95ConfidenceInterval());
		}

		for(int i = 0; i < toWrite.length; i++){
			writeToFile(destinationFileName, toWrite[i]);
		}

		return true;

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

	private static void printValues(SingleValueStats[] allStats){

		if(allStats == null){
			System.out.println("Null all stats");
			return;
		}
		System.out.println("Printing stats values:");
		for(int i = 0; i < allStats.length; i++){
			System.out.println("Printning stats " + i);
			if(allStats[i] instanceof SingleValueLongStats){
				System.out.println("Long stats");
				System.out.println(((SingleValueLongStats)allStats[i]).getValues());
			}else if(allStats[i] instanceof SingleValueDoubleStats){
				System.out.println("Double stats");
				System.out.println(((SingleValueDoubleStats)allStats[i]).getValues());
			}else{
				System.out.println("Detected wrong type of stats " + allStats[i].getClass().getSimpleName());
			}

		}
	}
}

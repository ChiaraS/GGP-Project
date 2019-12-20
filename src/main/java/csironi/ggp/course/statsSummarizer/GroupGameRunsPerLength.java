package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GroupGameRunsPerLength {

	/**
	 * Given a folder containing folders for each game run played by an agent for a fixed game, this class groups such folders depending on the
	 * length of the game run.
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length != 1) {
			System.out.println("Wrong number of arguments. Expecting the following inputs: [mainFolderPath].");
			return;
		}

		File mainLogFolder = new File(args[0]);

		for(File gameRunFolder : mainLogFolder.listFiles()) {

			File[] gameRunLogs = gameRunFolder.listFiles();

			for(File gameRunLog : gameRunLogs) {

				if(gameRunLog.getName().equals("Stats.csv")) {

					int turns = extractTurns(gameRunLog);

					File destinationFolder = new File(mainLogFolder + "/Length" + turns);

					if(!destinationFolder.exists()) {
						destinationFolder.mkdirs();
					}

					StatsUtils.move(gameRunFolder, new File(destinationFolder.getPath() + "/" + gameRunFolder.getName()));

				}

			}

		}

	}

	/**
	 * Given a file with the game stats (Stats.csv) extract the number of played turns for the game run.
	 * @param statsFile
	 * @return
	 */
	private static int extractTurns(File statsFile) {

		BufferedReader br = null;
		String theLine;
		String[] splitLine;
		int step = -1;
		int currentStep;

		try {
			br = new BufferedReader(new FileReader(statsFile));

			// Read header
			theLine = br.readLine();

			if(theLine != null){

				splitLine = theLine.split(";");

				if(splitLine[0].equals("Game step")) {

					theLine = br.readLine();

					while(theLine != null){
						// For each line, parse the statistic and add it to the map
						splitLine = theLine.split(";");

						if(splitLine.length > 0){

							currentStep = Integer.parseInt(splitLine[0]);

							if(currentStep < 0) {
								System.out.println("Found negative statistic for step " + step + " in Stats file " + statsFile.getPath() + "!");
							}

							if(currentStep > step) {
								step = currentStep;
							}

						}

						theLine = br.readLine();
					}

				}

			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + statsFile.getPath() + ".");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + statsFile.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
		}

		return step;

	}

}

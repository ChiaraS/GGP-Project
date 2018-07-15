package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import csironi.ggp.course.experiments.propnet.SingleValueDoubleStats;
import csironi.ggp.course.experiments.propnet.SingleValueLongStats;
import csironi.ggp.course.experiments.propnet.SingleValueStats;

public class StatsExtractor {

	private File theCSVFile;

	private ColumnType[] columnTypes;

	private int[] columnIndices;

	private String[] columnHeaders;

	private Map<String, SingleValueStats> allStats;

	public StatsExtractor(File theCSVFile,  String[] columnHeaders, ColumnType[] columnTypes, int[] columnIndices){

		if(columnHeaders.length == columnTypes.length && columnHeaders.length == columnIndices.length){

			 this.theCSVFile = theCSVFile;

			 this.columnTypes = columnTypes;

			 this.columnIndices = columnIndices;

			 this.columnHeaders =  columnHeaders;

			 this.allStats = new HashMap<String, SingleValueStats>();

			 for(int i = 0; i < this.columnHeaders.length; i++){

				 switch(columnTypes[i]){
					case LONG:
						this.allStats.put(columnHeaders[i], new SingleValueLongStats());
						break;
					case DOUBLE:
						this.allStats.put(columnHeaders[i], new SingleValueDoubleStats());
						break;
					default:
						System.out.println("Detected invalid column format for the .csv file " + this.theCSVFile.getName() + ".");
						System.out.println("Skipping summarization of the file.");
						this.allStats = null;
						return;
				}
			 }

			 this.extractStats();

		}else{
			this.allStats = null;
		}

	}

	private void extractStats(){

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		try {
			br = new BufferedReader(new FileReader(this.theCSVFile));
			// Read header
			theLine = br.readLine();
			splitLine = theLine.split(";");

			if(splitLine.length < this.columnIndices[this.columnIndices.length-1]){
				System.out.println("Detected invalid column index for the .csv file " + this.theCSVFile.getName() + ".");
				System.out.println("Skipping summarization of the file.");
				this.allStats = null;
				br.close();
				return;
			}

			// Check that the names of the columns to extract are correct
			for(int i = 0; i < this.columnIndices.length; i++){
				if(!this.columnHeaders[i].equals(splitLine[this.columnIndices[i]])){
					System.out.println("Detected invalid column header for the .csv file " + this.theCSVFile.getName() + ".");
					System.out.println("Skipping summarization of the file.");
					this.allStats = null;
					br.close();
					return;
				}
			}

			// NOTE: when the MCTS player doesn't have enough time to perform the search for a step, or when it is set to not perform
			// metagaming search, it logs speed stats anyway, using -1 for the stats that couldn't be computed (e.g. nodes/second,
			// iterations/second) and for the search time. Moreover, if the MCTS player starts the search with the MCTS manager, but
			// the MCTS manager realizes there is not enough time to perform any search,then in the stats we will have a 0 value for
			// the search time (and other statistics). In these cases, we should avoid considering these stats as samples, otherwise
			// we'll have wrong data in the aggregated statistics (i.e. they don't count as slow simulations samples, because the search
			// has actually never been performed).

			boolean checkInvalid = false;

			if(this.theCSVFile.getName().endsWith("-Stats.csv")){ // Check if we are summarizing the SpeedStats
				if(splitLine[2].equals("Search time(ms)")){ // This second check is just to make sure we consider the correct column for distinguishing invalid samples. Officially shouldn't be necessary to check this.

					// If we are summarizing the SpeedStats we must remember to check for invalid samples for each line
					checkInvalid = true;

				}else{
					System.out.println("Detected invalid column header for the Search time(ms) column in the speed statistics .csv file " + this.theCSVFile.getName() + ".");
					System.out.println("Skipping summarization of the file.");
					this.allStats = null;
					br.close();
					return;
				}

			}

			// Read first line with data
			theLine = br.readLine();

			// Check first line with data: if it refers to step 0 it means that data refers to the search performed
			// only in the metagame. These data must be excluded from the stats because this data is already included
			// in the data for the search of the first step.
			if(this.theCSVFile.getName().endsWith("-Stats.csv")){ // Check if we are summarizing the SpeedStats
				splitLine = theLine.split(";");
				int step;
				try{
					step = Integer.parseInt(splitLine[0]);

					if(step == 0){
						theLine = br.readLine();
					}
				}catch(NumberFormatException e){ // The search time must be a long number, if it's not there is some error
					System.out.println("Detected invalid value for the \"Game step\" for the .csv file " + this.theCSVFile.getName() + ".");
					System.out.println("Skipping summarization of the file.");
					this.allStats = null;
					br.close();
					return;
				}
			}

			while(theLine != null){
				// For each line, parse the parameters and add them to their statistic
				splitLine = theLine.split(";");

				// Check if we have to check for invalid stats
				if(checkInvalid){

					//int gameStep;
					long searchTime;

					//try{
					//	gameStep = Integer.parseInt(splitLine[0]);
					//}catch(NumberFormatException e){ // The game step must be an integer number, if it's not there is some error
					//	System.out.println("Detected invalid value for the \"Game step\" for the .csv file " + this.theCSVFile.getName() + ".");
					//	System.out.println("Skipping summarization of the file.");
					//	this.allStats = null;
					//	br.close();
					//	return;
					//}

					try{
						searchTime = Long.parseLong(splitLine[2]);
					}catch(NumberFormatException e){ // The search time must be an long number, if it's not there is some error
						System.out.println("Detected invalid value for the \"Search time(ms)\" for the .csv file " + this.theCSVFile.getName() + ".");
						System.out.println("Skipping summarization of the file.");
						this.allStats = null;
						br.close();
						return;
					}

					if(searchTime <= 0L){
						theLine = br.readLine();
						continue;
					}
				}


				for(int i = 0; i < this.columnIndices.length; i++){

					// Collect only logs of first 20 steps of each game for Expanded nodes
					if(this.columnHeaders[i].equals("Added nodes") && Integer.parseInt(splitLine[0]) > 10) {
						continue;
					}

					if(this.columnHeaders[i].equals("Memorized states") && Integer.parseInt(splitLine[0]) > 10) {
						continue;
					}

					switch(this.columnTypes[i]){
					case LONG:
						((SingleValueLongStats)this.allStats.get(this.columnHeaders[i])).addValue(Long.parseLong(splitLine[this.columnIndices[i]]));
						break;
					case DOUBLE:
						((SingleValueDoubleStats)this.allStats.get(this.columnHeaders[i])).addValue(Double.parseDouble(splitLine[this.columnIndices[i]]));
						break;
					default:
						System.out.println("Detected invalid column format for the .csv file " + this.theCSVFile.getName() + ".");
						System.out.println("Skipping summarization of the file.");
						this.allStats = null;
						br.close();
						return;
					}

				}

				theLine = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the header of the .csv file " + this.theCSVFile.getName() + ".");
			System.out.println("Skipping summarization of the file.");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + this.theCSVFile.getName() + ".");
					ioe.printStackTrace();
				}
        	}
        	this.allStats = null;
        	return;
		}

	}

	public Map<String,SingleValueStats> getExtractedStats(){

		return this.allStats;
	}


}

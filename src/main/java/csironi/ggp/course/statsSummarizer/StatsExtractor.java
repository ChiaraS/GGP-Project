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

			// Read first line with data
			theLine = br.readLine();

			while(theLine != null){
				// For each line, parse the parameters and add them to their statistic
				splitLine = theLine.split(";");
				for(int i = 0; i < this.columnIndices.length; i++){

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

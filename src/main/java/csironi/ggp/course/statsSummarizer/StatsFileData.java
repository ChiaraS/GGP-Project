package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatsFileData {

	private String matchID;

	private String fileHeader;

	private List<String> fileLines;

	public StatsFileData(File theStatsFile) {

		this.matchID = theStatsFile.getName().split("-")[0];

		this.fileLines = new ArrayList<String>();

		BufferedReader br;
		String theLine;
		String[] splitLine;
		try {
			br = new BufferedReader(new FileReader(theStatsFile));

			// First read file header assuming it's in the first line of the file.
			this.fileHeader = br.readLine();

			theLine = br.readLine();

			while(theLine != null){
				splitLine = theLine.split(";");
				if(splitLine.length > 0){
					this.fileLines.add(theLine);
				}
				theLine = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading a file in the match log folder.");
        	e.printStackTrace();
        	return;
		}

	}

	public StatsFileData(String fileHeader, List<String> fileLines) {

		this.matchID = null;

		this.fileHeader = fileHeader;

		this.fileLines = fileLines;

	}

	public void addLines(List<String> theLines) {
		this.fileLines.addAll(theLines);
	}

	public String getMatchID() {
		return this.matchID;
	}

	public String getFileHeader() {
		return this.fileHeader;
	}

	public List<String> getFileLines() {
		return this.fileLines;
	}

	public String toLogs() {

		String toLog = this.fileHeader;

		for(String s : this.fileLines) {
			toLog += "\n" + s;
		}

		return toLog;

	}

}

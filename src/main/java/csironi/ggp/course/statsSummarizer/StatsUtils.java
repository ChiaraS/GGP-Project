package csironi.ggp.course.statsSummarizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StatsUtils {

	public static void writeToFile(String filename, String message){
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

	public static void writeToFileMkParentDir(String filename, String message){

		File destinationFile = new File(filename);
		if(!destinationFile.getParentFile().isDirectory()){
			destinationFile.getParentFile().mkdirs();
		}

		writeToFile(filename, message);

	}

}

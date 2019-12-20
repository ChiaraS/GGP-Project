package csironi.ggp.course.statsSummarizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

	public static void move(File sourceFile, File destFile){

		//System.out.println("Moving " + sourceFile.getPath() + " to " + destFile.getPath());

	    if (sourceFile.isDirectory()){

	    	if(!destFile.exists()) {
	    		destFile.mkdirs();
	    	}

	        for (File file : sourceFile.listFiles()){

	            move(file, new File(destFile + "/" + file.getName()));

	        }

	    }else{

	        try {
	            Files.move(Paths.get(sourceFile.getPath()), Paths.get(destFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
	        } catch (IOException e) {
	        	System.out.println("Error when moving file " + sourceFile.getPath() + "!");
	        	e.printStackTrace();
	        }
	    }
	}

	public static void main(String[] args) {

		move(new File("C:\\Users\\c.sironi\\RES\\GGP\\ImprovedFPGAComparison\\Prova\\From"), new File("C:\\Users\\c.sironi\\RES\\GGP\\ImprovedFPGAComparison\\Prova\\To"));

	}

}

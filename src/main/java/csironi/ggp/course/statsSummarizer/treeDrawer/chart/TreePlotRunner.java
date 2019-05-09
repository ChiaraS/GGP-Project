package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TreePlotRunner extends Thread {

	private List<String> theSettings;

	private String stdOutputErrorFolderPath;

	private String fileNameWithoutExtension;

	public TreePlotRunner(List<String> theSettings, String stdOutputErrorFolderPath, String fileNameWithoutExtension){
		this.theSettings = theSettings;
		this.stdOutputErrorFolderPath = stdOutputErrorFolderPath;
		this.fileNameWithoutExtension = fileNameWithoutExtension;
	}

	@Override
	public void run(){

		ProcessBuilder pb = new ProcessBuilder(this.theSettings);

		//System.out.println(pb.command());

		File stdOutputErrorFile = new File(this.stdOutputErrorFolderPath + "/" + fileNameWithoutExtension + ".log");

		if(!stdOutputErrorFile.exists()){
			try {
				stdOutputErrorFile.createNewFile();

				pb.redirectOutput(stdOutputErrorFile);
				pb.redirectError(stdOutputErrorFile);
			} catch (IOException e) {
				System.out.println("Impossible to redirect standard error and output. Cannot create the destination file " + stdOutputErrorFile.getPath() + ".");
		        e.printStackTrace();
			}
		}

		Process process = null;
		try {
			process = pb.start();

			process.waitFor();
		} catch (IOException e) {
			System.out.println("Impossible to start tree plot process for file " + this.fileNameWithoutExtension + ".csv.");
	        e.printStackTrace();
		}catch (InterruptedException e) {
			System.out.println("Tree plot process runner interrupted while waiting for single tree plot process to complete execution for file " + this.fileNameWithoutExtension + ".csv.");
	        e.printStackTrace();
			if(process != null){
				process.destroy();
			}
			Thread.currentThread().interrupt();
		}

	}

}

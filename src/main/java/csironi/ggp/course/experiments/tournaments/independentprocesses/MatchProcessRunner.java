package csironi.ggp.course.experiments.tournaments.independentprocesses;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.logging.GamerLogger;

import csironi.ggp.course.experiments.tournaments.ExternalGamerAvailabilityManager;

public class MatchProcessRunner extends Thread {

	private int matchID;

	private List<String> theSettings;

	private String stdOutputErrorFolderPath;

	private Map<String,ExternalGamerAvailabilityManager> externalGamersManagers;

	public MatchProcessRunner(int matchID, List<String> theSettings, String stdOutputErrorFolderPath, Map<String,ExternalGamerAvailabilityManager> externalGamersManagers){
		this.matchID = matchID;
		this.theSettings = theSettings;
		this.stdOutputErrorFolderPath = stdOutputErrorFolderPath;
		this.externalGamersManagers = externalGamersManagers;
	}

	@Override
	public void run(){

		//GamerLogger.log("MatchRunner" + this.matchID, "Creating process to run match " + this.matchID + ".");

		// !!GamerLogger.logError("MatchRunner", "No free address left! Make sure that there are enough active external players for the number of parallel matches and game roles!");


		ProcessBuilder pb = new ProcessBuilder(this.theSettings);

		//System.out.println(pb.command());

		File stdOutputErrorFolder = new File(this.stdOutputErrorFolderPath);
		if(!stdOutputErrorFolder.exists()){
			stdOutputErrorFolder.mkdirs();
		}

		File stdOutputErrorFile = new File(stdOutputErrorFolder  + "/ConsoleOutput.log");

		if(!stdOutputErrorFile.exists()){
			try {
				stdOutputErrorFile.createNewFile();

				pb.redirectOutput(stdOutputErrorFile);
				pb.redirectError(stdOutputErrorFile);
			} catch (IOException e) {
				// ATTENTION!: cannot log error or stack trace using GamerLogger here because it will write on the file
				// named Error.log, and other threads of this class might do the same. Possible fix: let GamerLogger always
				// log errors in a file named like the given file plus "-Error" added at the end.
				GamerLogger.log("MatchRunner" + this.matchID, "Impossible to redirect standard error and output. Cannot create the destination file.");
		        StringWriter s = new StringWriter();
		        e.printStackTrace(new PrintWriter(s));
		        GamerLogger.log("MatchRunner" + this.matchID, s.toString());
			}
		}

		Process process = null;
		try {
			process = pb.start();

			process.waitFor();
		} catch (IOException e) {
			// ATTENTION!: cannot log error or stack trace using GamerLogger here because it will write on the file
			// named Error.log, and other threads of this class might do the same. Possible fix: let GamerLogger always
			// log errors in a file named like the given file plus "-Error" added at the end.
			GamerLogger.log("MatchRunner" + this.matchID, "Impossible to start match process " + this.matchID + ".");
	        StringWriter s = new StringWriter();
	        e.printStackTrace(new PrintWriter(s));
	        GamerLogger.log("MatchRunner" + this.matchID, s.toString());
		}catch (InterruptedException e) {
			GamerLogger.log("MatchRunner" + this.matchID, "Match process runner interrupted while waiting for single match process to complete execution.");
	        StringWriter s = new StringWriter();
	        e.printStackTrace(new PrintWriter(s));
	        GamerLogger.log("MatchRunner" + this.matchID, s.toString());
			if(process != null){
				process.destroy();
			}
			Thread.currentThread().interrupt();
		}

	}

}

package csironi.ggp.course.experiments.propnet;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.logging.GamerLogger;

/**
 * Inputs this program gets:
 *
 * - NECESSARY INPUTS:
 * 		[mainLogFolder] = the outer folder containing all the logs and files produced by this program and its subprograms.
 * 		[gameKey] = the key of the game to test (use the string "ALL" to test all games in the repository at once).
 *
 * - OPTIONAL INPUTS (NOTE: you can either specify all or none of them, but not only part of them.):
 * 		[repetitions] = number of times a single test has to be run for each game. (Default value: 100)
 * 		[givenInitTime] = the maximum time (in milliseconds) that the each test run has available to create the PropNet
 * 						  and perform the optimizations on it (if any is specified).
 * 						  This time is also used as limit for the state machine to initialize, but it isn't really
 * 						  taken into account since the state machine based on the PropNet receives the PropNet
 * 						  from outside only once it has been already created. (Default value: 420000ms)
 * 		[searchTime] = the amount of time (in milliseconds) each search test (MCS and MCTS test) must last. Both
 * 					   the MCS and the MCTS search tests will run the search from the root node for (approximately)
 * 					   this amount of time. (Default value: 60000ms)
 *  	[optimizations] = the optimizations that the PropNet manager must perform on the PropNet after creation.
 *  					  Each optimization corresponds to a number as follows:
 *  					  	0 = OptimizeAwayConstants
 *  						1 = RemoveAnonPropositions
 *  						2 = OptimizeAwayConstantValueComponents
 *  						3 = RemoveOutputlessComponents
 *  					  The optimizations to be performed must be specified with their corresponding numbers,
 *  					  separated by "-", in the order we want the manager to perform them (e.g. the input "0-1-2-3"
 *  					  will make the manager perform optimization 0, followed by optimization 1, followed by
 *  					  optimization 2, followed by optimization 3). To let the manager perform no optimizations
 *  					  give the string "null" as argument. (Default value: "null")
 *  	[withCache] = true if the state machine based on the propnet must use the cache, false otherwise. (Default
 *  				  value: false)
 *
 *
 * @author C.Sironi
 *
 */
public class PropnetTester {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args){

		if(args.length < 2){
			System.out.println("[PropnetTester] Impossible to run program. Specify at least a main log folder and the key of the game to be tested.");
			return;
		}

    	String mainLogFolder = args[0];
    	String gameKey = args[1];

    	ThreadContext.put("LOG_FOLDER", mainLogFolder);

    	GamerLogger.startFileLogging();

    	GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "Logger", "");
    	GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "PropNetTester", "");

    	GamerLogger.log("PropNetTester", "Starting PropNet tester.");

    	int repetitions = 100;
       	long givenInitTime = 420000L;
    	long searchTime = 60000L;
    	String optimizationsString = "null";
    	boolean withCache = false;

    	if(args.length == 7){
    		try{
    			repetitions = Integer.parseInt(args[2]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("PropNetTester", "Inconsistent number of test repetitions! Using default value.");
				repetitions = 100;
			}
	    	try{
				givenInitTime = Long.parseLong(args[3]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("PropNetTester", "Inconsistent maximum initialization time specification! Using default value.");
				givenInitTime = 420000L;
			}
			try{
				searchTime = Long.parseLong(args[4]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("PropNetTester", "Inconsistent search time specification! Using default value.");
				searchTime = 60000L;
			}

			optimizationsString = args[5];

			withCache = Boolean.parseBoolean(args[6]);

    	}

    	if(gameKey.equals("ALL")){
    		GamerLogger.log("PropNetTester", "Running PropNet test for ALL games.");
    	}else{
    		GamerLogger.log("PropNetTester", "Running PropNet test for game " + gameKey + ".");
    	}
    	GamerLogger.log("PropNetTester", "Logging in folder " + mainLogFolder + ".");

    	String testSettings = "Settings for current tests:\n";
    	testSettings += "[repetitions] = " + repetitions + "\n";
    	testSettings += "[givenInitTime] = " + givenInitTime + "\n";
    	testSettings += "[searchTime] = " + searchTime + "\n";
    	testSettings += "[optimizations] = " + optimizationsString + "\n";
    	testSettings += "[withCache] = " + withCache + "\n";

    	GamerLogger.log("PropNetTester", testSettings);

    	if(gameKey.equals("ALL")){

    	    //GameRepository theRepository = GameRepository.getDefaultRepository();

    	    GameRepository theRepository = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

    	    for(String aGameKey : theRepository.getGameKeys()) {
    	        if(aGameKey.contains("laikLee")) continue;

    	        testGame(mainLogFolder, aGameKey, repetitions, givenInitTime, searchTime, optimizationsString, withCache);

    	    }

    	}else{
    		testGame(mainLogFolder, gameKey, repetitions, givenInitTime, searchTime, optimizationsString, withCache);
    	}

	}

	private static void testGame(String mainLogFolder, String gameKey, int repetitions, long givenInitTime, long searchTime, String optimizationsString, boolean withCache){

		String gameFolder = mainLogFolder + "/" + gameKey;

    	ThreadContext.put("LOG_FOLDER", gameFolder);

    	String toLog = "Starting PropNet test for game " + gameKey + " with following settings:";
    	toLog += "\n[repetitions] = " + repetitions;
    	toLog += "\n[givenInitTime] = " + givenInitTime;
    	toLog += "\n[searchTime] = " + searchTime;
    	toLog += "\n[optimizations] = " + optimizationsString;
    	toLog += "\n[withCache] = " + withCache;

    	GamerLogger.log("PropnetTester", toLog);

		String singleRunID = null;
		String singleRunFolder = null;
		File logFile = null;
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "SingleRunPNTest.jar", gameFolder, singleRunFolder, gameKey, ""+givenInitTime, ""+searchTime, optimizationsString, ""+withCache);

		//System.out.println(pb.command());

		for(int i = 0; i < repetitions; i++){

			Process process = null;

			try{

				singleRunID = System.currentTimeMillis()+"-"+gameKey;
				singleRunFolder = gameFolder + "/" + singleRunID;

				GamerLogger.log("PropnetTester", "Starting single run " + singleRunID + ".");

				File logFolder = new File(singleRunFolder);
				if(!logFolder.exists()){
					logFolder.mkdirs();
				}

				logFile = new File(singleRunFolder + "/ConsoleOutput.log");

				if(!logFile.exists()){
					logFile.createNewFile();
				}

				pb.command().set(4, singleRunFolder);

				//System.out.println(pb.command());

				pb.redirectOutput(logFile);
				pb.redirectError(logFile);

				process = pb.start();

				process.waitFor();

				if(process.exitValue() != 0){
					GamerLogger.logError("PropnetTester", "Something went wrong when running single run " + singleRunID + " with the following arguments: " + pb.command() + ".");
				}else{
					GamerLogger.log("PropnetTester", "Correctly completed single run " + singleRunID + ".");
				}
			}catch(IOException ioe){
				GamerLogger.logError("PropnetTester", "Error setting up and starting the process that runs single run of test " + singleRunID + ".");
				GamerLogger.logStackTrace("PropnetTester", ioe);
			}catch(InterruptedException ie){
				GamerLogger.logError("PropnetTester", "Test interrupted while waiting for single test run process to complete execution for test " + singleRunID + ".");
				GamerLogger.logStackTrace("PropnetTester", ie);
				if(process != null){
					process.destroy();
				}
				Thread.currentThread().interrupt();
			}
		}

		GamerLogger.log("PropnetTester", "Test for game " + gameKey + " ended.");
	}


}

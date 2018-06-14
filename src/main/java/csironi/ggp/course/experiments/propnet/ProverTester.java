package csironi.ggp.course.experiments.propnet;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.logging.GamerLogger;

/**
 * NOTE!: this class must be updated to run the SingleRunProverTester.java class, because the class has been modified.
 *
 * Inputs this program gets:
 *
 * - NECESSARY INPUTS:
 * 		[mainLogFolder] = the outer folder containing all the logs and files produced by this program and its subprograms.
 * 		[gameKey] = the key of the game to test (use the string "ALL" to test all games in the repository at once).
 *
 * - OPTIONAL INPUTS (NOTE: you can either specify all or none of them, but not only part of them.):
 * 		[repetitions] = number of times a single test has to be run for each game. (Default value: 100)
 * 		[givenInitTime] = the maximum time (in milliseconds) that the each test run has available to create and
 * 						  initialize the state machine. (Default value: 420000ms)
 * 		[searchTime] = the amount of time (in milliseconds) each search test (MCS and MCTS test) must last. Both
 * 					   the MCS and the MCTS search tests will run the search from the root node for (approximately)
 * 					   this amount of time. (Default value: 60000ms)
 *  	[withCache] = true if the state machine based on the prover must use the cache, false otherwise. (Default
 *  				  value: false)
 * 		[cacheType] = specifies which version of the cache to use. Possible types are:
 *  					- old: implementation that equals the one already provided in this code base
 *  					- nosync: refactored cache that should spend less time searching entries in the
 *  							  cache and doesn't synchronize code and is not thread safe
 *
 *
 * @author C.Sironi
 *
 */
public class ProverTester {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args){

		if(args.length < 2){
			System.out.println("[ProverTester] Impossible to run program. Specify at least a main log folder and the key of the game to be tested.");
			return;
		}

    	String mainLogFolder = args[0];
    	String gameKey = args[1];

    	ThreadContext.put("LOG_FOLDER", mainLogFolder);

    	GamerLogger.startFileLogging();

    	GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "Logger", "");
    	GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "ProverTester", "");

    	GamerLogger.log("ProverTester", "Starting Prover tester.");

    	int repetitions = 100;
       	long givenInitTime = 420000L;
    	long searchTime = 60000L;
    	boolean withCache = false;
    	String cacheType = null;

    	if(args.length == 7){
    		try{
    			repetitions = Integer.parseInt(args[2]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("ProverTester", "Inconsistent number of test repetitions! Using default value.");
				repetitions = 100;
			}
	    	try{
				givenInitTime = Long.parseLong(args[3]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("ProverTester", "Inconsistent maximum initialization time specification! Using default value.");
				givenInitTime = 420000L;
			}
			try{
				searchTime = Long.parseLong(args[4]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("ProverTester", "Inconsistent search time specification! Using default value.");
				searchTime = 60000L;
			}

			withCache = Boolean.parseBoolean(args[5]);

			cacheType = args[6];

    	}

    	if(gameKey.equals("ALL")){
    		GamerLogger.log("ProverTester", "Running Prover test for ALL games.");
    	}else{
    		GamerLogger.log("ProverTester", "Running Prover test for game " + gameKey + ".");
    	}
    	GamerLogger.log("ProverTester", "Logging in folder " + mainLogFolder + ".");

    	String testSettings = "Settings for current tests:\n";
    	testSettings += "[repetitions] = " + repetitions + "\n";
    	testSettings += "[givenInitTime] = " + givenInitTime + "\n";
    	testSettings += "[searchTime] = " + searchTime + "\n";
    	testSettings += "[withCache] = " + withCache + "\n";
    	testSettings += "[cacheType] = " + cacheType + "\n";

    	GamerLogger.log("ProverTester", testSettings);

    	if(gameKey.equals("ALL")){

    	    //GameRepository theRepository = GameRepository.getDefaultRepository();

    	    GameRepository theRepository = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

    	    for(String aGameKey : theRepository.getGameKeys()) {
    	        if(aGameKey.contains("laikLee")) continue;

    	        testGame(mainLogFolder, aGameKey, repetitions, givenInitTime, searchTime, withCache, cacheType);

    	    }

    	}else{
    		testGame(mainLogFolder, gameKey, repetitions, givenInitTime, searchTime, withCache, cacheType);
    	}

	}

	private static void testGame(String mainLogFolder, String gameKey, int repetitions, long givenInitTime, long searchTime, boolean withCache, String cacheType){

		String gameFolder = mainLogFolder + "/" + gameKey;

    	ThreadContext.put("LOG_FOLDER", gameFolder);

    	String toLog = "Starting Prover test for game " + gameKey + " with following settings:";
    	toLog += "\n[repetitions] = " + repetitions;
    	toLog += "\n[givenInitTime] = " + givenInitTime;
    	toLog += "\n[searchTime] = " + searchTime;
    	toLog += "\n[withCache] = " + withCache;
    	toLog += "\n[cacheType] = " + cacheType;

    	GamerLogger.log("ProverTester", toLog);

		String singleRunID = null;
		String singleRunFolder = null;
		File logFile = null;
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "SingleRunProverTest.jar", gameFolder, singleRunFolder, gameKey, ""+givenInitTime, ""+searchTime, ""+withCache, cacheType);

		//System.out.println(pb.command());

		for(int i = 0; i < repetitions; i++){

			Process process = null;

			try{

				singleRunID = System.currentTimeMillis()+"-"+gameKey;
				singleRunFolder = gameFolder + "/" + singleRunID;

				GamerLogger.log("ProverTester", "Starting single run " + singleRunID + ".");

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
					GamerLogger.logError("ProverTester", "Something went wrong when running single run " + singleRunID + " with the following arguments: " + pb.command() + ".");
				}else{
					GamerLogger.log("ProverTester", "Correctly completed single run " + singleRunID + ".");
				}
			}catch(IOException ioe){
				GamerLogger.logError("ProverTester", "Error setting up and starting the process that runs single run of test " + singleRunID + ".");
				GamerLogger.logStackTrace("ProverTester", ioe);
			}catch(InterruptedException ie){
				GamerLogger.logError("ProverTester", "Test interrupted while waiting for single test run process to complete execution for test " + singleRunID + ".");
				GamerLogger.logStackTrace("ProverTester", ie);
				if(process != null){
					process.destroy();
				}
				Thread.currentThread().interrupt();
			}
		}

		GamerLogger.log("ProverTester", "Test for game " + gameKey + " ended.");
	}


}

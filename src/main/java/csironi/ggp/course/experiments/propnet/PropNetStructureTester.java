package csironi.ggp.course.experiments.propnet;

import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicPropNet;
import org.ggp.base.util.propnet.creationManager.PropNetManagerRunner;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.OptimizationCaller;
import org.ggp.base.util.propnet.factory.DynamicPropNetFactory;

/**
 * This class builds the propnet of the given game, logging the number of input-less components
 * detected during initialization and the number of detected legal and input propositions.
 *
 * @author C.Sironi
 *
 */
public class PropNetStructureTester {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}


	public static void main(String[] args){

		String gameToTest = null;

		long givenInitTime = 420000L;

		if(args.length == 2){
			gameToTest = args[0];
			try{
				givenInitTime = Long.parseLong(args[1]);
			}catch(NumberFormatException nfe){
				System.out.println("Inconsistent maximum initialization time specification! Using default value.");
				givenInitTime = 420000L;
			}
		}

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following time settings:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following time settings:");
		}
		System.out.println("Propnet building time: " + givenInitTime + "ms");

		/*************************** START TESTS ****************************/

    	ThreadContext.put("LOG_FOLDER", System.currentTimeMillis() + ".PnStructureTest");

    	GamerLogger.startFileLogging();

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "StructureCheckerSummary", "Game;#InputlessOR;#InputlessGOAL;#InputlessTERMINAL;#InputlessLEGAL;#InputlessNonINPUT;#InputlessOTHER;#InputlessBASE;#LEGALS;#possibleINPUTS;#addedINPUTS;ProperStructure");

		//GameRepository gameRepo = GameRepository.getDefaultRepository();

    	GameRepository gameRepo = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

    	for(String gameKey : gameRepo.getGameKeys()) {

	        if(gameKey.contains("laikLee")) continue;

	        // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
	        if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

	        List<Gdl> description = gameRepo.getGame(gameKey).getRules();

	        testGameStructure(gameKey, description, givenInitTime);
    	}

	}

	private static void testGameStructure(String gameKey, List<Gdl> description, long givenInitTime){

		GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "PropStructureChecker", "");

		GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "PropStructureChecker", gameKey + ":");

		SeparateInternalPropnetManager manager =  new SeparateInternalPropnetManager(description, System.currentTimeMillis() + givenInitTime, new OptimizationCaller[0]);

		PropNetManagerRunner.runPropNetManager(manager, givenInitTime);

		DynamicPropNet propnet = manager.getDynamicPropnet();

		if(propnet == null){
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "StructureCheckerSummary", gameKey + ";-1;-1;-1;-1;-1;-1;-1;-1;-1;-1;N.D;");
			GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "PropStructureChecker", "IMPOSSIBLE TO CHECK");
		}else{
			boolean properStructure = DynamicPropNetFactory.checkPropnetStructure(propnet);

			// UNCOMMENT THIS IF WANT TO RUN
			//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "StructureCheckerSummary", gameKey + ";" + propnet.inputlessOr + ";" + propnet.inputlessGoal + ";" + propnet.inputlessTerminal + ";" + propnet.inputlessLegal + ";" + propnet.inputlessNonInput + ";" + propnet.inputlessOther + ";" +  propnet.inputlessBase + ";" + propnet.numLegals + ";" + propnet.numPossibleInputs + ";" + propnet.numAddedInputs + ";" + properStructure + ";");
		}

	}

}

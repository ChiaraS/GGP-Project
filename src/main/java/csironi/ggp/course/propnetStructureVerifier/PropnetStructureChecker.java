/**
 *
 */
package csironi.ggp.course.propnetStructureVerifier;

import java.util.List;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingPropNet;
import org.ggp.base.util.propnet.factory.ForwardInterruptingPropNetFactory;
import org.ggp.base.util.propnet.factory.FwdInterrPropNetCreator;

/**
 * This class checks if the structure of the propnet is consistent (e.g. there are no gates with no input,
 * every proposition has the correct number of inputs given its type,...).
 *
 * It is possible to specify the following combinations of main arguments:
 *
 * 1. [keyOfGameToTest]
 * 2. [maximumPropnetConstructionTime] [maximumTestDuration]
 * 3. [maximumPropnetConstructionTime] [maximumTestDuration] [keyOfGameToTest]
 *
 * where:
 * [maximumPropnetConstructionTime] = time in milliseconds that is available to build and initialize
 * 									   the propnet (DEFAULT: 420000ms - 7mins).
 * [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
 * [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
 *
 * If nothing or something inconsistent is specified for any of the parameters, the default value will
 * be used.
 *
 * @author C.Sironi
 *
 */
public class PropnetStructureChecker {

	public static void main(String[] args){

		if(args.length == 0){
			System.out.println("Specify the key of the game on which to perform the propnet structure check (i.e. ticTacToe) or use the string ALL to test on all games.");
		}else{
			String game = args[0];
			long maxPropnetConstructionTime;

			if(args.length > 1){
				try{
					maxPropnetConstructionTime = Long.parseLong(args[1]);
				}catch(NumberFormatException e){
					System.out.println("Inconsistent maximum construction time specification! Using default value.");
					maxPropnetConstructionTime = 360000L;
				}
			}else{
				maxPropnetConstructionTime = 360000L;
			}

			System.out.println("Checking propnet structure on game(s): " + game);
			System.out.println("Maximum propnet construction time: " + maxPropnetConstructionTime);
			System.out.println();


			/*********************************************************************************/


			GamerLogger.setSpilloverLogfile(game + "PropnetStructureCheckerTable.csv");
		    GamerLogger.log(FORMAT.CSV_FORMAT, game + "PropnetStructureCheckerTable.csv", "Game key;Construction Time (ms);Check Duration (ms);Pass;");

		    if(game.equals("ALL")){
		    	GameRepository theRepository = GameRepository.getDefaultRepository();
		        for(String gameKey : theRepository.getGameKeys()) {
		            if(gameKey.contains("laikLee")) continue;
		            GamerLogger.log(FORMAT.CSV_FORMAT, game + "PropnetStructureCheckerTable.csv", checkPropnetStructure(gameKey, maxPropnetConstructionTime));
		            System.out.println();
		        }

			}else{
				GameRepository theRepository = GameRepository.getDefaultRepository();
				Game theGame = theRepository.getGame(game);
				if(theGame == null){
					System.out.println("Impossible to find the game with key " + game + ". Skipping test.");
				}else{
					GamerLogger.log(FORMAT.CSV_FORMAT, game + "PropnetStructureCheckerTable.csv", checkPropnetStructure(game, maxPropnetConstructionTime));
				}
			}
		}
	}

	public static String checkPropnetStructure(String gameKey, long maxPropnetCreationTime){

		GameRepository theRepository = GameRepository.getDefaultRepository();

		List<Gdl> description = theRepository.getGame(gameKey).getRules();

		System.out.println("Checking game " + gameKey + ".");

		ForwardInterruptingPropNet propNet = null;

		FwdInterrPropNetCreator creator = new FwdInterrPropNetCreator(description);

		Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

        GamerLogger.startFileLogging(fakeMatch, "PropnetStructureChecker");

    	// Try to create the propnet, if it takes too long stop the creation.
    	creator.start();
    	try{
    		creator.join(maxPropnetCreationTime);
    		// After 'maxPropnetCreationTime' milliseconds, if the creator thread is still alive it means
        	// that it is still busy creating the propnet and thus must be interrupted.
        	if(creator.isAlive()){
        		creator.interrupt();
        		// Wait for the creator to actually stop running (needed only to have no problems with logging:
    			// if both the state machine thread and the propnet creator thread try to write on the same log
    			// file at the same time there might be an exception. Moreover, the time order of the logs might
    			// not be respected in the file).
    			creator.join();
        	}
        	propNet = creator.getPropNet();
    	}catch (InterruptedException e) {
    		GamerLogger.logError("StateMachine", "[PropnetStructureChecker] Propnet creation interrupted by external action! Terminating initialization!");
    		GamerLogger.logStackTrace("StateMachine", e);
    		Thread.currentThread().interrupt();
    		GamerLogger.stopFileLogging();
    		System.out.println("Impossible to check game " + gameKey + ". Procedure interrupted.");
			return gameKey + ";-1;-1;FALSE;";
		}

    	if(propNet == null){
    		GamerLogger.stopFileLogging();
    		System.out.println("Impossible to check game " + gameKey + ". Propnet didn't build in time.");
    		return gameKey + ";" + creator.getConstructionTime() + ";-1;FALSE;";
    	}else{

    		System.out.println("Checking...");

    		System.out.println("Propnet has: " + propNet.getSize() + " COMPONENTS, " + propNet.getNumPropositions() + " PROPOSITIONS, " + propNet.getNumConstants() + " CONSTANTS, " + propNet.getNumLinks() + " LINKS.");
    		System.out.println("Propnet has: " + propNet.getNumAnds() + " ANDS, " + propNet.getNumOrs() + " ORS, " + propNet.getNumNots() + " NOTS.");
    		System.out.println("Propnet has: " + propNet.getNumBases() + " BASES, " + propNet.getNumTransitions() + " TRANSITIONS.");
    		System.out.println("Propnet has: " + propNet.getNumInputs() + " INPUTS, " + propNet.getNumLegals() + " LEGALS.");
    		System.out.println("Propnet has: " + propNet.getNumGoals() + " GOALS.");



    		long checkDuration = System.currentTimeMillis();

    		boolean pass = ForwardInterruptingPropNetFactory.checkPropnetStructure(propNet);

    		checkDuration = System.currentTimeMillis() - checkDuration;

    		GamerLogger.stopFileLogging();

    		System.out.println("Done checking game " + gameKey + ".");

    		return gameKey + ";" + creator.getConstructionTime() + ";" + checkDuration + ";" + pass + ";";
    	}
	}

}

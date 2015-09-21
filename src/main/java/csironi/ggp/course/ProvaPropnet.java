/**
 *
 */
package csironi.ggp.course;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingPropNet;
import org.ggp.base.util.propnet.factory.ForwardInterruptingPropNetFactory;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.CheckFwdInterrPropNetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

/**
 * @author C.Sironi
 *
 */
public class ProvaPropnet {

	public static void main(String []args){

		//provaGame("amazonsTorus", 300000);

		provaGame("snake_2009_big", 300000);

		//printKeys();


		//String description = "( ( role player1 ) ( role player2 ) ( <= ( base ( guessed ?player ?number ) ) ( role ?player ) ( guessableNumber ?number ) ) ( <= ( input ?player ( guess ?number ) ) ( role ?player ) ( guessableNumber ?number ) ) ( <= ( legal ?player ( guess ?number ) ) ( role ?player ) ( guessableNumber ?number ) ) ( <= ( next ( guessed ?player ?number ) ) ( does ?player ( guess ?number ) ) ) ( <= ( total ?number ) ( true ( guessed player1 ?n1 ) ) ( true ( guessed player2 ?n2 ) ) ( sum ?n1 ?n2 ?number ) ) ( <= ( twoThirdsAverage ?out ) ( total ?total ) ( times3 ?out ?total ) ) ( <= ( twoThirdsAverage ?out ) ( total ?total ) ( succ ?total ?tp1 ) ( times3 ?out ?tp1 ) ) ( <= ( twoThirdsAverage ?out ) ( total ?total ) ( succ ?tm1 ?total ) ( times3 ?out ?tm1 ) ) ( <= ( closeness ?player ?score ) ( true ( guessed ?player ?guess ) ) ( twoThirdsAverage ?result ) ( absDiff ?result ?guess ?score ) ) ( <= ( notClosest ?player ) ( closeness ?player ?score ) ( closeness ?otherPlayer ?lowerScore ) ( distinct ?player ?otherPlayer ) ( lt ?lowerScore ?score ) ) ( <= ( closest ?player ) ( role ?player ) ( not ( notClosest ?player ) ) ) ( <= ( goal player1 100 ) ( closest player1 ) ( notClosest player2 ) ) ( <= ( goal player2 100 ) ( notClosest player1 ) ( closest player2 ) ) ( <= ( goal ?player 50 ) ( role ?player ) ( closest player1 ) ( closest player2 ) ) ( <= ( goal ?player 0 ) ( notClosest ?player ) ) ( <= terminal ( true ( guessed ?player ?number ) ) ) ( <= ( absDiff ?x ?x 0 ) ( anyNumber ?x ) ) ( <= ( absDiff ?x ?y ?z ) ( lt ?y ?x ) ( succ ?xm1 ?x ) ( absDiff ?xm1 ?y ?zm1 ) ( succ ?zm1 ?z ) ) ( <= ( absDiff ?x ?y ?z ) ( lt ?x ?y ) ( succ ?ym1 ?y ) ( absDiff ?x ?ym1 ?zm1 ) ( succ ?zm1 ?z ) ) ( <= ( sum ?x 0 ?x ) ( anyNumber ?x ) ) ( <= ( sum ?x ?y ?z ) ( succ ?ym1 ?y ) ( sum ?x ?ym1 ?zm1 ) ( succ ?zm1 ?z ) ) ( times3 0 0 ) ( <= ( times3 ?x ?y ) ( succ ?ym1 ?y ) ( succ ?ym2 ?ym1 ) ( succ ?ym3 ?ym2 ) ( times3 ?xm1 ?ym3 ) ( succ ?xm1 ?x ) ) ( <= ( lt ?x ?y ) ( lte ?x ?y ) ( distinct ?x ?y ) ) ( <= ( lte ?x ?x ) ( anyNumber ?x ) ) ( <= ( lte ?x ?y ) ( succ ?ym1 ?y ) ( lte ?x ?ym1 ) ) ( anyNumber 0 ) (<= ( anyNumber ?n ) ( succ ?m ?n ) ) ( <= ( guessableNumber ?number ) ( lte ?number 3 ) ) ( succ 0 1 ) ( succ 1 2 ) ( succ 2 3 ) ( succ 3 4 ) ( succ 4 5 ) ( succ 5 6 ) ( succ 6 7 ) )";

		//printPropnet(description, "gt_two_thirds_2p");

	}

	/**
	 * This method, given the GDL description, builds the corresponding propnet,
	 * prints its .dot representation and saves it in a file, both before and after
	 * imposing the consistency on the values of the components of the propnet.
	 *
	 * NOTE that this method doesn't check the propnet building time. It will
	 * wait indefinitely for the propnet to build.
	 *
	 * @param description the GDL description of the game.
	 * @param gameName the key of the game, used to reate the .dot files names.
	 */
	public static void printPropnet(String description, String gameName){
		Game game = Game.createEphemeralGame(description);

		List<Gdl> list = game.getRules();

		ForwardInterruptingPropNet propNet = null;

		try {
			propNet = ForwardInterruptingPropNetFactory.create(list, true);
		} catch (InterruptedException e) {
			System.out.println("Something went wrong with the creation of the propnet!");
			e.printStackTrace();
		}

		String string = null;

		if(propNet != null){

			string = propNet.toString();
		}

		System.out.println(string);

		// SAVE TO FILE
		try{
    		// Write propnet to file
            BufferedWriter out = new BufferedWriter(new FileWriter(gameName + "Propnet.dot", false));
            out.write(string);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

		if(propNet != null){
			propNet.imposeConsistency();
			string = propNet.toString();
		}

		// SAVE TO FILE
		try{
    		// Write propnet to file
            BufferedWriter out = new BufferedWriter(new FileWriter(gameName + "InitPropnet.dot", false));
            out.write(string);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

	}

	/**
	 * This method experiments with the propnet of the game Buttons and Lights.
	 */
	public static void prova(){
		/*

		String incompleteDescription = " ( ( role xplayer ) ( role oplayer ) ( init ( cell 1 1 b ) ) ( init ( cell 1 2 b ) ) ( init ( cell 1 3 b ) ) ( init ( cell 2 1 b ) ) ( init ( cell 2 2 b ) ) ( init ( cell 2 3 b ) ) ( init ( cell 3 1 b ) ) ( init ( cell 3 2 b ) ) ( init ( cell 3 3 b ) ) ( init ( control xplayer ) ) ( <= ( next ( cell ?m ?n x ) ) ( does xplayer ( mark ?m ?n ) ) ( true ( cell ?m ?n b ) ) ) ( <= ( next ( cell ?m ?n o ) ) ( does oplayer ( mark ?m ?n ) ) ( true (cell ?m ?n b ) ) ) ( <= ( next ( cell ?m ?n ?w ) ) ( true ( cell ?m ?n ?w ) ) ( distinct ?w b ) ) ( <= ( next ( cell ?m ?n b ) ) ( does ?w ( mark ?j ?k ) ) ( true ( cell ?m ?n b ) ) ( or ( distinct ?m ?j ) ( distinct ?n ?k ) ) ) ( <= ( next ( control xplayer ) ) ( true ( control oplayer ) ) ) ( <= ( next ( control oplayer ) ) ( true ( control xplayer ) ) ) ( <= ( row ?m ?x ) ( true ( cell ?m 1 ?x ) ) ( true ( cell ?m 2 ?x ) ) ( true ( cell ?m 3 ?x ) ) ) ( <= ( column ?n ?x ) ( true ( cell 1 ?n ?x ) ) ( true ( cell 2 ?n ?x ) ) ( true ( cell 3 ?n ?x ) ) ) ( <= ( diagonal ?x ) ( true ( cell 1 1 ?x ) ) ( true ( cell 2 2 ?x ) ) ( true ( cell 3 3 ?x ) ) ) ( <= ( diagonal ?x ) ( true ( cell 1 3 ?x ) ) ( true ( cell 2 2 ?x ) ) ( true ( cell 3 1 ?x ) ) ) ( <= ( line ?x ) ( row ?m ?x ) ) ( <= ( line ?x ) ( column ?m ?x ) ) ( <= ( line ?x ) ( diagonal ?x ) ) ( <= open ( true ( cell ?m ?n b ) ) ) ( <= ( legal ?w ( mark ?x ?y ) ) ( true ( cell ?x ?y b ) ) ( true ( control ?w ) ) ) ( <= ( legal xplayer noop ) ( true ( control oplayer ) ) ) ( <= ( legal oplayer noop ) ( true ( control xplayer ) ) ) ( <= ( goal xplayer 100 ) ( line x ) ) ( <= ( goal xplayer 50 ) ( not ( line x ) ) ( not ( line o ) ) ( not open ) ) ( <= ( goal xplayer 0 ) ( line o ) ) ( <= ( goal oplayer 100 ) ( line o ) ) ( <= ( goal oplayer 50 ) ( not ( line x ) ) ( not ( line o ) ) ( not open ) ) ( <= ( goal oplayer 0 ) ( line x ) ) ( <= terminal ( line x ) ) ( <= terminal ( line o ) ) ( <= terminal ( not open ) ) ) ";

		String completeDescription = " ( ( role xplayer ) ( role oplayer ) ( index 1 ) ( index 2 ) ( index 3 ) ( <= ( base ( cell ?x ?y b ) ) ( index ?x ) ( index ?y ) ) ( <= ( base ( cell ?x ?y x ) ) ( index ?x ) ( index ?y ) ) ( <= ( base ( cell ?x ?y o ) ) ( index ?x ) ( index ?y ) ) ( <= ( base ( control ?p ) )	( role ?p ) ) ( <= ( input ?p ( mark ?x ?y ) ) ( index ?x ) ( index ?y ) ( role ?p ) ) ( <= ( input ?p noop ) ( role ?p ) ) ( init ( cell 1 1 b ) ) ( init ( cell 1 2 b ) ) ( init ( cell 1 3 b ) ) ( init ( cell 2 1 b ) ) ( init ( cell 2 2 b ) ) ( init ( cell 2 3 b ) ) ( init ( cell 3 1 b ) ) ( init ( cell 3 2 b ) ) ( init ( cell 3 3 b ) ) ( init ( control xplayer ) ) ( <= ( next ( cell ?m ?n x ) ) ( does xplayer ( mark ?m ?n ) ) ( true ( cell ?m ?n b ) ) ) ( <= ( next ( cell ?m ?n o ) ) ( does oplayer ( mark ?m ?n ) ) ( true ( cell ?m ?n b ) ) ) ( <= ( next ( cell ?m ?n ?w ) ) ( true ( cell ?m ?n ?w ) ) ( distinct ?w b ) ) ( <= ( next ( cell ?m ?n b ) ) ( does ?w ( mark ?j ?k ) ) ( true ( cell ?m ?n b ) ) ( or ( distinct ?m ?j ) ( distinct ?n ?k ) ) ) ( <= ( next ( control xplayer ) ) ( true ( control oplayer ) ) ) ( <= ( next ( control oplayer ) ) ( true ( control xplayer ) ) ) ( <= ( row ?m ?x ) ( true ( cell ?m 1 ?x ) ) ( true ( cell ?m 2 ?x ) ) ( true ( cell ?m 3 ?x ) ) ) ( <= ( column ?n ?x ) ( true ( cell 1 ?n ?x ) ) ( true ( cell 2 ?n ?x ) ) ( true ( cell 3 ?n ?x ) ) ) ( <= ( diagonal ?x ) ( true ( cell 1 1 ?x ) ) ( true ( cell 2 2 ?x ) ) ( true ( cell 3 3 ?x ) ) ) ( <= ( diagonal ?x ) ( true ( cell 1 3 ?x ) ) ( true ( cell 2 2 ?x ) ) ( true ( cell 3 1 ?x ) ) ) ( <= ( line ?x ) ( row ?m ?x ) ) ( <= ( line ?x ) ( column ?m ?x ) ) ( <= ( line ?x ) ( diagonal ?x ) ) ( <= open ( true ( cell ?m ?n b ) ) ) ( <= ( legal ?w ( mark ?x ?y ) ) ( true ( cell ?x ?y b ) ) ( true ( control ?w ) ) ) ( <= ( legal xplayer noop ) ( true ( control oplayer ) ) ) ( <= ( legal oplayer noop ) ( true ( control xplayer ) ) ) ( <= ( goal xplayer 100 ) ( line x ) ) ( <= ( goal xplayer 50 ) ( not ( line x ) ) ( not ( line o ) ) ( not open ) ) ( <= ( goal xplayer 0 ) ( line o ) ) ( <= ( goal oplayer 100 ) ( line o ) ) ( <= ( goal oplayer 50 ) ( not ( line x ) ) ( not ( line o ) ) ( not open ) ) ( <= ( goal oplayer 0 ) ( line x ) ) ( <= terminal ( line x ) ) ( <= terminal ( line o ) ) ( <= terminal ( not open ) ) ) ";

		System.out.println(incompleteDescription);
		System.out.println(completeDescription);

		Game incompleteGame = Game.createEphemeralGame(incompleteDescription);
		Game completeGame = Game.createEphemeralGame(completeDescription);

		List<Gdl> incompleteList = incompleteGame.getRules();
		List<Gdl> completeList = completeGame.getRules();

		System.out.println(incompleteList.toString());
		System.out.println(completeList.toString());

		PropNet incompletePropNet = null;
		PropNet completePropNet = null;

		try {
			incompletePropNet = OptimizingPropNetFactory.create(incompleteList, true);
			completePropNet = OptimizingPropNetFactory.create(completeList, true);
		} catch (InterruptedException e) {
			System.out.println("Uh oh");
		}

		Proposition p = completePropNet.getInitProposition();

		System.out.println(p.getName());

		String incompleteString = null;
		String completeString = null;

		if(incompletePropNet != null || completePropNet != null){
			incompleteString = incompletePropNet.toString();
			completeString = completePropNet.toString();
		}

		System.out.println(incompleteString);
		System.out.println(completeString);

		if(incompleteString.equals(completeString)){
			System.out.println("WOW");
		}

		*/


		//BUTTONS AND LIGHTS
		/*String BeLdescription = "( ( role robot ) ( base p ) ( base q ) ( base r ) ( base 1 ) ( base 2 ) ( base 3 ) ( base 4 ) ( base 5 ) ( base 6 ) ( base 7 ) ( input robot a ) ( input robot b ) ( input robot c ) ( init 1 ) ( legal robot a ) ( legal robot b ) ( legal robot c ) ( <= ( next p ) ( does robot a ) ( not ( true p ) ) ) ( <= ( next p ) ( does robot b ) ( true q ) ) ( <= ( next p ) ( does robot c ) ( true p ) ) ( <= ( next q ) ( does robot a ) ( true q ) ) ( <= ( next q ) ( does robot b ) ( true p ) ) ( <= ( next q ) ( does robot c ) ( true r ) ) ( <= ( next r ) ( does robot a ) ( true r ) ) ( <= ( next r ) ( does robot b ) ( true r ) ) ( <= ( next r ) ( does robot c ) ( true q ) ) ( <= ( next ?y ) ( true ?x ) ( successor ?x ?y ) ) ( <= ( goal robot 100 ) ( true p ) ( true q ) ( true r ) ) ( <= ( goal robot 0 ) ( not ( true p ) ) ) ( <= ( goal robot 0 ) ( not ( true q ) ) ) ( <= ( goal robot 0 ) ( not ( true r ) ) ) ( <= terminal ( true p ) ( true q ) ( true r ) ) ( <= terminal ( true 7 ) ) ( successor 1 2 ) ( successor 2 3 ) ( successor 3 4 ) ( successor 4 5 ) ( successor 5 6 ) ( successor 6 7 ) )";

		Game BeLGame = Game.createEphemeralGame(BeLdescription);

		List<Gdl> BeLList = BeLGame.getRules();

		PropNet BeLPropNet = null;

		try {
			BeLPropNet = OptimizingPropNetFactory.create(BeLList, true);
		} catch (InterruptedException e) {
			System.out.println("Uh oh");
		}

		String BeLString = null;

		if(BeLPropNet != null){

			BeLString = BeLPropNet.toString();
		}

		System.out.println(BeLString);

		Set<Proposition> props = BeLPropNet.getPropositions();
		Set<Component> comps = BeLPropNet.getComponents();


		for(Proposition p : props){
			System.out.println("[ " + p.getName() + " , " + p.getValue() + " ]");
		}

		for(Component c : comps){
			if(c instanceof Proposition){
				System.out.println("[ " + ((Proposition) c).getName() + " , " + ((Proposition) c).getValue() + " ]");
			}else{
				System.out.println("[ " + c.getClass().getName() + " , " + c.getValue() + " ]");
			}
		}*/

		//BUTTONS AND LIGHTS
		String BeLdescription = "( ( role robot ) ( base p ) ( base q ) ( base r ) ( base 1 ) ( base 2 ) ( base 3 ) ( base 4 ) ( base 5 ) ( base 6 ) ( base 7 ) ( input robot a ) ( input robot b ) ( input robot c ) ( init 1 ) ( legal robot a ) ( legal robot b ) ( legal robot c ) ( <= ( next p ) ( does robot a ) ( not ( true p ) ) ) ( <= ( next p ) ( does robot b ) ( true q ) ) ( <= ( next p ) ( does robot c ) ( true p ) ) ( <= ( next q ) ( does robot a ) ( true q ) ) ( <= ( next q ) ( does robot b ) ( true p ) ) ( <= ( next q ) ( does robot c ) ( true r ) ) ( <= ( next r ) ( does robot a ) ( true r ) ) ( <= ( next r ) ( does robot b ) ( true r ) ) ( <= ( next r ) ( does robot c ) ( true q ) ) ( <= ( next ?y ) ( true ?x ) ( successor ?x ?y ) ) ( <= ( goal robot 100 ) ( true p ) ( true q ) ( true r ) ) ( <= ( goal robot 0 ) ( not ( true p ) ) ) ( <= ( goal robot 0 ) ( not ( true q ) ) ) ( <= ( goal robot 0 ) ( not ( true r ) ) ) ( <= terminal ( true p ) ( true q ) ( true r ) ) ( <= terminal ( true 7 ) ) ( successor 1 2 ) ( successor 2 3 ) ( successor 3 4 ) ( successor 4 5 ) ( successor 5 6 ) ( successor 6 7 ) )";

		Game BeLGame = Game.createEphemeralGame(BeLdescription);

		List<Gdl> BeLList = BeLGame.getRules();

		ForwardInterruptingPropNet BeLPropNet = null;

		try {
			BeLPropNet = ForwardInterruptingPropNetFactory.create(BeLList, true);
		} catch (InterruptedException e) {
			System.out.println("Something went wrong with the creation of the propnet!");
			e.printStackTrace();
		}

		String BeLString = null;

		if(BeLPropNet != null){

			BeLString = BeLPropNet.toString();
		}

		System.out.println(BeLString);

		BeLPropNet.imposeConsistency();

		BeLString = BeLPropNet.toString();

		System.out.println(BeLString);

		BeLPropNet.getInitProposition().setAndPropagateValue(true);

		BeLString = BeLPropNet.toString();

		System.out.println(BeLString);

		BeLPropNet.resetValues();
		BeLPropNet.getInitProposition().setValue(true);
		BeLPropNet.imposeConsistency();

		BeLString = BeLPropNet.toString();

		System.out.println(BeLString);

		//Set<Proposition> props = BeLPropNet.getPropositions();
		//Set<ForwardInterruptingComponent> comps = BeLPropNet.getComponents();


		/*for(Proposition p : props){
			System.out.println("[ " + p.getName() + " , " + p.getValue() + " ]");
		}*/

		/*for(ForwardInterruptingComponent c : comps){
			if(c instanceof ForwardInterruptingProposition){
				System.out.println("[ " + ((ForwardInterruptingProposition) c).getName() + " , " + ((ForwardInterruptingProposition) c).getValue() + " ]");
			}else{
				System.out.println("[ " + c.getClass().getName() + " , " + c.getValue() + " ]");
			}
		}*/



	}

	/**
	 * This method performs one complete simulation from root to terminal state of the given game,
	 * using both the ProverStateMachine and the CheckFwdInterrPropnetStateMachine. For each step
	 * it prints the content of the states computed by both the state machines and also saves the
	 * propnet state in a .dot format, so that it will be possible to check how the values of the
	 * propositions in the propnet changed at every step.
	 *
	 * @param game the GDL game description.
	 * @param maxPropnetCreationTime the maximum time that the propnet state machine has available
	 * to build the propnet.
	 */
	public static void provaGame(String game, long maxPropnetCreationTime){

        ProverStateMachine theReference;
        CheckFwdInterrPropNetStateMachine thePropNetMachine;

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            if(!gameKey.equals(game)) continue;

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine();
            thePropNetMachine = new CheckFwdInterrPropNetStateMachine(maxPropnetCreationTime);

            theReference.initialize(description);
            try {
				thePropNetMachine.initialize(description);
			} catch (StateMachineInitializationException e) {
				System.out.println("Something went wrong with the creation of the propnet!");
				e.printStackTrace();
			}

            ForwardInterruptingPropNet propnet = thePropNetMachine.getPropNet();

            // It's possible to continue the execution only if the state machine managed to build the propnet
            if(propnet != null){

	            int step = 0;

	            //PRINTING THE PROPNET

	            String propnetRepresentation = null;

	    		propnetRepresentation = propnet.toString();

	    		//System.out.println(propnetRepresentation);
	    		try{
		    		// Write propnet to file
		            BufferedWriter out = new BufferedWriter(new FileWriter("Step"+step+"Propnet"+gameKey+".dot", false));
		            out.write(propnetRepresentation);
		            out.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }

	    		//MachineState init = thePropNetMachine.getInitialState();
	    		//if(init == null){
	    		//	System.out.println("Cavolo!");
	    		//}else{
	    		//	System.out.println("INIT STATE:");
	    		//	System.out.println(init);
	    		//}

	            //END OF PROPNET PRINTING

	    		MachineState proverState = null;
	    		MachineState propnetState = null;

	    		try {
	                proverState = theReference.getInitialState();
	            } catch(Exception e) {
	                GamerLogger.log("StateMachine", "Prover machine failed to generate an initial state!");
	                e.printStackTrace();
	            }
	    		try {
	                propnetState = thePropNetMachine.getInitialState();
	            } catch(Exception e) {
	                GamerLogger.log("StateMachine", "Propnet machine failed to generate an initial state!");
	                e.printStackTrace();
	            }

	    		System.out.println("PROVER INITIAL STATE");
	    		System.out.println(proverState);
	    		System.out.println("PROPNET INITIAL STATE");
	    		System.out.println(propnetState);

	    		while(!theReference.isTerminal(proverState)){

	    			step++;

	    			List<Move> jointMove = null;
	    			try {
	    				jointMove = theReference.getRandomJointMove(proverState);
	                    proverState = theReference.getNextState(proverState, jointMove);
	                } catch(Exception e) {
	                    GamerLogger.log("StateMachine", "Prover machine failed to generate the next state!");
	                    e.printStackTrace();
	                }
	        		try {
	                    propnetState = thePropNetMachine.getNextState(propnetState, jointMove);
	                } catch(Exception e) {
	                    GamerLogger.log("StateMachine", "Propnet machine failed to generate the next state!");
	                    e.printStackTrace();
	                }
	        		System.out.println("PROVER STATE");
	        		System.out.println(proverState);
	        		System.out.println("PROPNET STATE");
	        		System.out.println(propnetState);

	        		//PRINTING THE PROPNET
	                propnetRepresentation = null;
	        		propnetRepresentation = propnet.toString();
	        		//System.out.println(propnetRepresentation);
	        		try{
	    	    		// Write propnet to file
	    	            BufferedWriter out = new BufferedWriter(new FileWriter("Step"+step+"Propnet"+gameKey+".dot", false));
	    	            out.write(propnetRepresentation);
	    	            out.close();
	    	        } catch (IOException e) {
	    	            e.printStackTrace();
	    	        }

	        		//MachineState init = thePropNetMachine.getInitialState();
	        		//if(init == null){
	        		//	System.out.println("Cavolo!");
	        		//}else{
	        		//	System.out.println("INIT STATE:");
	        		//	System.out.println(init);
	        		//}

	                //END OF PROPNET PRINTING

	    		}
            }
        }
	}


	/**
	 * This method prints all the game keys in the GGP Base repository on a file,
	 * pointing out among them the games that will be considered in the tests (the
	 * ones that do not contain the word "likeLee").
	 */
	public static void printKeys(){
		GamerLogger.setSpilloverLogfile("GameKeys");

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
        	if(gameKey.contains("laikLee")){
        		GamerLogger.log("GameKeys", "NO: " + gameKey);
        	}else{
        		GamerLogger.log("GameKeys", "YES: " + gameKey);
        	}
        }
	}
}

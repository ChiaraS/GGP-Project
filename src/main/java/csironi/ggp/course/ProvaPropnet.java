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
import org.ggp.base.util.statemachine.implementation.propnet.ForwardInterruptingPropNetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

/**
 * @author C.Sironi
 *
 */
public class ProvaPropnet {

	public static void main(String []args){

		provaGame("amazonsTorus");

	}

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
			System.out.println("Uh oh");
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

	public static void provaGame(String game){

        ProverStateMachine theReference;
        ForwardInterruptingPropNetStateMachine thePropNetMachine;

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            if(!gameKey.equals(game)) continue;

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine();
            thePropNetMachine = new ForwardInterruptingPropNetStateMachine();

            theReference.initialize(description);
            thePropNetMachine.initialize(description);

            int step = 0;

            //PRINTING THE PROPNET
            ForwardInterruptingPropNet propnet = thePropNetMachine.getPropNet();
            String propnetRepresentation = null;
    		if(propnet != null){
    			propnetRepresentation = propnet.toString();
    		}else{
    			//System.out.println("UFFAAAAAAAAAA");
    		}
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
            }
    		try {
                propnetState = thePropNetMachine.getInitialState();
            } catch(Exception e) {
                GamerLogger.log("StateMachine", "Propnet machine failed to generate an initial state!");
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
                    GamerLogger.log("StateMachine", "Prover machine failed to generate an initial state!");
                }
        		try {
                    propnetState = thePropNetMachine.getNextState(propnetState, jointMove);
                } catch(Exception e) {
                    GamerLogger.log("StateMachine", "Propnet machine failed to generate an initial state!");
                }
        		System.out.println("PROVER STATE");
        		System.out.println(proverState);
        		System.out.println("PROPNET STATE");
        		System.out.println(propnetState);

        		//PRINTING THE PROPNET
                propnetRepresentation = null;
        		if(propnet != null){
        			propnetRepresentation = propnet.toString();
        		}else{
        			//System.out.println("UFFAAAAAAAAAA");
        		}
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

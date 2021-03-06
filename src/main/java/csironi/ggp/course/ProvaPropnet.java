/**
 *
 */
package csironi.ggp.course;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.player.gamer.statemachine.MCTS.MctsGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateConstant;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingPropNet;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.components.ForwardInterruptingAnd;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.components.ForwardInterruptingConstant;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.components.ForwardInterruptingNot;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.components.ForwardInterruptingOr;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.components.ForwardInterruptingProposition;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.components.ForwardInterruptingTransition;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicPropNet;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.PropNetManagerRunner;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.OptimizationCaller;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.OptimizeAwayConstantValueComponents;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.OptimizeAwayConstants;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.RemoveAnonPropositions;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.RemoveOutputlessComponents;
import org.ggp.base.util.propnet.factory.ForwardInterruptingPropNetFactory;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.CheckFwdInterrPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.propnet.FwdInterrPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.safe.InitializationSafeStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

/**
 * @author C.Sironi
 *
 */
public class ProvaPropnet {

	public static void main(String []args) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineInitializationException{

		//createDuct();

		//printOptPropnetFromDescription("( ( role player ) ( light p ) ( light q ) ( <= ( legal player ( turnOn ?x ) ) ( not ( true ( on ?x ) ) ) ( light ?x ) ) ( <= ( next ( on ?x ) ) ( does player ( turnOn ?x ) ) ) ( <= ( next ( on ?x ) ) ( true ( on ?x ) ) ) ( <= terminal ( true ( on p ) ) ( true ( on q ) ) ) ( <= ( goal player 100 ) ( true ( on p ) ) ( true ( on q) ) ) ) ", "none", 10000L);

		//printOptPropnetFromGameCache("ticTacToe", "none", 10000L);

		//altraProva();

		//ennesimaProva();

		//printDynamicPropnet("onestep", 420000L);

		//checkPropnetStructure("ticTacToe");

		//provaGame2("ad_game_2x2", 300000);

		//provaGame("snake_2009_big", 300000);

		//printKeys();


		//String description = "( ( role player1 ) ( role player2 ) ( <= ( base ( guessed ?player ?number ) ) ( role ?player ) ( guessableNumber ?number ) ) ( <= ( input ?player ( guess ?number ) ) ( role ?player ) ( guessableNumber ?number ) ) ( <= ( legal ?player ( guess ?number ) ) ( role ?player ) ( guessableNumber ?number ) ) ( <= ( next ( guessed ?player ?number ) ) ( does ?player ( guess ?number ) ) ) ( <= ( total ?number ) ( true ( guessed player1 ?n1 ) ) ( true ( guessed player2 ?n2 ) ) ( sum ?n1 ?n2 ?number ) ) ( <= ( twoThirdsAverage ?out ) ( total ?total ) ( times3 ?out ?total ) ) ( <= ( twoThirdsAverage ?out ) ( total ?total ) ( succ ?total ?tp1 ) ( times3 ?out ?tp1 ) ) ( <= ( twoThirdsAverage ?out ) ( total ?total ) ( succ ?tm1 ?total ) ( times3 ?out ?tm1 ) ) ( <= ( closeness ?player ?score ) ( true ( guessed ?player ?guess ) ) ( twoThirdsAverage ?result ) ( absDiff ?result ?guess ?score ) ) ( <= ( notClosest ?player ) ( closeness ?player ?score ) ( closeness ?otherPlayer ?lowerScore ) ( distinct ?player ?otherPlayer ) ( lt ?lowerScore ?score ) ) ( <= ( closest ?player ) ( role ?player ) ( not ( notClosest ?player ) ) ) ( <= ( goal player1 100 ) ( closest player1 ) ( notClosest player2 ) ) ( <= ( goal player2 100 ) ( notClosest player1 ) ( closest player2 ) ) ( <= ( goal ?player 50 ) ( role ?player ) ( closest player1 ) ( closest player2 ) ) ( <= ( goal ?player 0 ) ( notClosest ?player ) ) ( <= terminal ( true ( guessed ?player ?number ) ) ) ( <= ( absDiff ?x ?x 0 ) ( anyNumber ?x ) ) ( <= ( absDiff ?x ?y ?z ) ( lt ?y ?x ) ( succ ?xm1 ?x ) ( absDiff ?xm1 ?y ?zm1 ) ( succ ?zm1 ?z ) ) ( <= ( absDiff ?x ?y ?z ) ( lt ?x ?y ) ( succ ?ym1 ?y ) ( absDiff ?x ?ym1 ?zm1 ) ( succ ?zm1 ?z ) ) ( <= ( sum ?x 0 ?x ) ( anyNumber ?x ) ) ( <= ( sum ?x ?y ?z ) ( succ ?ym1 ?y ) ( sum ?x ?ym1 ?zm1 ) ( succ ?zm1 ?z ) ) ( times3 0 0 ) ( <= ( times3 ?x ?y ) ( succ ?ym1 ?y ) ( succ ?ym2 ?ym1 ) ( succ ?ym3 ?ym2 ) ( times3 ?xm1 ?ym3 ) ( succ ?xm1 ?x ) ) ( <= ( lt ?x ?y ) ( lte ?x ?y ) ( distinct ?x ?y ) ) ( <= ( lte ?x ?x ) ( anyNumber ?x ) ) ( <= ( lte ?x ?y ) ( succ ?ym1 ?y ) ( lte ?x ?ym1 ) ) ( anyNumber 0 ) (<= ( anyNumber ?n ) ( succ ?m ?n ) ) ( <= ( guessableNumber ?number ) ( lte ?number 3 ) ) ( succ 0 1 ) ( succ 1 2 ) ( succ 2 3 ) ( succ 3 4 ) ( succ 4 5 ) ( succ 5 6 ) ( succ 6 7 ) )";
/*
		GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            if(gameKey.equals("amazonsTorus") || gameKey.equals("god") || gameKey.equals("amazonsSuicide") ||
            		gameKey.equals("alexChess") || gameKey.equals("slaughter") ||
            		gameKey.equals("factoringImpossibleTurtleBrain") || gameKey.equals("gt_two_thirds_6p") ||
            		gameKey.equals("cylinder-checkers") || gameKey.equals("mummymaze1p") ||
            		gameKey.equals("merrills") || gameKey.equals("chess") || gameKey.equals("sudoku")) continue;

            System.out.println();
            System.out.println("GameKey: " + gameKey);

            printPropnetImprovements(gameKey);
        }
*/
		printPropnet("buttons");

		//provaOpenbitset();

		//prova();
		//tryThis();

		//tryThisToo(new Object());

		//Object o = new Integer(4);

		//tryThisToo(o);

		//tryThisToo((Integer)o);

		//provaInsiemi();
		//provaUgualeUguale();
		//provaOverflow();
		//provaGameExtendedPropnet("coins_atomic");

		//printPropnetImprovements("gt_two_thirds_2p");

	}

	public static void createDuct(){

		MctsGamer g = new MctsGamer();

		System.out.println(g.getName());


	}

	private static double computeDUCTvalue(double score, double moveVisits, double nodeVisits){

		double c = 0.7;
		// NOTE: this should never happen if we use this class together with the InternalPropnetMCTSManager
		// because the selection phase in a node starts only after all moves have been expanded and visited
		// at least once. However a check is performed to keep the computation consistent even when a move
		// has never been visited (i.e. the "infinite" value (Double.MAX_VALUE) is returned).
		if(moveVisits == 0){
			return Double.MAX_VALUE;
		}

		double avgScore = (score / moveVisits) / 100.0;
		double exploration = c * (Math.sqrt(Math.log(nodeVisits)/moveVisits));
		return  avgScore + exploration;

	}

	public static void ennesimaProva(){

		int a = 59078294;

		System.out.println(a);

		System.out.println(a * 1000);

		System.out.println(a * 1000.0);

		System.out.println((double) a * 1000);

		System.out.println((double) a * 1000.0);

		System.out.println(((double) (a * 1000)));

		System.out.println(((double) (a * 1000.0)));

		long aa = 59078294;

		System.out.println(aa);

		System.out.println(aa * 1000);

		System.out.println(aa * 1000.0);

		System.out.println((double) aa * 1000);

		System.out.println((double) aa * 1000.0);

		System.out.println(((double) (aa * 1000)));

		System.out.println(((double) (aa * 1000.0)));

	}

	public static void printDynamicPropnet(String gameKey, long givenInitTime) throws StateMachineInitializationException{

        System.out.println("Looking for game " + gameKey + "...");

        GameRepository theRepository = GameRepository.getDefaultRepository();

        Game theGame = theRepository.getGame(gameKey);

        if(theGame == null){
        	System.out.println("Couldn't find it. Impossible to build and print propnet.");
        	return;
        }

        Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey));

        GamerLogger.startFileLogging(fakeMatch, "DynamicPNPrinter");

        GamerLogger.log("DynamicPNPrinter", "Building and printing propnet for game " + gameKey);

        List<Gdl> description = theRepository.getGame(gameKey).getRules();

        // Create the executor service that will run the propnet manager that creates the propnet
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create the propnet creation manager
        SeparateInternalPropnetManager manager = new SeparateInternalPropnetManager(description, System.currentTimeMillis() + givenInitTime);

  	  	// Start the manager
  	  	executor.execute(manager);

  	  	// Shutdown executor to tell it not to accept any more task to execute.
		// Note that this doesn't interrupt previously started tasks.
		executor.shutdown();

		// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
		try{
			executor.awaitTermination(givenInitTime, TimeUnit.MILLISECONDS);
		}catch(InterruptedException e){ // The thread running the propnet creation and printing has been interrupted => stop
			executor.shutdownNow(); // Interrupt everything
			GamerLogger.logError("DynamicPNPrinter", "Propnet building and printing task for game " + gameKey + " interrupted.");
			GamerLogger.logStackTrace("DynamicPNPrinter", e);
			GamerLogger.stopFileLogging();
			Thread.currentThread().interrupt();
			return;
		}

		// Here the available time has elapsed, so we must interrupt the thread if it is still running.
		executor.shutdownNow();

		// Wait for the thread to actually terminate
		while(!executor.isTerminated()){

			// If the thread didn't terminate, wait for a minute and then check again
			try{
				executor.awaitTermination(1, TimeUnit.MINUTES);
			}catch(InterruptedException e) {
				// If this exception is thrown it means the thread that is executing the creatino and printing
				// of the propnet has been interrupted. If we do nothing this method could be stuck in the
				// while loop anyway until all tasks in the executor have terminated, thus we break out of the
				// loop and return.
				// What happens to the still running tasks in the executor? Who will make sure they terminate?
				GamerLogger.logError("DynamicPNPrinter", "Propnet building and printing task for game " + gameKey + " interrupted.");
				GamerLogger.logStackTrace("DynamicPNPrinter", e);
				GamerLogger.stopFileLogging();
				Thread.currentThread().interrupt();
				return;
			}
		}

		// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.

		DynamicPropNet dpropnet = manager.getDynamicPropnet();

		if(dpropnet != null){
			System.out.println(dpropnet.toString());
		}

		ImmutablePropNet ipropnet = manager.getImmutablePropnet();
		ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();

		// Create the state machine giving it the propnet and the propnet state.
		// NOTE that if any of the two is null, it means that the propnet creation/initialization went wrong
		// and this will be detected by the state machine during initialization.
		InternalPropnetStateMachine thePropnetMachine = new SeparateInternalPropnetStateMachine(new Random(), ipropnet, propnetState);

		thePropnetMachine.initialize(description, System.currentTimeMillis() + 60000L);


	}

	public static void altraProva(){

		long l = Long.MAX_VALUE/2;

		System.out.println(l);

		double d = (double)l;

		System.out.println(d);

		System.out.println();

		int i = Integer.MAX_VALUE;

		System.out.println(i);

		double d2 = (double)i;

		System.out.println(d2);

		System.out.println();

		System.out.println(Double.MAX_VALUE);
		System.out.println(Long.MAX_VALUE);
		System.out.println(Integer.MAX_VALUE);


		int score = 1942871190;
		int moveVisits = 23724598;
		int nodeVisits = 23724598;

		System.out.println("score = " + score);
		System.out.println("moveVisits = " + moveVisits);
		System.out.println("nodeVisits = " + nodeVisits);

		double dScore = (double) score;
		double dMoveVisits = (double) moveVisits;
		double dNodeVisits = (double) nodeVisits;

		System.out.println();

		System.out.println("dScore = " + dScore);
		System.out.println("dMoveVisits = " + dMoveVisits);
		System.out.println("dNodeVisits = " + dNodeVisits);

		double avgScore = (double) score / ((double)(moveVisits * 100.0));
		double dAvgScore = dScore / (dMoveVisits * 100.0);

		System.out.println();

		System.out.println("avgScore = " + avgScore);
		System.out.println("dAvgScore = " + dAvgScore);

		double c = 0.7;

		System.out.println();

		System.out.println("c = " + c);

		double logNodeVisits = Math.log(nodeVisits);
		double dLogNodeVisits = Math.log((double)nodeVisits);
		double ddLogNodeVisits = Math.log(dNodeVisits);

		System.out.println();
		System.out.println("logNodeVisits = " + logNodeVisits);
		System.out.println("dLogNodeVisits = " + dLogNodeVisits);
		System.out.println("ddLogNodeVisits = " + ddLogNodeVisits);

		double fraction = logNodeVisits / ((double)moveVisits);
		double dFraction = logNodeVisits / dMoveVisits;

		System.out.println();
		System.out.println("fraction = " + fraction);
		System.out.println("dFraction = " + dFraction);

		double sqrt = Math.sqrt(fraction);

		System.out.println();
		System.out.println("sqrt = " + sqrt);

		double exploration = c * sqrt;

		System.out.println();
		System.out.println("exploration = " + exploration);

		double uct = avgScore + exploration;

		System.out.println();
		System.out.println("uct = " + uct);

		double cAvgScore = ((double) score) / ((double)(moveVisits * 100.0));
		//double exploration = this.c * (Math.sqrt(Math.log(nodeVisits)/(double)moveVisits));
		//return  avgScore + exploration;



	}

	public static void altraProva2(){

		System.out.println(Math.log(0));

		System.out.println(Math.log(0)/40);

		System.out.println(Math.log(0)/0);

		System.out.println(Math.sqrt(Math.log(0)/40));

		System.out.println(Math.sqrt(Math.log(0)/0));

		System.out.println(0.7*(Math.sqrt(Math.log(0)/40)));

		System.out.println((90.0 / (3.0 * 100)) + (0.7*(Math.sqrt(Math.log(0)/3.0))));

		System.out.println((Math.sqrt(Math.log(0)/40)) >= (Math.sqrt(Math.log(0)/40)));
	}

	public static void tryThisToo(Object o){
		System.out.println("Object");
	}

	public static void tryThisToo(Integer i){
		System.out.println("Integer");
	}




	public static void tryThis() throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineInitializationException{
		String gdl = " ( ( role lp ) ( init f ) ( <= p ( true f ) ) ( <= p p ) ( legal lp m ) ( <= terminal ( not ( true f ) ) ) ( <= ( goal lp 100 ) p ) ( <= ( goal lp 0 ) ( not p ) ) ) ";

		Game g = Game.createEphemeralGame(gdl);

		List<Gdl> description = g.getRules();

        // Create the executor service that will run the propnet manager that creates the propnet
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create the propnet creation manager
        SeparateInternalPropnetManager manager = new SeparateInternalPropnetManager(description, Long.MAX_VALUE);

  	  	// Start the manager
  	  	executor.execute(manager);

  	  	// Shutdown executor to tell it not to accept any more task to execute.
		// Note that this doesn't interrupt previously started tasks.
		executor.shutdown();

		// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
		try{
			executor.awaitTermination(300000L, TimeUnit.MILLISECONDS);
		}catch(InterruptedException e){ // The thread running the verifier has been interrupted => stop the test
			executor.shutdownNow(); // Interrupt everything
			e.printStackTrace();
			Thread.currentThread().interrupt();
			return;
		}

		// Here the available time has elapsed, so we must interrupt the thread if it is still running.
		executor.shutdownNow();

		// Wait for the thread to actually terminate
		while(!executor.isTerminated()){

			// If the thread didn't terminate, wait for a minute and then check again
			try{
				executor.awaitTermination(1, TimeUnit.MINUTES);
			}catch(InterruptedException e) {
				// If this exception is thrown it means the thread that is executing the verification
				// of the state machine has been interrupted. If we do nothing this state machine could be stuck in the
				// while loop anyway until all tasks in the executor have terminated, thus we break out of the loop and return.
				// What happens to the still running tasks in the executor? Who will make sure they terminate?
				e.printStackTrace();
				Thread.currentThread().interrupt();
				return;
			}
		}

		// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.

		ImmutablePropNet propnet = manager.getImmutablePropnet();
		ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();

		DynamicPropNet dp = manager.getDynamicPropnet();

		System.out.println(dp.toString());

		// Create the state machine giving it the propnet and the propnet state.
		// NOTE that if any of the two is null, it means that the propnet creation/initialization went wrong
		// and this will be detected by the state machine during initialization.
		SeparateInternalPropnetStateMachine thePropnetMachine = new SeparateInternalPropnetStateMachine(new Random(), propnet, propnetState);

		thePropnetMachine.initialize(description, Long.MAX_VALUE);

		CompactMachineState state = thePropnetMachine.getCompactInitialState();

		System.out.println(state);

		List<CompactRole> roles = thePropnetMachine.getCompactRoles();

		System.out.println(roles.size());
		System.out.println(roles.get(0));

		System.out.println(thePropnetMachine.isTerminal(state));

		List<CompactMove> m = thePropnetMachine.getCompactLegalMoves(state, roles.get(0));
		CompactMachineState next = thePropnetMachine.getCompactNextState(state, m);

		System.out.println(next);

		System.out.println(thePropnetMachine.isTerminal(next));
		System.out.println(thePropnetMachine.getGoal(next, roles.get(0)));



	}

	public static void provaInsiemi(){
		Set<Integer> set1 = new HashSet<Integer>();
		for(int i = 0; i < 20; i++){
			set1.add(new Integer(i));
		}

		System.out.print("S1 = {");
		for(Integer i : set1){
			System.out.print(" " + i.intValue());
		}
		System.out.println(" }");

		System.out.println();

		System.out.print("S1 = {");
		for(Integer i : set1){
			System.out.print(" " + i.intValue());
		}
		System.out.println(" }");
	}

	public static void provaUgualeUguale(){
		ExternalizedStateConstant trueConstant = new ExternalizedStateConstant(true);
		ExternalizedStateComponent trueComponent = trueConstant;

		System.out.println(trueConstant == trueComponent);


		int[] a = new int[5];

		a[0] = 2;
		a[1] = 4;
		a[2] = 6;
		a[3] = 8;
		a[4] = 10;

		int[] b = a.clone();

		System.out.print("a = [ ");
		for(int i = 0; i < a.length; i++){
			System.out.print(a[i] + " ");
		}
		System.out.println("]");

		System.out.println();

		System.out.print("b = [ ");
		for(int i = 0; i < b.length; i++){
			System.out.print(b[i] + " ");
		}
		System.out.println("]");

		a[0] = 2;
		a[1] = 3;
		a[2] = 6;
		a[3] = 7;
		a[4] = 10;

		System.out.println();

		System.out.print("a = [ ");
		for(int i = 0; i < a.length; i++){
			System.out.print(a[i] + " ");
		}
		System.out.println("]");

		System.out.println();

		System.out.print("b = [ ");
		for(int i = 0; i < b.length; i++){
			System.out.print(b[i] + " ");
		}
		System.out.println("]");
	}

	public static void provaOverflow(){
		int n = Integer.MAX_VALUE-3;

		n = -3;

		for(int i = 0; i < 32; i++){
			System.out.print(n&1);
			n = n >> 1;
		}


	}

	public static void checkPropnetStructure(String gameKey){
		GameRepository theRepository = GameRepository.getDefaultRepository();

		List<Gdl> description = theRepository.getGame(gameKey).getRules();

		ForwardInterruptingPropNet propNet = null;

		long startTime = System.currentTimeMillis();

		try {
			propNet = ForwardInterruptingPropNetFactory.create(description, false);
			System.out.println("Done initializing propnet; took " + (System.currentTimeMillis() - startTime) + "ms, propnet has " + propNet.getComponents().size() + " components and " + propNet.getNumLinks() + " links");
			System.out.println("Propnet has " +propNet.getNumAnds()+" ands; "+propNet.getNumOrs()+" ors; "+propNet.getNumNots()+" nots");
			System.out.println("Propnet has " +propNet.getNumBases() + " bases; "+propNet.getNumTransitions()+" transitions; "+propNet.getNumInputs()+" inputs");
		} catch (InterruptedException e) {
			System.out.println("Something went wrong with the creation of the propnet!");
			e.printStackTrace();
			return;
		}

		GamerLogger.setSpilloverLogfile("ProponetStructureCheck.log");

		ForwardInterruptingPropNetFactory.checkPropnetStructure(propNet);
	}


	public static void provaOpenbitset(){

		OpenBitSet bits = new OpenBitSet(20);

		System.out.println("Instantiation values:");

		for(int i = 0; i < bits.size(); i++){
			if(bits.fastGet(i)){
				System.out.print("1");
			}else{
				System.out.print("0");
			}
		}

		System.out.println();
		System.out.println("Total true bits: " + bits.cardinality());

		bits.fastSet(2);
		bits.fastSet(5);
		bits.fastSet(8);
		bits.fastSet(11);
		bits.fastSet(14);
		bits.fastSet(17);
		bits.fastSet(20);
		bits.fastSet(14);


		System.out.println();
		System.out.println("Initialization values:");

		for(int i = 0; i < bits.size(); i++){
			if(bits.fastGet(i)){
				System.out.print("1");
			}else{
				System.out.print("0");
			}
		}

		System.out.println();
		System.out.println("Total true bits: " + bits.cardinality());


		System.out.println();
		System.out.println("The 15th bit is " + bits.fastGet(14) + ".");

		System.out.println();
		System.out.println("All the values:");

		for(int i = 0; i < bits.size(); i++){
			if(bits.fastGet(i)){
				System.out.print("1 ");
			}else{
				System.out.print("0 ");
			}
		}

		bits.fastClear(bits.nextSetBit(0));
		bits.fastClear(2);

		System.out.println();
		System.out.println("All the values:");

		for(int i = 0; i < bits.size(); i++){
			if(bits.fastGet(i)){
				System.out.print("1 ");
			}else{
				System.out.print("0 ");
			}
		}

		OpenBitSet a = new OpenBitSet(120);
		a.fastSet(0);
		a.fastSet(1);
		a.fastSet(4);
		a.fastSet(5);
		a.fastSet(8);
		a.fastSet(9);
		a.fastSet(80);

		System.out.println();
		System.out.println();
		System.out.print("A = ");

		for(int i = 0; i < a.size(); i++){
			if(a.fastGet(i)){
				System.out.print("1");
			}else{
				System.out.print("0");
			}
		}

		OpenBitSet b = new OpenBitSet(20);
		b.fastSet(0);
		b.fastSet(2);
		b.fastSet(4);
		b.fastSet(6);
		b.fastSet(8);
		b.fastSet(10);

		System.out.println();
		System.out.println();
		System.out.print("B = ");

		for(int i = 0; i < b.size(); i++){
			if(b.fastGet(i)){
				System.out.print("1");
			}else{
				System.out.print("0");
			}
		}

		b.xor(a);

		System.out.println();
		System.out.println();
		System.out.print("A = ");

		for(int i = 0; i < b.size(); i++){
			if(b.fastGet(i)){
				System.out.print("1");
			}else{
				System.out.print("0");
			}
		}
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
	public static void printPropnet(String gameKey){

		GameRepository theRepository = GameRepository.getDefaultRepository();
        //GameRepository theRepository = new LocalFolderGameRepository("C:/Users/c.sironi/BITBUCKET REPOS/GGP-Base/GDLFolder");

		List<Gdl> description = theRepository.getGame(gameKey).getRules();

		ForwardInterruptingPropNet propNet = null;

		try {
			propNet = ForwardInterruptingPropNetFactory.create(description, true);
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
            BufferedWriter out = new BufferedWriter(new FileWriter(gameKey + "Propnet.dot", false));
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
            BufferedWriter out = new BufferedWriter(new FileWriter(gameKey + "InitPropnet.dot", false));
            out.write(string);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
	public static void printPropnetImprovements(String gameKey){

		GameRepository theRepository = GameRepository.getDefaultRepository();

		List<Gdl> description = theRepository.getGame(gameKey).getRules();

		ForwardInterruptingPropNet propNet = null;

		long startTime = System.currentTimeMillis();

		try {
			propNet = ForwardInterruptingPropNetFactory.create(description, false);
			System.out.println("Done initializing propnet; took " + (System.currentTimeMillis() - startTime) + "ms, propnet has " + propNet.getComponents().size() + " components and " + propNet.getNumLinks() + " links");
			System.out.println("Propnet has " +propNet.getNumAnds()+" ands; "+propNet.getNumOrs()+" ors; "+propNet.getNumNots()+" nots");
			System.out.println("Propnet has " +propNet.getNumBases() + " bases; "+propNet.getNumTransitions()+" transitions; "+propNet.getNumInputs()+" inputs");
		} catch (InterruptedException e) {
			System.out.println("Something went wrong with the creation of the propnet!");
			e.printStackTrace();
			return;
		}

		System.out.println();
		System.out.println("Printing constant components and their children:");

		Set<ForwardInterruptingComponent> components = propNet.getComponents();

		for(ForwardInterruptingComponent c : components){
			if(c instanceof ForwardInterruptingConstant){
				if(c.getValue()){
					System.out.println("TRUE COMPONENT");
				}else{
					System.out.println("FALSE COMPONENT");
				}

				System.out.println("[ ");
				for(ForwardInterruptingComponent cc : c.getOutputs()){
					if(cc instanceof ForwardInterruptingProposition){
						System.out.println("(PROPOSITION=" + ((ForwardInterruptingProposition) cc).getName() + ", NUM OUTPUTS=" + cc.getOutputs().size() + ", VALUE=" + cc.getValue() + ")");
					}else if(cc instanceof ForwardInterruptingTransition){
						System.out.println("(TRANSITION, NUM OUTPUTS=" + cc.getOutputs().size() + ", VALUE=" + cc.getValue() + ")");
					}else if(cc instanceof ForwardInterruptingConstant){
						System.out.println("(CONSTANT, NUM OUTPUTS=" + cc.getOutputs().size() + ", VALUE=" + cc.getValue() + ")");
					}else if(cc instanceof ForwardInterruptingAnd){
						System.out.println("(AND, NUM OUTPUTS=" + cc.getOutputs().size() + ", NUM INPUTS=" + cc.getInputs().size() + ", VALUE=" + cc.getValue() + ")");
					}else if(cc instanceof ForwardInterruptingOr){
						System.out.println("(OR, NUM OUTPUTS=" + cc.getOutputs().size() + ", NUM INPUTS=" + cc.getInputs().size() + ", VALUE=" + cc.getValue() + ")");
					}else if(cc instanceof ForwardInterruptingNot){
						System.out.println("(NOT, NUM OUTPUTS=" + cc.getOutputs().size() + ", NUM INPUTS=" + cc.getInputs().size() + ", VALUE=" + cc.getValue() + ")");
					}
				}
				System.out.println("[ ");
			}
		}

		String string = null;

		if(propNet != null){

			string = propNet.toString();
		}

		//System.out.println(string);


		// SAVE TO FILE
		try{
    		// Write propnet to file
            BufferedWriter out = new BufferedWriter(new FileWriter(gameKey + "Propnet.dot", false));
            out.write(string);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

/*

		if(propNet != null){
			System.out.println("Removing amomymous props");
			startTime = System.currentTimeMillis();
			ForwardInterruptingPropNetFactory.removeAnonymousPropositions(propNet);
			System.out.println("Done removing amomymous props; took " + (System.currentTimeMillis() - startTime) + "ms, propnet has " + propNet.getComponents().size() + " components and " + propNet.getNumLinks() + " links");
			System.out.println("Propnet has " +propNet.getNumAnds()+" ands; "+propNet.getNumOrs()+" ors; "+propNet.getNumNots()+" nots");
			System.out.println("Propnet has " +propNet.getNumBases() + " bases; "+propNet.getNumTransitions()+" transitions; "+propNet.getNumInputs()+" inputs");
			string = propNet.toString();
		}
*/
		/*
		// SAVE TO FILE
		try{
    		// Write propnet to file
            BufferedWriter out = new BufferedWriter(new FileWriter(gameKey + "AnonPropnet.dot", false));
            out.write(string);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

/*		if(propNet != null){
			System.out.println("Removing init props");
			startTime = System.currentTimeMillis();
			ForwardInterruptingPropNetFactory.removeInits(propNet);
			System.out.println("Done removing init props; took " + (System.currentTimeMillis() - startTime) + "ms, propnet has " + propNet.getComponents().size() + " components and " + propNet.getNumLinks() + " links");
			System.out.println("Propnet has " +propNet.getNumAnds()+" ands; "+propNet.getNumOrs()+" ors; "+propNet.getNumNots()+" nots");
			System.out.println("Propnet has " +propNet.getNumBases() + " bases; "+propNet.getNumTransitions()+" transitions; "+propNet.getNumInputs()+" inputs");
			string = propNet.toString();
		}


		// SAVE TO FILE
		try{
    		// Write propnet to file
            BufferedWriter out = new BufferedWriter(new FileWriter(gameKey + "NoInitPropnet.dot", false));
            out.write(string);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

*/
		if(propNet != null){
			System.out.println("Removing unreachable bases and inputs.");

			Collection<ForwardInterruptingProposition> baseProps = propNet.getBasePropositions().values();



			Set<ForwardInterruptingProposition> basesTrueByInit = new HashSet<ForwardInterruptingProposition>();

			//System.out.println("BASES BEFORE: [");

			for(ForwardInterruptingProposition p : baseProps){

			//	System.out.println(p.getName());

				if(((ForwardInterruptingTransition)p.getSingleInput()).isDependingOnInit()){
					basesTrueByInit.add(p);
				}
			}

			System.out.println("ALL BASE PROPS: " + baseProps.size());

			System.out.println("BASE PROPS TRUE BY INIT: " + basesTrueByInit.size());

			//System.out.println("]");

			System.out.println();

			startTime = System.currentTimeMillis();
			try {
				ForwardInterruptingPropNetFactory.removeUnreachableBasesAndInputs(propNet, basesTrueByInit);
			} catch (InterruptedException e) {
				System.out.println("Something went wrong with the removal of unreachable bases and inputs!");
				e.printStackTrace();
				return;
			}

			System.out.println();
			System.out.println("Done removing unreachable bases and inputs; took " + (System.currentTimeMillis() - startTime) + "ms, propnet has " + propNet.getComponents().size() + " components and " + propNet.getNumLinks() + " links");
			System.out.println("Propnet has " +propNet.getNumAnds()+" ands; "+propNet.getNumOrs()+" ors; "+propNet.getNumNots()+" nots");
			System.out.println("Propnet has " +propNet.getNumBases() + " bases; "+propNet.getNumTransitions()+" transitions; "+propNet.getNumInputs()+" inputs");
			//string = propNet.toString();

			//Collection<ForwardInterruptingProposition> newBaseProps = propNet.getBasePropositions().values();

			//System.out.println("BASES AFTER: [");

			//for(ForwardInterruptingProposition p : newBaseProps){

			//	System.out.println(p.getName());

			//}

			//System.out.println("]");


			System.out.println();
			System.out.println("Printing constant components and their children:");

			components = propNet.getComponents();

			for(ForwardInterruptingComponent c : components){
				if(c instanceof ForwardInterruptingConstant){
					if(c.getValue()){
						System.out.println("TRUE COMPONENT");
					}else{
						System.out.println("FALSE COMPONENT");
					}

					System.out.println("[ ");
					for(ForwardInterruptingComponent cc : c.getOutputs()){
						if(cc instanceof ForwardInterruptingProposition){
							System.out.println("(PROPOSITION=" + ((ForwardInterruptingProposition) cc).getName() + ", NUM OUTPUTS=" + cc.getOutputs().size() + ", VALUE=" + cc.getValue() + ")");
						}else if(cc instanceof ForwardInterruptingTransition){
							System.out.println("(TRANSITION, NUM OUTPUTS=" + cc.getOutputs().size() + ", VALUE=" + cc.getValue() + ")");
						}else if(cc instanceof ForwardInterruptingConstant){
							System.out.println("(CONSTANT, NUM OUTPUTS=" + cc.getOutputs().size() + ", VALUE=" + cc.getValue() + ")");
						}else if(cc instanceof ForwardInterruptingAnd){
							System.out.println("(AND, NUM OUTPUTS=" + cc.getOutputs().size() + ", NUM INPUTS=" + cc.getInputs().size() + ", VALUE=" + cc.getValue() + ")");
						}else if(cc instanceof ForwardInterruptingOr){
							System.out.println("(OR, NUM OUTPUTS=" + cc.getOutputs().size() + ", NUM INPUTS=" + cc.getInputs().size() + ", VALUE=" + cc.getValue() + ")");
						}else if(cc instanceof ForwardInterruptingNot){
							System.out.println("(NOT, NUM OUTPUTS=" + cc.getOutputs().size() + ", NUM INPUTS=" + cc.getInputs().size() + ", VALUE=" + cc.getValue() + ")");
						}
					}
					System.out.println("[ ");
				}
			}
		}


		// SAVE TO FILE
		try{
    		// Write propnet to file
            BufferedWriter out = new BufferedWriter(new FileWriter(gameKey + "NoUnreachablePropnet.dot", false));
            out.write(string);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


		//if(propNet != null){
		//	propNet.imposeConsistency();
		//	string = propNet.toString();
		//}


		// SAVE TO FILE
		//try{
    		// Write propnet to file
        //    BufferedWriter out = new BufferedWriter(new FileWriter(gameKey + "InitPropnet.dot", false));
        //    out.write(string);
        //    out.close();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

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
		//String BeLdescription = "( ( role robot ) ( base p ) ( base q ) ( base r ) ( base 1 ) ( base 2 ) ( base 3 ) ( base 4 ) ( base 5 ) ( base 6 ) ( base 7 ) ( input robot a ) ( input robot b ) ( input robot c ) ( init 1 ) ( legal robot a ) ( legal robot b ) ( legal robot c ) ( <= ( next p ) ( does robot a ) ( not ( true p ) ) ) ( <= ( next p ) ( does robot b ) ( true q ) ) ( <= ( next p ) ( does robot c ) ( true p ) ) ( <= ( next q ) ( does robot a ) ( true q ) ) ( <= ( next q ) ( does robot b ) ( true p ) ) ( <= ( next q ) ( does robot c ) ( true r ) ) ( <= ( next r ) ( does robot a ) ( true r ) ) ( <= ( next r ) ( does robot b ) ( true r ) ) ( <= ( next r ) ( does robot c ) ( true q ) ) ( <= ( next ?y ) ( true ?x ) ( successor ?x ?y ) ) ( <= ( goal robot 100 ) ( true p ) ( true q ) ( true r ) ) ( <= ( goal robot 0 ) ( not ( true p ) ) ) ( <= ( goal robot 0 ) ( not ( true q ) ) ) ( <= ( goal robot 0 ) ( not ( true r ) ) ) ( <= terminal ( true p ) ( true q ) ( true r ) ) ( <= terminal ( true 7 ) ) ( successor 1 2 ) ( successor 2 3 ) ( successor 3 4 ) ( successor 4 5 ) ( successor 5 6 ) ( successor 6 7 ) )";

		String BeLdescription = "((role player) (light p) (light q) (light r) (<= (legal player (switchOn ?x)) (not (true (on ?x))) (light ?x)) (<= (next (on ?x)) (does player (switchOn ?x))) (<= (next (on ?x)) (true (on ?x))) (<= terminal (true (on p)) (true (on q)) (true (on r))) (<= (goal player 100) (true (on p)) (true (on q)) (true (on r))))";


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
        CheckFwdInterrPropnetStateMachine thePropNetMachine;

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            if(!game.equals("ALL") && !gameKey.equals(game)) continue;

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine(new Random());
            thePropNetMachine = new CheckFwdInterrPropnetStateMachine(new Random(), maxPropnetCreationTime);

            theReference.initialize(description, Long.MAX_VALUE);
            try {
				thePropNetMachine.initialize(description, Long.MAX_VALUE);
			} catch (StateMachineInitializationException e) {
				System.out.println("Something went wrong with the creation of the propnet!");
				e.printStackTrace();
			}

            ForwardInterruptingPropNet propnet = thePropNetMachine.getPropNet();

            // It's possible to continue the execution only if the state machine managed to build the propnet
            if(propnet != null){

	            int step = 0;

	            //PRINTING THE PROPNET

	            //String propnetRepresentation = null;

	    		//propnetRepresentation = propnet.toString();

	    		//System.out.println(propnetRepresentation);
	    		//try{
		    		// Write propnet to file
		        //    BufferedWriter out = new BufferedWriter(new FileWriter("Step"+step+"Propnet"+gameKey+".dot", false));
		        //    out.write(propnetRepresentation);
		        //    out.close();
		        //} catch (IOException e) {
		        //     e.printStackTrace();
		        //}

	    		//MachineState init = thePropNetMachine.getInitialState();
	    		//if(init == null){
	    		//	System.out.println("Cavolo!");
	    		//}else{
	    		//	System.out.println("INIT STATE:");
	    		//	System.out.println(init);
	    		//}

	            //END OF PROPNET PRINTING

	    		ExplicitMachineState proverState = null;
	    		ExplicitMachineState propnetState = null;

	    		try {
	                proverState = theReference.getExplicitInitialState();
	            } catch(Exception e) {
	                GamerLogger.log("StateMachine", "Prover machine failed to generate an initial state!");
	                e.printStackTrace();
	            }
	    		try {
	                propnetState = thePropNetMachine.getExplicitInitialState();
	            } catch(Exception e) {
	                GamerLogger.log("StateMachine", "Propnet machine failed to generate an initial state!");
	                e.printStackTrace();
	            }

	    		//System.out.println("PROVER INITIAL STATE");
	    		//System.out.println(proverState);
	    		//System.out.println("PROPNET INITIAL STATE");
	    		//System.out.println(propnetState);

	    		if(!proverState.equals(propnetState)){
	    			System.out.println("INIT STATES DIVERSI PER: " + gameKey);
	    		}

	    		//System.out.println();

	    		while(!theReference.isTerminal(proverState)){

	    			step++;

	    			List<ExplicitMove> jointMove = null;
	    			try {
	    				jointMove = theReference.getRandomJointMove(proverState);
	                    proverState = theReference.getExplicitNextState(proverState, jointMove);
	                } catch(Exception e) {
	                    GamerLogger.log("StateMachine", "Prover machine failed to generate the next state!");
	                    e.printStackTrace();
	                }
	        		try {
	                    propnetState = thePropNetMachine.getExplicitNextState(propnetState, jointMove);
	                } catch(Exception e) {
	                    GamerLogger.log("StateMachine", "Propnet machine failed to generate the next state!");
	                    e.printStackTrace();
	                }
	        		//System.out.println("PROVER STATE");
	        		//System.out.println(proverState);
	        		//System.out.println("PROPNET STATE");
	        		//System.out.println(propnetState);

	        		if(!proverState.equals(propnetState)){
		    			System.out.println("STATES DIVERSI PER: " + gameKey);
		    		}

	        		/*
	        		System.out.println();


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
	                 *
	                 */

	    		}
            }
        }
	}

	/**
	 * This method performs one complete simulation from root to terminal state of the given game,
	 * using both the ProverStateMachine and the FwdInterrPropnetStateMachine. For each step
	 * it prints the content of the states computed by both the state machines and also saves the
	 * propnet state in a .dot format, so that it will be possible to check how the values of the
	 * propositions in the propnet changed at every step.
	 *
	 * @param game the GDL game description.
	 * @param maxPropnetCreationTime the maximum time that the propnet state machine has available
	 * to build the propnet.
	 */
	public static void provaGame2(String game, long initTime){

        ProverStateMachine theReference;
        InitializationSafeStateMachine thePropNetMachine;

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            if(!game.equals("ALL") && !gameKey.equals(game)) continue;

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine(new Random());
            Random random = new Random();
            thePropNetMachine = new InitializationSafeStateMachine(random, new FwdInterrPropnetStateMachine(random));

            theReference.initialize(description, Long.MAX_VALUE);
            try {
				thePropNetMachine.initialize(description, System.currentTimeMillis() + initTime);
			} catch (StateMachineInitializationException e) {
				System.out.println("Something went wrong with the creation of the propnet!");
				e.printStackTrace();
			}

            ForwardInterruptingPropNet propnet = ((FwdInterrPropnetStateMachine) thePropNetMachine.getTheRealMachine()).getPropNet();

            // It's possible to continue the execution only if the state machine managed to build the propnet
            if(propnet != null){

	            int step = 0;

	            //PRINTING THE PROPNET

	            //String propnetRepresentation = null;

	    		//propnetRepresentation = propnet.toString();

	    		//System.out.println(propnetRepresentation);
	    		//try{
		    		// Write propnet to file
		        //    BufferedWriter out = new BufferedWriter(new FileWriter("Step"+step+"Propnet"+gameKey+".dot", false));
		        //    out.write(propnetRepresentation);
		        //    out.close();
		        //} catch (IOException e) {
		        //     e.printStackTrace();
		        //}

	    		//MachineState init = thePropNetMachine.getInitialState();
	    		//if(init == null){
	    		//	System.out.println("Cavolo!");
	    		//}else{
	    		//	System.out.println("INIT STATE:");
	    		//	System.out.println(init);
	    		//}

	            //END OF PROPNET PRINTING

	    		ExplicitMachineState proverState = null;
	    		ExplicitMachineState propnetState = null;

	    		try {
	                proverState = theReference.getExplicitInitialState();
	            } catch(Exception e) {
	                GamerLogger.log("StateMachine", "Prover machine failed to generate an initial state!");
	                e.printStackTrace();
	            }
	    		try {
	                propnetState = thePropNetMachine.getExplicitInitialState();
	            } catch(Exception e) {
	                GamerLogger.log("StateMachine", "Propnet machine failed to generate an initial state!");
	                e.printStackTrace();
	            }

	    		System.out.println("PROVER INITIAL STATE");
	    		System.out.println(proverState);
	    		System.out.println("PROPNET INITIAL STATE");
	    		System.out.println(propnetState);

	    		if(!proverState.equals(propnetState)){
	    			System.out.println("INIT STATES DIVERSI PER: " + gameKey);
	    		}

	    		//System.out.println();

	    		while(!theReference.isTerminal(proverState)){

	    			step++;

	    			List<ExplicitMove> jointMove = null;
	    			try {
	    				jointMove = theReference.getRandomJointMove(proverState);
	                    proverState = theReference.getExplicitNextState(proverState, jointMove);
	                } catch(Exception e) {
	                    GamerLogger.log("StateMachine", "Prover machine failed to generate the next state!");
	                    e.printStackTrace();
	                }
	        		try {
	                    propnetState = thePropNetMachine.getExplicitNextState(propnetState, jointMove);
	                } catch(Exception e) {
	                    GamerLogger.log("StateMachine", "Propnet machine failed to generate the next state!");
	                    e.printStackTrace();
	                }
	        		System.out.println("PROVER STATE");
	        		System.out.println(proverState);
	        		System.out.println("PROPNET STATE");
	        		System.out.println(propnetState);

	        		if(!proverState.equals(propnetState)){
		    			System.out.println("STATES DIVERSI PER: " + gameKey);
		    		}


	        		System.out.println();

/*
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
	                 *
	                 */

	    		}

	    		List<Double> proverGoals = null;
	    		try {
					proverGoals = theReference.getGoals(proverState);
				} catch (GoalDefinitionException | StateMachineException e) {
					GamerLogger.log("StateMachine", "Prover machine failed to compute goals!");
					e.printStackTrace();
				}

	    		List<Double> propnetGoals = null;
	    		try {
					propnetGoals = thePropNetMachine.getGoals(propnetState);
				} catch (GoalDefinitionException | StateMachineException e) {
					GamerLogger.log("StateMachine", "Propnet machine failed to compute goals!");
					e.printStackTrace();
				}

	    		if(proverGoals != null)
	    			System.out.println(proverGoals);

	    		if(propnetGoals != null)
	    			System.out.println(proverGoals);
            }
        }
	}


	/**
	 * This method performs one complete simulation from root to terminal state of the given game,
	 * using both the ProverStateMachine and the ExtendedStatePropnetStateMachine. For each step
	 * it prints the content of the states computed by both the state machines and also saves the
	 * propnet state in a .dot format, so that it will be possible to check how the values of the
	 * propositions in the propnet changed at every step.
	 *
	 * NOTE that this method doesn't check the propnet building time. It will
	 * wait indefinitely for the propnet to build.
	 *
	 * @param game the GDL game description.
	 */
	public static void provaGameExtendedPropnet(String game){

        ProverStateMachine theReference;
        FwdInterrPropnetStateMachine thePropNetMachine;

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            if(!gameKey.equals(game)) continue;

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine(new Random());
            thePropNetMachine = new FwdInterrPropnetStateMachine(new Random());

            theReference.initialize(description, Long.MAX_VALUE);
            try {
				thePropNetMachine.initialize(description, Long.MAX_VALUE);
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

	    		ExplicitMachineState proverState = null;
	    		ExplicitMachineState propnetState = null;

	    		try {
	                proverState = theReference.getExplicitInitialState();
	            } catch(Exception e) {
	                GamerLogger.log("StateMachine", "Prover machine failed to generate an initial state!");
	                e.printStackTrace();
	            }
	    		try {
	                propnetState = thePropNetMachine.getExplicitInitialState();
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

	    			List<ExplicitMove> jointMove = null;
	    			try {
	    				jointMove = theReference.getRandomJointMove(proverState);
	                    proverState = theReference.getExplicitNextState(proverState, jointMove);
	                } catch(Exception e) {
	                    GamerLogger.log("StateMachine", "Prover machine failed to generate the next state!");
	                    e.printStackTrace();
	                }
	        		try {
	                    propnetState = thePropNetMachine.getExplicitNextState(propnetState, jointMove);
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

	public static void printOptPropnetFromGameCache(String gameKey, String opts, long givenInitTime) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineInitializationException{

		GameRepository theRepository = GameRepository.getDefaultRepository();

		List<Gdl> description = theRepository.getGame(gameKey).getRules();

		//System.out.println(description);

		printOptPropnet(description, opts, givenInitTime);

	}

	public static void printOptPropnetFromDescription(String gdl, String opts, long givenInitTime) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineInitializationException{

		Game g = Game.createEphemeralGame(gdl);

		List<Gdl> description = g.getRules();

		//System.out.println(description);

		printOptPropnet(description, opts, givenInitTime);

	}

	/**
	 * Given a GDL description, this method builds the propnet optimizing it as specified and then prints it.
	 *
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 * @throws GoalDefinitionException
	 * @throws StateMachineInitializationException
	 */
	public static void printOptPropnet(List<Gdl> description, String opts, long givenInitTime) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineInitializationException{

		SeparateInternalPropnetManager manager =  new SeparateInternalPropnetManager(description, System.currentTimeMillis() + givenInitTime, parseOptimizations(opts));

		PropNetManagerRunner.runPropNetManager(manager, givenInitTime);

		DynamicPropNet dynamicPropnet = manager.getDynamicPropnet();

		if(dynamicPropnet != null){
			System.out.println("Printing propnet:");
			System.out.println(dynamicPropnet.toString());
		}else{
			System.out.println("Impossible to print propnet: creation failed.");
		}

	}

	private static OptimizationCaller[] parseOptimizations(String opts){

		if(opts.equalsIgnoreCase("none")){
			return new OptimizationCaller[0];
		}

		if(opts.equalsIgnoreCase("default")){
			return null;
		}

		String[] splitOpts = opts.split("-");

		if(splitOpts.length < 1){
			throw new IllegalArgumentException();
		}

		OptimizationCaller[] optimizations = new OptimizationCaller[splitOpts.length];

		for(int i = 0; i < splitOpts.length; i++){
			switch(splitOpts[i]){
				case "0":
					optimizations[i] = new OptimizeAwayConstants();
					break;
				case "1":
					optimizations[i] = new RemoveAnonPropositions();
					break;
				case "2":
					optimizations[i] = new OptimizeAwayConstantValueComponents();
					break;
				case "3":
					optimizations[i] = new RemoveOutputlessComponents();
					break;
				default:
					throw new IllegalArgumentException();
			}
		}

		return optimizations;
	}



}

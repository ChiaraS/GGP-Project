package org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MCSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class HybridMcsManager {

	/**
	 * All the game-dependent and global parameters needed by the McsManager and its strategies.
	 * Must be reset between games.
	 */
	private GameDependentParameters gameDependentParameters;

	/**
	 * The game state currently being searched.
	 */
	private MachineState currentState;

	/**
	 * The statistics for all the legal moves for myRole in the state currently being searched.
	 */
	private CompleteMoveStats[] currentMovesStatistics;

	/**
	 * The strategy that this MCS manager must use to perform playouts.
	 */
	private PlayoutStrategy playoutStrategy;

	/**
	 * The state machine that this MCS manager uses to reason on the game
	 */
	//private AbstractStateMachine theMachine;

	/**
	 * The role performing the search.
	 */
	//private Role myRole;

	/**
	 * Maximum depth that the MCS algorithm must visit.
	 */
	private int maxSearchDepth;

	/**
	 *
	 */
	private Random random;

	/**
	 * Number of simulations per search that this MCS manager can perform.
	 * NOTE that if this number is set to a positive number then the manager
	 * will ignore any time limit and always perform the exact number of
	 * simulations specified by this parameter.
	 */
	private int numExpectedIterations;

	/**
	 * Number of performed iterations.
	 */
	//private int iterations;

	/**
	 * Number of all visited states since the start of the search.
	 */
	//private int visitedNodes;

	/**
	 * Start time of last performed search.
	 */
	//private long searchStart;

	/**
	 * End time of last performed search.
	 */
	//private long searchEnd;

	/**
	 *
	 */
	public HybridMcsManager(Random random, GamerSettings gamerSettings, String gamerType) {

		GamerLogger.log("SearchManagerCreation", "Creating search manager for gamer " + gamerType + ".");

		this.random = random;

		this.maxSearchDepth = gamerSettings.getIntPropertyValue("SearchManager.maxSearchDepth");

		if(gamerSettings.specifiesProperty("SearchManager.numExpectedIterations")){
			this.numExpectedIterations = gamerSettings.getIntPropertyValue("SearchManager.numExpectedIterations");
		}else{
			this.numExpectedIterations = -1;
		}
		this.gameDependentParameters = new GameDependentParameters();

		// Create strategies according to the types specified in the gamer configuration
		SharedReferencesCollector sharedReferencesCollector = new SharedReferencesCollector();

		String[] multiPropertyValue;

		multiPropertyValue = gamerSettings.getIDPropertyValue("SearchManager.playoutStrategyType");

		try {
			this.playoutStrategy = (PlayoutStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PLAYOUT_STRATEGIES.getConcreteClasses(),
					multiPropertyValue[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, multiPropertyValue[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating PlayoutStrategy " + gamerSettings.getIDPropertyValue("SearchManager.playoutStrategyType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		sharedReferencesCollector.setPlayoutStrategy(this.playoutStrategy);

		this.playoutStrategy.setReferences(sharedReferencesCollector);

		GamerLogger.log("SearchManagerCreation", "Creation of search manager for gamer " + gamerType + " ended successfully.");

		this.currentState = null;
		this.currentMovesStatistics = null;

		//this.theMachine = theMachine;
		//this.myRole = myRole;

		//this.iterations = 0;
		//this.visitedNodes = 0;
		//this.searchStart = 0;
		//this.searchEnd = 0;

	}

	/**
	public HybridMCSManager(PlayoutStrategy playoutStrategy, AbstractStateMachine theMachine, Role myRole, int maxSearchDepth, Random random) {

		this.currentState = null;
		this.currentMovesStatistics = null;

		this.playoutStrategy = playoutStrategy;
		this.theMachine = theMachine;
		this.myRole = myRole;
		this.maxSearchDepth = maxSearchDepth;
		this.random = random;

		this.iterations = 0;
		this.visitedNodes = 0;
		this.searchStart = 0;
		this.searchEnd = 0;

	}*/

	public String printSearchManager(){

		String toLog = "MCS_MANAGER_TYPE = " + this.getClass().getSimpleName();

		//String toLog = "MCS manager initialized with the following state mahcine " + this.theMachine.getName();

		toLog += "\n\nMAX_SEARCH_DEPTH = " + this.maxSearchDepth + "\nNUM_EXPECTED_ITERATIONS = " + numExpectedIterations;

		//toLog += "\nMCS manager initialized with the following playout strategy: ";

		//for(Strategy s : this.strategies){
		//	toLog += "\n" + s.printStrategy();
		//}

		toLog += "\nPLAYOUT_STRATEGY =" + this.playoutStrategy.printComponent("\n  ");

		toLog += "\nabstract_state_machine = " + (this.gameDependentParameters.getTheMachine() == null ? "null" : this.gameDependentParameters.getTheMachine().getName());
		toLog += "\nnum_roles = " + this.gameDependentParameters.getNumRoles();
		toLog += "\nmy_role_index = " + this.gameDependentParameters.getMyRoleIndex();
		toLog += "\ncurrent_game_step = " + this.gameDependentParameters.getGameStep();
		String stepScoreSumForRoleStirng;
		if(this.gameDependentParameters.getStepScoreSumForRoles() != null){
			stepScoreSumForRoleStirng = "[ ";
			for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getStepScoreSumForRoles().length; roleIndex++){
				stepScoreSumForRoleStirng += (this.gameDependentParameters.getStepScoreSumForRoles()[roleIndex] + " ");
			}
			stepScoreSumForRoleStirng += "]";
		}else{
			stepScoreSumForRoleStirng =	"null";
		}
		toLog += "\nstep_score_sum_for_role = " + stepScoreSumForRoleStirng;
		toLog += "\nstep_iterations = " + this.gameDependentParameters.getStepIterations();
		toLog += "\nstep_visited_nodes = " + this.gameDependentParameters.getStepVisitedNodes();
		toLog += "\nstep_search_duration = " + this.gameDependentParameters.getStepSearchDuration();
		toLog += "\ncurrent_iteration_visited_nodes = " + this.gameDependentParameters.getCurrentIterationVisitedNodes();

		return toLog;

	}

	public void clearManager(){

		this.playoutStrategy.clearComponent();

		this.gameDependentParameters.clearGameDependentParameters();

		this.currentState = null;

		this.currentMovesStatistics = null;

	}

	public void setUpManager(AbstractStateMachine theMachine, int numRoles, int myRoleIndex, long actualPlayClock){

		this.gameDependentParameters.resetGameDependentParameters(theMachine, numRoles, myRoleIndex, actualPlayClock);

		this.playoutStrategy.setUpComponent();

	}

	public CompleteMoveStats getBestMove() throws MCSException{

		if(this.currentMovesStatistics!=null){
			List<Integer> chosenMovesIndices = new ArrayList<Integer>();

			double maxAvgScore = -1;
			double currentAvgScore;

			// For each legal move check the average score
			for(int i = 0; i < this.currentMovesStatistics.length; i++){

				int visits =  this.currentMovesStatistics[i].getVisits();

				//System.out.println("Visits: " + visits);

				double scoreSum = this.currentMovesStatistics[i].getScoreSum();

				//System.out.println("Score sum: " + scoreSum);

				/**
				 * Extra check to make sure that the visits exceed the maximum
				 * feasible value for an int type.
				 * TODO: remove this check once you are reasonably sure that
				 * this can never happen.
				 */
				if(visits < 0){
					throw new RuntimeException("Negative value for visits : VISITS=" + visits + ".");
				}

				if(visits == 0){
					// Default score for unvisited moves
					currentAvgScore = -1;

					//System.out.println("Default move average score: " + currentAvgScore);

				}else{
					// Compute average score
					currentAvgScore = scoreSum / ((double) visits);

					//System.out.println("Computed average score: " + currentAvgScore);
				}

				//System.out.println("Max avg score: " + maxAvgScore);

				// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
				if(currentAvgScore > maxAvgScore){
					maxAvgScore = currentAvgScore;
					chosenMovesIndices.clear();
					chosenMovesIndices.add(new Integer(i));
					//System.out.println("Resetting.");
				}else if(currentAvgScore == maxAvgScore){
					chosenMovesIndices.add(new Integer(i));

					//System.out.println("Adding index: " + i);
				}
			}

			//System.out.println("Number of indices: " + chosenMovesIndices.size());

			int bestMoveIndex = chosenMovesIndices.get(this.random.nextInt(chosenMovesIndices.size()));

			return this.currentMovesStatistics[bestMoveIndex];
		}else{
			throw new MCSException("Impossible to compute best move without any move statistic.");
		}
	}


	public void search(MachineState state, long timeout) throws MCSException{

		List<Role> roles = this.gameDependentParameters.getTheMachine().getRoles();

		Role myRole = roles.get(this.gameDependentParameters.getMyRoleIndex());

		// If the state is different from the last searched state,
		// remove the old state and create new move statistics.
		if(!(state.equals(this.currentState))){

			this.currentState = state;

			List<Move> legalMoves;
			try {
				legalMoves = this.gameDependentParameters.getTheMachine().getLegalMoves(this.currentState,  myRole);
			} catch (MoveDefinitionException | StateMachineException e) {
				GamerLogger.log("MCSManager", "Error when computing legal moves for my role in the root state before starting Monte Carlo search.");
				GamerLogger.logStackTrace("MCSManager", e);
				throw new MCSException("Impossible to perform search: legal moves cannot be computed and explored in the given state.", e);
			}

			this.currentMovesStatistics = new CompleteMoveStats[legalMoves.size()];

			for(int i = 0; i < this.currentMovesStatistics.length; i++){
				this.currentMovesStatistics[i] = new CompleteMoveStats(legalMoves.get(i));
			}

		} // Otherwise proceed with the search using the old statistics and updating them.

		Move myCurrentMove;
		List<Move> jointMove;
		MachineState nextState;
		SimulationResult[] simulationResults;
		//int[] playoutVisitedNodes = new int[1];
		double myGoal;

		long searchStart = System.currentTimeMillis();
		// Analyze every move, iterating until the timeout is reached.
		for (int i = 0; true; i = (i+1) % this.currentMovesStatistics.length) {
		    if (this.timeToStopSearch(timeout))
		        break;

		    this.gameDependentParameters.resetIterationStatistics();

		    // Get the move.
		    myCurrentMove = this.currentMovesStatistics[i].getTheMove();

			try {
				// Get a random joint move where my role plays its currently analyzed move.
				jointMove = this.gameDependentParameters.getTheMachine().getRandomJointMove(this.currentState, myRole, myCurrentMove);
				// Get the state reachable with this joint move.
				nextState =  this.gameDependentParameters.getTheMachine().getNextState(this.currentState, jointMove);
				// Increase number of visited nodes
				this.gameDependentParameters.increaseCurrentIterationVisitedNodes();
				// Get the goals obtained by performing playouts from this state.
				simulationResults = this.playoutStrategy.playout(null, jointMove, nextState, this.maxSearchDepth-1);
			} catch (TransitionDefinitionException | StateMachineException | MoveDefinitionException e) {
				// NOTE: when an exception is thrown we consider the iteration still valid getting a reward of 0 for all players.
				// In this case, moves that lead to game situations that the state machine cannot deal with correctly are penalized.
				// Another option would be to skip the iteration and ignore it completely.
				GamerLogger.logError("McsManager", "Cannot compute joint move or next state. Stopping iteration and returning a goal of 0 for all roles.");

				double[] goals = new double[this.gameDependentParameters.getNumRoles()];
				for(int j = 0; j < goals.length; j++) {
					goals[j] = 0;
				}
				simulationResults = new SimulationResult[1];
				simulationResults[0] = new SimulationResult(goals);

			}

			for(SimulationResult simulationResult : simulationResults) {
				myGoal = simulationResult.getTerminalGoals()[this.gameDependentParameters.getMyRoleIndex()];

				this.currentMovesStatistics[i].incrementVisits();
				this.currentMovesStatistics[i].incrementScoreSum(myGoal);

				this.gameDependentParameters.increaseCurrentIterationVisitedNodes(simulationResult.getPlayoutLength());
				this.gameDependentParameters.increaseStepIterations();
				this.gameDependentParameters.increaseStepScoreSumForRoles(simulationResult.getTerminalGoals());
			}

			this.gameDependentParameters.increaseStepVisitedNodes(this.gameDependentParameters.getCurrentIterationVisitedNodes());

			// If something goes wrong in advancing with the current move, assume that the game length corresponds to the
			// number of visited nodes so far in the current iteration
			// TODO: if we use this sample we might underestimate the game length. Should this sample be ignored? Note that
			// if we want to ignore this sample we must remember that when computing the average length we have to decrease
			// by 1 the number of iterations by which we divide the total game length sum.
			this.gameDependentParameters.increaseStepGameLengthSum(this.gameDependentParameters.getCurrentIterationVisitedNodes());
		}

		long searchEnd = System.currentTimeMillis();

		this.gameDependentParameters.increaseStepSearchDuration(searchEnd-searchStart);
	}

	/*
	public void resetSearch(){
		this.currentState = null;
		this.currentMovesStatistics = null;
	}

	public int getIterations(){
		return this.iterations;
	}

	public int getVisitedNodes(){
		return this.visitedNodes;
	}

	public long getSearchTime(){
		return (this.searchEnd - this.searchStart);
	}*/

	/**
	 * Method that checks when it's time to stop the search.
	 */
	private boolean timeToStopSearch(long timeout){

		if(this.numExpectedIterations > 0){

			return this.gameDependentParameters.getStepIterations() == this.numExpectedIterations;

		}else{

			return System.currentTimeMillis() >= timeout;

		}

	}

	public void beforeMoveActions(int currentGameStep, boolean metagame){

		this.gameDependentParameters.setGameStep(currentGameStep);

		this.gameDependentParameters.setMetagame(metagame);

	}

	public void afterMetagameActions(){
		this.gameDependentParameters.setMetagame(false);
	}

	public double[] getStepScoreSumForRoles(){
		return this.gameDependentParameters.getStepScoreSumForRoles();
	}

	public int getStepIterations(){
		return this.gameDependentParameters.getStepIterations();
	}

	public double getStepVisitedNodes(){
		return this.gameDependentParameters.getStepVisitedNodes();
	}

	public long getStepSearchDuration(){
		return this.gameDependentParameters.getStepSearchDuration();
	}

	/**
	 * ATTENTION! This method has to be used ONLY when testing the propnet speed, NEVER
	 * change this parameter otherwise.
	 */
	public void setNumExpectedIterations(int numExpectedIterations) {
		this.numExpectedIterations = numExpectedIterations;
	}

	public int getNumExpectedIterations() {
		return this.numExpectedIterations;
	}

}

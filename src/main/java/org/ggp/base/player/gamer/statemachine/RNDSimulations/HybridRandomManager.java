package org.ggp.base.player.gamer.statemachine.RNDSimulations;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.RNDSimulations.exceptions.RandomException;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class HybridRandomManager {

	/**
	 * All the game-dependent and global parameters needed by the MctsManager and its strategies.
	 * Must be reset between games.
	 */
	private GameDependentParameters gameDependentParameters;

	/**
	 * The strategy that this MCS manager must use to perform playouts.
	 */
	private PlayoutStrategy playoutStrategy;

	/**
	 * The game state currently being searched.
	 */
	private MachineState currentState;

	/**
	 * Maximum depth that the MCTS algorithm must visit.
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
	 *
	 */
	public HybridRandomManager(Random random, GamerSettings gamerSettings, String gamerType) {

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

		this.currentState = null;

		GamerLogger.log("SearchManagerCreation", "Creation of search manager for gamer " + gamerType + " ended successfully.");

	}

	public String printSearchManager(){

		String toLog = "RANDOM_MANAGER_TYPE = " + this.getClass().getSimpleName();

		toLog += "\n\nMAX_SEARCH_DEPTH = " + this.maxSearchDepth + "\nNUM_EXPECTED_ITERATIONS = " + numExpectedIterations;

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

	}

	public void setUpManager(AbstractStateMachine theMachine, int numRoles, int myRoleIndex, long actualPlayClock){

		this.gameDependentParameters.resetGameDependentParameters(theMachine, numRoles, myRoleIndex, actualPlayClock);

		this.playoutStrategy.setUpComponent();

	}

	public CompleteMoveStats getBestMove() throws RandomException{

		if(this.currentState == null) {
			GamerLogger.log("RandomManager", "Error when looking for best move for my role.");
			throw new RandomException("Impossible to get best move: no state is set. Perform first the serach on a state.");
		}

		Role myRole = this.gameDependentParameters.getTheMachine().getRoles().get(this.gameDependentParameters.getMyRoleIndex());

		List<Move> legalMoves;
		try {
			legalMoves = this.gameDependentParameters.getTheMachine().getLegalMoves(this.currentState,  myRole);
		} catch (MoveDefinitionException | StateMachineException e) {
			GamerLogger.log("RandomManager", "Error when computing legal moves for my role when looking for best move.");
			GamerLogger.logStackTrace("RandomManager", e);
			throw new RandomException("Impossible to get best move: legal moves cannot be computed in the given state.", e);
		}

		if(legalMoves.size() < 0) {
			GamerLogger.log("RandomManager", "Error when looking for best move for my role.");
			throw new RandomException("Impossible to get best move: no legal moves found in the given state fo my role.");
		}

		return new CompleteMoveStats(legalMoves.get(this.random.nextInt(legalMoves.size())));

	}


	public void search(MachineState state, long timeout) throws RandomException{

		this.currentState = state;

		SimulationResult[] simulationResults;

		long searchStart = System.currentTimeMillis();

		while(!this.timeToStopSearch(timeout)) {

			this.gameDependentParameters.resetIterationStatistics();

			// Get the goals obtained by performing playouts from this state.
			simulationResults = this.playoutStrategy.playout(null, this.currentState, this.maxSearchDepth);

			for(SimulationResult simulationResult : simulationResults) {
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

	public int getStepVisitedNodes(){
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


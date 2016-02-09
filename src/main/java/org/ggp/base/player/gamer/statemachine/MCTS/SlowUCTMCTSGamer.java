/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.Random;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions.MCTSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.RandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMove;
import org.ggp.base.player.gamer.statemachine.propnet.InternalPropnetGamer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

/**
 * This gamer performs UCT Monte Carlo Tree Search.
 *
 * The gamer can be set in two ways:
 * - [DUCT=true] (default): the player will perform Decoupled UCT Monte Carlo Tree Search.
 * - [DUCT=false]: the player will perform Sequential UCT Monte Carlo Tree Search.
 *
 * {At the beginning of each game it tries to build the propnet. If it builds it will use for the
 * whole game the state machine based on the propnet otherwise it will use the cached prover.
 * Depending on the chosen state machine it will perform DUCT using the corresponding tree structure
 * (i.e. the one that uses internal propnet states to perform MCTS if the propnet managed to build,
 * the one that uses standard states otherwise). TODO: not true yet. Now it only uses the propnet
 * state machine and throws exception if it cannot be built.}
 *
 * @author C.Sironi
 *
 */
public abstract class SlowUCTMCTSGamer extends InternalPropnetGamer {

	/**
	 * Game step. Keeps track of the current game step.
	 */
	private int gameStep;

	/**
	 * Parameters used by the MCTS manager.
	 */
	private double c;
	private double uctOffset;
	private int gameStepOffset;
	private int maxSearchDepth;
	/**
	 * True if this player must use a manager that runs the DUCT version
	 * of Monte Carlo Tree Search, false otherwise.
	 */
	protected boolean DUCT;


	/**
	 * The class that takes care of performing Monte Carlo tree search.
	 */
	protected InternalPropnetMCTSManager mctsManager;

	/**
	 *
	 */
	public SlowUCTMCTSGamer() {
		// TODO: change code so that the parameters can be set from outside.

		super();

		this.DUCT = true;
		this.c = 0.7;
		this.uctOffset = 0.01;
		this.gameStepOffset = 2;
		this.maxSearchDepth = 500;

	}

	/**
	 *
	 */
	public SlowUCTMCTSGamer(InternalPropnetStateMachine thePropnetMachine) {
		// TODO: change code so that the parameters can be set from outside.

		super(thePropnetMachine);

		this.DUCT = true;
		this.c = 0.7;
		this.uctOffset = 0.01;
		this.gameStepOffset = 2;
		this.maxSearchDepth = 500;

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineMetaGame(long)
	 */
	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		// For now the player can play only with the state machine based on the propnet.
		// TODO: temporary solution! FIX!
		// We throw an exception if the state machine based on the propnet couldn't be initialized.
		if(this.thePropnetMachine == null){
			throw new StateMachineException("Impossible to play without the state machine based on the propnet.");
		}

		long start = System.currentTimeMillis();
		long realTimeout = timeout - this.safetyMargin;
		// Information to log at the end of metagame.
		// As default value they are initialized with "-1". A value of "-1" for a parameter means that
		// its value couldn't be computed (because there was no time or because of an error).
		long thinkingTime; // Total time used for metagame
		long searchTime = -1; // Actual time spent on the search
		int iterations = -1;
    	int visitedNodes = -1;
    	double iterationsPerSecond = -1;
    	double nodesPerSecond = -1;
		GamerLogger.log("Gamer", "Starting metagame with available thinking time " + (realTimeout-start) + "ms.");
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", "Game step;Thinking time(ms);Search time(ms);Iterations;Visited nodes;Iterations/second;Nodes/second;Chosen move;Move score sum;Move visits;Avg move score");

		this.gameStep = 0;

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		// Create the MCTS manager and start simulations.
		this.mctsManager = new InternalPropnetMCTSManager(this.DUCT, myRole, new UCTSelection(numRoles, myRole, r, uctOffset, c),
	       		new RandomExpansion(numRoles, myRole, r), new RandomPlayout(this.thePropnetMachine),
	       		new StandardBackpropagation(numRoles, myRole),	new MaximumScoreChoice(myRole, r),
	       		this.thePropnetMachine, gameStepOffset, maxSearchDepth);

		// If there is enough time left start the MCT search.
		// Otherwise return from metagaming.
		if(System.currentTimeMillis() < realTimeout){
			// If search fails during metagame?? TODO: should I throw exception here and say I'm not able to play?
			// If I don't it'll throw exception later anyway! Better stop now?

			GamerLogger.log("Gamer", "Starting search during metagame.");

			try {
				this.mctsManager.search(this.thePropnetMachine.getInternalInitialState(), realTimeout, gameStep+1);

				GamerLogger.log("Gamer", "Done searching during metagame.");
				searchTime = this.mctsManager.getSearchTime();
	        	iterations = this.mctsManager.getIterations();
	        	visitedNodes = this.mctsManager.getVisitedNodes();
	        	if(searchTime != 0){
		        	iterationsPerSecond = ((double) iterations * 1000)/((double) searchTime);
		        	nodesPerSecond = ((double) visitedNodes * 1000)/((double) searchTime);
	        	}else{
	        		iterationsPerSecond = 0;
	        		nodesPerSecond = 0;
	        	}
	        	thinkingTime = System.currentTimeMillis() - start;
			}catch(MCTSException e) {
				GamerLogger.logError("Gamer", "Exception during search while metagaming.");
				GamerLogger.logStackTrace("Gamer", e);

				thinkingTime = System.currentTimeMillis() - start;
			}
		}else{
			GamerLogger.log("Gamer", "No time to start the search during metagame.");

			thinkingTime = System.currentTimeMillis() - start;
		}

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", this.gameStep + ";" + thinkingTime + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";null;-1;-1;-1;");
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		long start = System.currentTimeMillis();
		long realTimeout = timeout - this.safetyMargin;

		// Information to log at the end of move selection.
		// As default value numeric parameters are initialized with "-1" and the others with "null".
		// A value of "-1" or "null" for a parameter means that its value couldn't be computed
		// (because there was no time or because of an error).
		long thinkingTime;
		long searchTime = -1;
		int iterations = -1;
    	int visitedNodes = -1;
    	double iterationsPerSecond = -1;
    	double nodesPerSecond = -1;
    	Move theMove = null;
    	long moveScoreSum = -1L;
    	long moveVisits = -1;
    	double moveAvgScore = -1;

		this.gameStep++;

		GamerLogger.log("Gamer", "Starting move selection for game step " + this.gameStep + " with available time " + (realTimeout-start) + "ms.");

		if(System.currentTimeMillis() < realTimeout){

			GamerLogger.log("Gamer", "Selecting move using MCTS.");

			InternalPropnetMachineState currentState = this.thePropnetMachine.stateToInternalState(this.getCurrentState());

			try {
				InternalPropnetMCTSNode currentNode = this.mctsManager.search(currentState, realTimeout, gameStep);
				MCTSMove selectedMove = this.mctsManager.getBestMove(currentNode);

				searchTime = this.mctsManager.getSearchTime();
				iterations = this.mctsManager.getIterations();
		    	visitedNodes = this.mctsManager.getVisitedNodes();
		    	if(searchTime != 0){
		        	iterationsPerSecond = ((double) iterations * 1000)/((double) searchTime);
		        	nodesPerSecond = ((double) visitedNodes * 1000)/((double) searchTime);
	        	}else{
	        		iterationsPerSecond = 0;
	        		nodesPerSecond = 0;
	        	}
		    	theMove = this.thePropnetMachine.internalMoveToMove(selectedMove.getTheMove());
		    	moveScoreSum = selectedMove.getScoreSum();
		    	moveVisits = selectedMove.getVisits();
		    	moveAvgScore = moveScoreSum / ((double) moveVisits);

				GamerLogger.log("Gamer", "Returning MCTS move " + theMove + ".");
			}catch(MCTSException e){
				GamerLogger.logError("Gamer", "MCTS failed to return a move.");
				GamerLogger.logStackTrace("Gamer", e);
				// If the MCTS manager failed to return a move return a random one.
				theMove = this.thePropnetMachine.getRandomMove(this.getCurrentState(), this.getRole());
				GamerLogger.log("Gamer", "Returning random move " + theMove + ".");
			}
		}else{
			// If there is no time return a random move.
			//GamerLogger.log("Gamer", "No time to start the search during metagame.");
			theMove = this.thePropnetMachine.getRandomMove(this.getCurrentState(), this.getRole());
			GamerLogger.log("Gamer", "No time to select next move using MCTS. Returning random move " + theMove + ".");
		}

		thinkingTime = System.currentTimeMillis() - start;

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", this.gameStep + ";" + thinkingTime + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";" + theMove + ";" + moveScoreSum + ";" + moveVisits + ";" + moveAvgScore + ";");

		// TODO: IS THIS NEEDED? WHEN?
		notifyObservers(new GamerSelectedMoveEvent(this.thePropnetMachine.getLegalMoves(this.getCurrentState(), this.getRole()), theMove, thinkingTime));

		return theMove;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineStop()
	 */
	@Override
	public void stateMachineStop() {

		this.mctsManager = null;
		super.stateMachineStop();

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineAbort()
	 */
	@Override
	public void stateMachineAbort() {

		this.mctsManager = null;
		super.stateMachineStop();

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.Gamer#getName()
	 */
	/*
	@Override
	public String getName(){
		String type = "";
		if(this.DUCT){
			type = "DUCT";
		}else{
			type = "SUCT";
		}
		return super.getName()+"-"+type;
	}*/

}

package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.Random;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.L4J2InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions.MCTSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.L4J2RandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMove;
import org.ggp.base.player.gamer.statemachine.propnet.L4J2InternalPropnetGamer;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public abstract class L4J2SlowUCTMCTSGamer extends L4J2InternalPropnetGamer {

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
	protected L4J2InternalPropnetMCTSManager mctsManager;

	/**
	 *
	 */
	public L4J2SlowUCTMCTSGamer() {
		// TODO: change code so that the parameters can be set from outside.

		this.DUCT = true;
		this.c = 1.4;
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
    	LOGGER.info("[Gamer] Starting metagame with available thinking time " + (realTimeout-start) + "ms.");
    	CSV_LOGGER.info("Game step;Thinking time(ms);Search time(ms);Iterations;Visited nodes;Iterations/second;Nodes/second;Chosen move;Move score sum;Move visits;Avg move score");

		this.gameStep = 0;

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		// Create the MCTS manager and start simulations.
		this.mctsManager = new L4J2InternalPropnetMCTSManager(this.DUCT, myRole, new UCTSelection(numRoles, myRole, r, uctOffset, c),
	       		new RandomExpansion(numRoles, myRole, r), new L4J2RandomPlayout(this.thePropnetMachine),
	       		new StandardBackpropagation(numRoles, myRole),	new MaximumScoreChoice(myRole, r),
	       		this.thePropnetMachine, gameStepOffset, maxSearchDepth);

		// If there is enough time left start the MCT search.
		// Otherwise return from metagaming.
		if(System.currentTimeMillis() < realTimeout){
			// If search fails during metagame?? TODO: should I throw exception here and say I'm not able to play?
			// If I don't it'll throw exception later anyway! Better stop now?

			LOGGER.info("[Gamer] Starting search during metagame.");

			try {
				this.mctsManager.search(this.thePropnetMachine.getInternalInitialState(), realTimeout, gameStep+1);

				LOGGER.info("[Gamer] Done searching during metagame.");
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
				LOGGER.error("[Gamer] Exception during search while metagaming.", e);

				thinkingTime = System.currentTimeMillis() - start;
			}
		}else{
			LOGGER.info("[Gamer] No time to start the search during metagame.");

			thinkingTime = System.currentTimeMillis() - start;
		}

		CSV_LOGGER.info("" + this.gameStep + ";" + thinkingTime + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";null;-1;-1;-1;");
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

		LOGGER.info("[Gamer] Starting move selection for game step " + this.gameStep + " with available time " + (realTimeout-start) + "ms.");

		if(System.currentTimeMillis() < realTimeout){

			LOGGER.info("[Gamer] Selecting move using MCTS.");

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

		    	LOGGER.info("[Gamer] Returning MCTS move " + theMove + ".");
			}catch(MCTSException e){
				LOGGER.error("[Gamer] MCTS failed to return a move.", e);
				// If the MCTS manager failed to return a move return a random one.
				theMove = this.thePropnetMachine.getRandomMove(this.getCurrentState(), this.getRole());
				LOGGER.error("[Gamer] Returning random move " + theMove + ".");
			}
		}else{
			// If there is no time return a random move.
			theMove = this.thePropnetMachine.getRandomMove(this.getCurrentState(), this.getRole());
			LOGGER.info("[Gamer] No time to select next move using MCTS. Returning random move " + theMove + ".");
		}

		thinkingTime = System.currentTimeMillis() - start;

		CSV_LOGGER.info("" + this.gameStep + ";" + thinkingTime + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";" + theMove + ";" + moveScoreSum + ";" + moveVisits + ";" + moveAvgScore + ";");

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
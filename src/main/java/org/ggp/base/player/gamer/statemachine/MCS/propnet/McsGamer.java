/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCS.propnet;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MCSException;
import org.ggp.base.player.gamer.statemachine.MCS.manager.propnet.InternalPropnetMCSManager;
import org.ggp.base.player.gamer.statemachine.MCS.manager.propnet.PnCompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PnRandomPlayout;
import org.ggp.base.player.gamer.statemachine.propnet.InternalPropnetGamer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

/**
 * This gamer performs Monte Carlo Search.
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
public class McsGamer extends InternalPropnetGamer {

	/**
	 * Game step. Keeps track of the current game step.
	 */
	private int gameStep;

	/**
	 * Parameters used by the MCS manager.
	 */
	private int maxSearchDepth;

	/**
	 * The class that takes care of performing Monte Carlo search.
	 */
	protected InternalPropnetMCSManager mcsManager;

	/**
	 *
	 */
//	private PlayoutStrategy playoutStrategy;

	/**
	 *
	 */
	public McsGamer() {

		super();
		this.maxSearchDepth = 500;

		//this.safetyMargin = 100000L;

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
		long realTimeout = timeout - this.selectMoveSafetyMargin;
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

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());

		// Create the MCS manager and start simulations.
		this.mcsManager = new InternalPropnetMCSManager(new PnRandomPlayout(this.thePropnetMachine),
				this.thePropnetMachine,	myRole, maxSearchDepth, this.random);

		// If there is enough time left start the MCT search.
		// Otherwise return from metagaming.
		if(System.currentTimeMillis() < realTimeout){
			// If search fails during metagame?? TODO: should I throw exception here and say I'm not able to play?
			// If I don't it'll throw exception later anyway! Better stop now?

			GamerLogger.log("Gamer", "Starting search during metagame.");

			try {
				this.mcsManager.search(this.thePropnetMachine.getCompactInitialState(), realTimeout);

				GamerLogger.log("Gamer", "Done searching during metagame.");
				searchTime = this.mcsManager.getSearchTime();
	        	iterations = this.mcsManager.getIterations();
	        	visitedNodes = this.mcsManager.getVisitedNodes();
	        	if(searchTime != 0){
		        	iterationsPerSecond = ((double) iterations * 1000)/((double) searchTime);
		        	nodesPerSecond = ((double) visitedNodes * 1000)/((double) searchTime);
	        	}else{
	        		iterationsPerSecond = 0;
	        		nodesPerSecond = 0;
	        	}
	        	thinkingTime = System.currentTimeMillis() - start;
			}catch(MCSException e) {
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
	public ExplicitMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		long start = System.currentTimeMillis();
		long realTimeout = timeout - this.selectMoveSafetyMargin;

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
    	ExplicitMove theMove = null;
    	double moveScoreSum = -1.0;
    	int moveVisits = -1;
    	double moveAvgScore = -1;

		this.gameStep++;

		GamerLogger.log("Gamer", "Starting move selection for game step " + this.gameStep + " with available time " + (realTimeout-start) + "ms.");

		if(System.currentTimeMillis() < realTimeout){

			GamerLogger.log("Gamer", "Selecting move using MCS.");

			CompactMachineState currentState = this.thePropnetMachine.convertToCompactMachineState(this.getCurrentState());

			try {
				this.mcsManager.search(currentState, realTimeout);
				PnCompleteMoveStats selectedMove = this.mcsManager.getBestMove();

				searchTime = this.mcsManager.getSearchTime();
				iterations = this.mcsManager.getIterations();
		    	visitedNodes = this.mcsManager.getVisitedNodes();
		    	if(searchTime != 0){
		        	iterationsPerSecond = ((double) iterations * 1000)/((double) searchTime);
		        	nodesPerSecond = ((double) visitedNodes * 1000)/((double) searchTime);
	        	}else{
	        		iterationsPerSecond = 0;
	        		nodesPerSecond = 0;
	        	}
		    	theMove = this.thePropnetMachine.convertToExplicitMove(selectedMove.getTheMove());
		    	moveScoreSum = selectedMove.getScoreSum();
		    	moveVisits = selectedMove.getVisits();
		    	moveAvgScore = moveScoreSum / ((double) moveVisits);

				GamerLogger.log("Gamer", "Returning MCS move " + theMove + ".");
			}catch(MCSException e){
				GamerLogger.logError("Gamer", "MCS failed to return a move.");
				GamerLogger.logStackTrace("Gamer", e);
				// If the MCS manager failed to return a move return a random one.
				theMove = this.thePropnetMachine.getRandomMove(this.getCurrentState(), this.getRole());
				GamerLogger.log("Gamer", "Returning random move " + theMove + ".");
			}
		}else{
			// If there is no time return a random move.
			//GamerLogger.log("Gamer", "No time to start the search during metagame.");
			theMove = this.thePropnetMachine.getRandomMove(this.getCurrentState(), this.getRole());
			GamerLogger.log("Gamer", "No time to select next move using MCS. Returning random move " + theMove + ".");
		}

		thinkingTime = System.currentTimeMillis() - start;

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", this.gameStep + ";" + thinkingTime + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";" + theMove + ";" + moveScoreSum + ";" + moveVisits + ";" + moveAvgScore + ";");

		// TODO: IS THIS NEEDED? WHEN?
		notifyObservers(new GamerSelectedMoveEvent(this.thePropnetMachine.getExplicitLegalMoves(this.getCurrentState(), this.getRole()), theMove, thinkingTime));

		return theMove;
	}

	@Override
	public String getName() {
		/*String type = "";
		if(this.singleGame){
			type = "SingleGame";
		}else{
			type = "Starndard";
		}
		return getClass().getSimpleName() + "-" + type;*/

		return getClass().getSimpleName();
	}

}

/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCS.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.PlayoutStrategy;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.implementation.internalPropnet.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMove;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetRole;

/**
 * @author C.Sironi
 *
 */
public class InternalPropnetMCSManager {

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;

	static{

		LOGGER = LogManager.getRootLogger();

	}

	/**
	 * The game state currently being searched.
	 */
	private InternalPropnetMachineState currentState;

	/**
	 * The statistics for all the legal moves for myRole in the state currently being searched.
	 */
	private MCSMove[] currentMovesStatistics;

	/**
	 * The strategy that this MCS manager must use to perform playouts.
	 */
	private PlayoutStrategy playoutStrategy;

	/**
	 * The state machine that this MCTS manager uses to reason on the game
	 */
	private InternalPropnetStateMachine theMachine;

	/**
	 * The role performing the search.
	 */
	private InternalPropnetRole myRole;

	/**
	 * Maximum depth that the MCTS algorithm must visit.
	 */
	private int maxSearchDepth;

	/**
	 *
	 */
	private Random random;

	/**
	 * Number of performed iterations.
	 */
	private int iterations;

	/**
	 * Number of all visited states since the start of the search.
	 */
	private int visitedNodes;

	/**
	 * Start time of last performed search.
	 */
	private long searchStart;

	/**
	 * End time of last performed search.
	 */
	private long searchEnd;

	/**
	 *
	 */
	public InternalPropnetMCSManager(PlayoutStrategy playoutStrategy, InternalPropnetStateMachine theMachine, InternalPropnetRole myRole, int maxSearchDepth, Random random) {

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
	}

	public MCSMove getBestMove() throws MCSException{

		if(this.currentMovesStatistics!=null){
			List<Integer> chosenMovesIndices = new ArrayList<Integer>();

			double maxAvgScore = -1;
			double currentAvgScore;

			// For each legal move check the average score
			for(int i = 0; i < this.currentMovesStatistics.length; i++){

				long visits =  this.currentMovesStatistics[i].getVisits();

				//System.out.println("Visits: " + visits);

				long scoreSum = this.currentMovesStatistics[i].getScoreSum();

				//System.out.println("Score sum: " + scoreSum);

				if(visits == 0){
					// Default score for unvisited moves
					currentAvgScore = -1;

					//System.out.println("Default move average score: " + currentAvgScore);

				}else{
					// Compute average score
					currentAvgScore = ((double) scoreSum) / ((double) visits);

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


	public void search(InternalPropnetMachineState state, long timeout) throws MCSException{

		// Reset so that if the search fails we'll have a duration of 0ms for it
		// instead of the duration of the previous search.
		this.searchStart = 0L;
		this.searchEnd = 0L;

		this.iterations = 0;
		this.visitedNodes = 0;

		// If the state is different from the last searched state,
		// remove the old state and create new move statistics.
		if(!(state.equals(this.currentState))){

			this.currentState = state;

			List<InternalPropnetMove> legalMoves;
			try {
				legalMoves = this.theMachine.getInternalLegalMoves(this.currentState, this.myRole);
			} catch (MoveDefinitionException e) {
				LOGGER.error("[MCSManager] Error when computing legal moves for my role in the root state before starting Monte Carlo search.", e);
				throw new MCSException("Impossible to perform search: legal moves cannot be computed and explored in the given state.", e);
			}

			this.currentMovesStatistics = new MCSMove[legalMoves.size()];

			for(int i = 0; i < this.currentMovesStatistics.length; i++){
				this.currentMovesStatistics[i] = new MCSMove(legalMoves.get(i));
			}

		} // Otherwise proceed with the search using the old statistics and updating them.

		InternalPropnetMove myCurrentMove;
		List<InternalPropnetMove> jointMove;
		InternalPropnetMachineState nextState;
		int[] goals;
		int[] playoutVisitedNodes = new int[1];
		int myGoal;

		this.searchStart = System.currentTimeMillis();
		// Analyze every move, iterating until the timeout is reached.
		for (int i = 0; true; i = (i+1) % this.currentMovesStatistics.length) {
		    if (System.currentTimeMillis() >= timeout)
		        break;

		    this.iterations++;

		    // Get the move.
		    myCurrentMove = this.currentMovesStatistics[i].getTheMove();

		    // Always increment at least once. Even if the playout fails we consider one node to have been
		    // visited because we update the statistics of the current move with a 0 goal.
		    this.visitedNodes++;
		    try {

		    	// Get a random joint move where my role plays its currently analyzed move.
				jointMove = this.theMachine.getRandomJointMove(this.currentState, this.myRole, myCurrentMove);
				// Get the state reachable with this joint move.
				nextState =  this.theMachine.getInternalNextState(this.currentState, jointMove);
				// Get the goals obtained by performing playouts from this state.
				goals = this.playoutStrategy.playout(nextState, playoutVisitedNodes, this.maxSearchDepth-1);
				this.visitedNodes += playoutVisitedNodes[0];
				myGoal = goals[this.myRole.getIndex()];

			} catch (StateMachineException | MoveDefinitionException e) {

				LOGGER.error("[MCSManager] Failed retrieving random joint move for the currently analyzed move of my role during Monte Carlo Search.", e);

				myGoal = 0;

			}

		    this.currentMovesStatistics[i].incrementVisits();
		    this.currentMovesStatistics[i].incrementScoreSum(myGoal);
		}

		this.searchEnd = System.currentTimeMillis();
	}

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
	}

}

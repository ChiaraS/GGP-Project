/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.BackpropagationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.ExpansionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MoveChoiceStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.SelectionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTTranspositionTable;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

/**
 * @author C.Sironi
 *
 */
public class InternalPropnetMCTSManager extends MCTSManager {

	/**
	 * Strategies that the MCTSManger must use to perform the different MCTS phases.
	 */
	private SelectionStrategy selectionStrategy;

	private ExpansionStrategy expansionStrategy;

	private PlayoutStrategy playoutStrategy;

	private BackpropagationStrategy backpropagationStrategy;

	private MoveChoiceStrategy moveChoiceStrategy;

	/**
	 * The state machine that this MCTS manager uses to reason on the game
	 */
	private InternalPropnetStateMachine theMachine;

	/**
	 * The role of the playing gamer.
	 */
	private InternalPropnetRole myRole;

	/**
	 * Maximum depth that the MCTS algorithm must visit.
	 */
	private int maxSearchDepth;

	/**
	 * The transposition table (implemented with HashMap that uses the internal propnet state as key
	 * and solves collisions with linked lists).
	 */
	private DUCTTranspositionTable transpositionTable;

	/**
	 * Number of performed iterations.
	 */
	private int iterations;

	/**
	 * Number of all visited states since the start of the search.
	 */
	private int visitedNodes;

	/**
	 * Number of visited nodes in the current iteration so far.
	 */
	private int currentIterationVisitedNodes;

	/**
	 *
	 */
	public InternalPropnetMCTSManager(SelectionStrategy selectionStrategy, ExpansionStrategy expansionStrategy,
			PlayoutStrategy playoutStrategy, BackpropagationStrategy backpropagationStrategy,
			MoveChoiceStrategy moveChoiceStrategy, InternalPropnetStateMachine theMachine,
			InternalPropnetRole myRole, int gameStepOffset, int maxSearchDepth) {

		this.selectionStrategy = selectionStrategy;
		this.expansionStrategy = expansionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.backpropagationStrategy = backpropagationStrategy;
		this.moveChoiceStrategy = moveChoiceStrategy;
		this.theMachine = theMachine;
		this.myRole = myRole;
		this.transpositionTable = new DUCTTranspositionTable(gameStepOffset);
		this.maxSearchDepth = maxSearchDepth;
	}

	public DUCTMove getBestMove(InternalPropnetMachineState initialState, long timeout, int gameStep) throws MoveDefinitionException{



		return this.moveChoiceStrategy.chooseBestMove(initialNode, this.myRole);
	}

	public void search(InternalPropnetMachineState initialState, long timeout, int gameStep)  throws MoveDefinitionException{

		this.iterations = 0;
		this.visitedNodes = 0;

		this.transpositionTable.clean(gameStep);

		// If it's the first time during the game that we call this method the transposition table is empty
		// so we create the first node, otherwise we check if the node is already in the tree.

		InternalPropnetDUCTMCTreeNode initialNode = this.transpositionTable.getNode(initialState);

		if(initialNode == null){
			initialNode = this.createNewNode(initialState);
			this.transpositionTable.putNode(initialState, initialNode);
		}

		while(System.currentTimeMillis() < timeout){
			this.currentIterationVisitedNodes = 0;
			this.searchNext(initialState, initialNode);
			this.iterations++;
			this.visitedNodes += this.currentIterationVisitedNodes;

			//System.out.println("Iteration: " + this.iterations);
		}

	}

	private int[] searchNext(InternalPropnetMachineState currentState, InternalPropnetDUCTMCTreeNode currentNode) {

		int[] goals;

		// Check if the state is terminal, and if so, return the final goals (saved in the node) for all players.
		if(this.theMachine.isTerminal(currentState)){

			//System.out.println("Reached terminal state.");

			goals = currentNode.getGoals();
			// If a state in the tree is terminal, it must record the goals for every player.
			// If it doesn't there must be a programming error.
			if(goals == null){
				GamerLogger.logError("MCTSManager", "Detected null goals for a node in the tree corresponding to a terminal state.");
				throw new RuntimeException("Detected null goals for a node in the tree corresponding to a terminal state.");
			}
			return goals;
		}

		// If the state is not terminal, it can be visited (i.e. one of its moves explored) only if the depth limit has not been reached.
		if(this.currentIterationVisitedNodes >= this.maxSearchDepth){

			// The state is not terminal, but we have reached the depth limit.
			// So we must return some goals. Try to return the goals of the non-terminal state
			// and if they cannot be computed return default tie-goals.

			// Try to get the goals of the state.
			try{
				goals = this.theMachine.getGoals(currentState);
			}catch(GoalDefinitionException e){

				GamerLogger.logError("MCTSManager", "Search interrupted (in the Monte Carlo tree) before reaching a treminal state. Impossible to compute real goals. Returning default tie goals.");
				GamerLogger.logStackTrace("MCTSManager", e);
				goals = this.getTieGoals();

			}

			return goals;
		}

		this.currentIterationVisitedNodes++;

		//System.out.println("Node: " + this.currentIterationVisitedNodes);

		DUCTJointMove ductJointMove;
		InternalPropnetMachineState nextState;
		InternalPropnetDUCTMCTreeNode nextNode;

		// If the state is not terminal we must check if we have to expand it or if we have to continue the selection.
		// Depending on what needs to be done, get the joint move to be expanded/selected.
		boolean expansionRequired = this.expansionStrategy.expansionRequired(currentNode);
		if(expansionRequired){

			//System.out.println("Expanding.");

			ductJointMove = this.expansionStrategy.expand(currentNode);
		}else{

			//System.out.println("Selecting.");

			ductJointMove = this.selectionStrategy.select(currentNode);
		}

		// Get the next state according to the joint move...
		nextState = this.theMachine.getInternalNextState(currentState, ductJointMove.getJointMove());
		// ...and get the corresponding MCT node from the transposition table.
		nextNode = this.transpositionTable.getNode(nextState);

		// If we cannot find such tree node we create it and add it to the table.
		// NOTE: there are 3 situations when the next node might not be in the tree yet:
		// 1. If we are expanding the current node, the chosen joint move will probably
		// lead to an unexplored state (depends on the choice the expansion strategy makes
		// and on the fact that the state might have been visited already from a different
		// sequence of actions).
		// 2. If the expansion doesn't look at unexplored joint moves to choose the joint
		// move to expand, but only at unexplored single moves for each player, it might
		// be that all single moves for each player have been explored already, but the
		// selection picks a combination of them that has not been explored yet and the
		// corresponding next state hasn't thus been added to the tree yet.
		// 3. It might also be the case that the selection chooses a joint move whose
		// corresponding state has been already visited in a previous run of the MCTS,
		// but since the corresponding MCT node hasn't been visited in recent runs anymore
		// it has been removed from the transposition table during the "cleaning" process.
		if(nextNode == null){

			//System.out.println("Adding new node to table: " + nextState);

			nextNode = this.createNewNode(nextState);
			this.transpositionTable.putNode(nextState, nextNode);
		}

		// Now that we have the MCT node, if we are expanding:
		if(expansionRequired){
			// No need to perform playout if the node is terminal, we just return the goals in the node.
			// Otherwise we perform the playout.
			if(this.theMachine.isTerminal(nextState)){

				//System.out.println("Expanded state is terminal.");

				goals = nextNode.getGoals();
			}else{
				// Check how many nodes can be visited after the current one. At this point
				// "currentIterationVisitedNodes" can be at most equal to the "maxSearchDepth".
				int availableDepth = this.maxSearchDepth - this.currentIterationVisitedNodes;

				int[] playoutVisitedNodes = new int[1];
				// Note that if no depth is left for the playout, the playout itself will take care of
				// returning the added-state goal values (if any) or the default tie goal values.
				goals = this.playoutStrategy.playout(nextState, this.myRole, playoutVisitedNodes, availableDepth);
				this.currentIterationVisitedNodes += playoutVisitedNodes[0];
				//System.out.println("Node: " + this.visitedNodes);
			}
		}else{
			// Otherwise, if we are selecting:
			goals = this.searchNext(nextState, nextNode);
		}

		this.backpropagationStrategy.update(currentNode, ductJointMove, goals);
		return goals;
	}

	private !!!!/*FIX so that it doesn't throw exceptions!*/InternalPropnetDUCTMCTreeNode createNewNode(InternalPropnetMachineState state) /*throws MoveDefinitionException*/{
		if(this.theMachine.isTerminal(state)){

			int goals[];
			try {
				goals = this.theMachine.getGoals(state);
			} catch (GoalDefinitionException e) {
				GamerLogger.logError("MCTSManager", "Failed to retrieve the goals while adding a terminal state to the tree.");
				GamerLogger.logStackTrace("MCTSManager", e);
				goals = this.getLossGoals();
			}

			return new InternalPropnetDUCTMCTreeNode(null, goals);
		}else{

			InternalPropnetRole[] roles = this.theMachine.getInternalRoles();
			DUCTMove[][] moves = new DUCTMove[roles.length][];

			for(int i = 0; i < roles.length; i++){

				List<InternalPropnetMove> legalMoves = this.theMachine.getInternalLegalMoves(state, roles[i]);

				moves[i] = new DUCTMove[legalMoves.size()];
				for(int j = 0; j < legalMoves.size(); j++){
					moves[i][j] = new DUCTMove(legalMoves.get(j));
				}
			}

			return new InternalPropnetDUCTMCTreeNode(moves, null);
		}
	}

	public int getIterations(){
		return this.iterations;
	}

	public int getVisitedNodes(){
		return this.visitedNodes;
	}

	// TODO: remove these duplicate methods
	private int[] getLossGoals(){
		int[] goals;
		int numRoles = this.theMachine.getInternalRoles().length;
		goals = new int[numRoles];
		if(numRoles > 1){
			for(int i = 0; i < goals.length; i++){
				// Attention! Since this rounds the goals to the next integer, it might make a zero-sum game loose
				// the property of being zero-sum. However, this doesn't influence our MCTS implementation.
				goals[i] = (int) Math.round(1.0 / ((double)numRoles-1.0));
			}
		}
		goals[this.myRole.getIndex()] = 0;

		return goals;
	}

	private int[] getTieGoals(){
		int[] goals;
		int numRoles = this.theMachine.getInternalRoles().length;
		goals = new int[numRoles];
		for(int i = 0; i < goals.length; i++){
			// Attention! Since this rounds the goals to the next integer, it might make a zero-sum game loose
			// the property of being zero-sum. However, this doesn't influence our MCTS implementation.
			goals[i] = (int) Math.round(1.0 / ((double)numRoles));
		}

		return goals;
	}

}

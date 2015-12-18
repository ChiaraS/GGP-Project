/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.BackpropagationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.ExpansionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MoveChoiceStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.SelectionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
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
	private Map<InternalPropnetMachineState,InternalPropnetDUCTMCTreeNode> transpositionTable;

	/**
	 * Number of performed iterations.
	 */
	private int iterations;

	/**
	 * Number of visited states.
	 */
	private int visitedNodes;

	/**
	 *
	 */
	public InternalPropnetMCTSManager(SelectionStrategy selectionStrategy, ExpansionStrategy expansionStrategy,
			PlayoutStrategy playoutStrategy, BackpropagationStrategy backpropagationStrategy,
			MoveChoiceStrategy moveChoiceStrategy, InternalPropnetStateMachine theMachine, InternalPropnetRole myRole) {

		this.selectionStrategy = selectionStrategy;
		this.expansionStrategy = expansionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.backpropagationStrategy = backpropagationStrategy;
		this.moveChoiceStrategy = moveChoiceStrategy;
		this.theMachine = theMachine;
		this.myRole = myRole;
		this.transpositionTable = new HashMap<InternalPropnetMachineState,InternalPropnetDUCTMCTreeNode>();
	}

	public InternalPropnetMove getBestMove(InternalPropnetMachineState initialState, long timeout) throws MoveDefinitionException{

		this.iterations = 0;
		this.visitedNodes = 0;

		// If it's the first time during the game that we call this method the transposition table is empty
		// so we create the first node, otherwise we check if the node is already in the tree.

		InternalPropnetDUCTMCTreeNode initialNode = this.transpositionTable.get(initialState);

		if(initialNode == null){
			initialNode = this.createNewNode(initialState);
			this.transpositionTable.put(initialState, initialNode);
		}

		while(System.currentTimeMillis() < timeout){
			this.search(initialState, initialNode);
			this.iterations++;

			//System.out.println("Iteration: " + this.iterations);
		}

		return this.moveChoiceStrategy.chooseBestMove(initialNode, this.myRole);
	}

	private int[] search(InternalPropnetMachineState currentState, InternalPropnetDUCTMCTreeNode currentNode) throws MoveDefinitionException{

		// Check if the state is terminal, and if so, return the final goals (saved in the node) for all players.
		if(this.theMachine.isTerminal(currentState)){

			//System.out.println("Reached terminal state.");

			int[] goals = currentNode.getGoals();
			// If a state in the tree is terminal, it must record the goals for every player.
			// If it doesn't there must be a programming error.
			if(goals == null){
				GamerLogger.logError("MCTSManager", "Detected null goals for a node in the tree corresponding to a terminal state.");
				throw new RuntimeException("Detected null goals for a node in the tree corresponding to a terminal state.");
			}
			return goals;
		}

		// If the state is not terminal, it can be visited (i.e. one of its actions explored) only if the depth limit has not been reached.

		if(this.visitedNodes >= this.maxSearchDepth){
			return this.getTieGoals();
		}

		this.visitedNodes++;

		//System.out.println("Node: " + this.visitedNodes);

		// If the state is not terminal we must check if we have to expand it or if we have to continue the selection.
		if(this.expansionStrategy.expansionRequired(currentNode)){
			DUCTJointMove ductJointMove = this.expansionStrategy.expand(currentNode);

			//System.out.println("Expanding move: " + jointMove);

			InternalPropnetMachineState stateToAdd = this.theMachine.getInternalNextState(currentState, ductJointMove.getJointMove());
			InternalPropnetDUCTMCTreeNode nodeToAdd = this.transpositionTable.get(stateToAdd);
			if(nodeToAdd == null){
				nodeToAdd = this.createNewNode(stateToAdd);
				this.transpositionTable.put(stateToAdd, nodeToAdd);
			}
			// No need to perform playout if the node is terminal, we just return the goals in the node.
			// Otherwise we perform the playout.
			int[] goals;
			if(this.theMachine.isTerminal(stateToAdd)){

				System.out.println("Expanded state is terminal.");

				goals = nodeToAdd.getGoals();
			}else{
				// Check how many nodes can be visited after the current one. At this point "visitedNodes" can be at most equal
				// to the "maxSearchDepth".
				int availableDepth = this.maxSearchDepth - this.visitedNodes;

				int[] playoutVisitedNodes = new int[1];
				// Note that if no depth is left for the playout, the playout itself will take care of returning the added
				// state goal values (if any) or the default tie goal values.
				goals = this.playoutStrategy.playout(stateToAdd, this.myRole, playoutVisitedNodes, availableDepth);
				this.visitedNodes += playoutVisitedNodes[0];
				//System.out.println("Node: " + this.visitedNodes);
			}
			this.backpropagationStrategy.update(currentNode, ductJointMove, goals);
			return goals;
		}

		DUCTJointMove ductJointMove = this.selectionStrategy.select(currentNode);

		//System.out.println("Selected move: " + jointMove);

		InternalPropnetMachineState selectedState = this.theMachine.getInternalNextState(currentState, ductJointMove.getJointMove());
		InternalPropnetDUCTMCTreeNode selectedNode = this.transpositionTable.get(selectedState);

		// Get the node corresponding to the selected state. If we cannot find such tree node there must be
		// something wrong in the implementation. The table records with no errors all the nodes corresponding
		// to states already added to the tree. Since this method examines states already in the tree the
		// corresponding tree node must be non-null.
		if(selectedNode == null){
			GamerLogger.logError("MCTSManager", "MCTS trying to perform selection on a state not yet in the tree.");
			throw new RuntimeException("MCTS trying to perform selection on a state not yet in the tree.");
		}

		int[] goals = this.search(selectedState, selectedNode);
		this.backpropagationStrategy.update(currentNode, ductJointMove, goals);
		return goals;
	}

	private InternalPropnetDUCTMCTreeNode createNewNode(InternalPropnetMachineState state) throws MoveDefinitionException{
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
			DUCTMove[][] actions = new DUCTMove[roles.length][];

			for(int i = 0; i < roles.length; i++){

				List<InternalPropnetMove> legalMoves = this.theMachine.getInternalLegalMoves(state, roles[i]);

				actions[i] = new DUCTMove[legalMoves.size()];
				for(int j = 0; j < legalMoves.size(); j++){
					actions[i][j] = new DUCTMove(legalMoves.get(j));
				}
			}

			return new InternalPropnetDUCTMCTreeNode(actions, null);
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

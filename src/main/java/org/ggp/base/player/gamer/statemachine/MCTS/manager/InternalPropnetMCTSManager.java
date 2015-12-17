/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.BackpropagationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.ExpansionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MoveChoiceStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.SelectionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTActionsStatistics;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
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

	public InternalPropnetMove getBestMove(InternalPropnetMachineState initialState, long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException{

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
		}

		return this.moveChoiceStrategy.chooseBestMove(initialNode, this.myRole);
	}

	private int[] search(InternalPropnetMachineState currentState, InternalPropnetDUCTMCTreeNode currentNode) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException{

		// Check if the state is terminal, and if so, return the final goals (saved in the node) for all players.
		if(this.theMachine.isTerminal(currentState)){
			int[] goals = currentNode.getGoals();
			// If a state in the tree is terminal, it must record the goals for every player.
			// If it doesn't there must be a programming error.
			if(goals == null){
				GamerLogger.log("MCTSManager", "Detected null goals for a node in the tree corresponding to a terminal state.");
				throw new RuntimeException("Detected null goals for a node in the tree corresponding to a terminal state.");
			}
			return goals;
		}

		this.visitedNodes++;

		// If the state is not terminal we must check if we have to expand it or if we have to continue the selection.
		if(this.expansionStrategy.expansionRequired(currentNode)){
			List<InternalPropnetMove> jointMove = this.expansionStrategy.expand(currentNode);
			InternalPropnetMachineState stateToAdd = this.theMachine.getInternalNextState(currentState, jointMove);
			InternalPropnetDUCTMCTreeNode nodeToAdd = this.transpositionTable.get(stateToAdd);
			if(nodeToAdd == null){
				nodeToAdd = this.createNewNode(stateToAdd);
				this.transpositionTable.put(stateToAdd, nodeToAdd);
			}
			// No need to perform playout if the node is terminal, we just return the goals in the node.
			// Otherwise we perform the playout.
			int[] goals;
			if(this.theMachine.isTerminal(stateToAdd)){
				goals = nodeToAdd.getGoals();
			}else{
				int[] playoutVisitedNodes = new int[1];
				goals = this.playoutStrategy.playout(stateToAdd, playoutVisitedNodes);
				this.visitedNodes += playoutVisitedNodes[0];
			}
			this.backpropagationStrategy.update(currentNode, jointMove, goals);
			return goals;
		}

		List<InternalPropnetMove> jointMove = this.selectionStrategy.select(currentNode);

		InternalPropnetMachineState selectedState = this.theMachine.getInternalNextState(currentState, jointMove);
		InternalPropnetDUCTMCTreeNode selectedNode = this.transpositionTable.get(selectedState);

		// Get the node corresponding to the selected state. If we cannot find such tree node there must be
		// something wrong in the implementation. The table records with no errors all the nodes corresponding
		// to states already added to the tree. Since this method examines states already in the tree the
		// corresponding tree node must be non-null.
		if(selectedNode == null){
			GamerLogger.log("MCTSManager", "MCTS trying to perform selection on a state not yet in the tree.");
			throw new RuntimeException("MCTS trying to perform selection on a state not yet in the tree.");
		}

		int[] goals = this.search(selectedState, selectedNode);
		this.backpropagationStrategy.update(currentNode, jointMove, goals);
		return goals;
	}

	private InternalPropnetDUCTMCTreeNode createNewNode(InternalPropnetMachineState state) throws MoveDefinitionException, GoalDefinitionException{
		if(this.theMachine.isTerminal(state)){
			return new InternalPropnetDUCTMCTreeNode(null, null, this.theMachine.getGoals(state));
		}else{
			InternalPropnetRole[] roles = this.theMachine.getInternalRoles();
			DUCTActionsStatistics[] actionsStatistics = new DUCTActionsStatistics[roles.length];
			List<List<InternalPropnetMove>> allJointMoves = new ArrayList<List<InternalPropnetMove>>();

			List<InternalPropnetMove> jointMove = new ArrayList<InternalPropnetMove>();

			allJointMoves.add(jointMove);

			for(int i = 0; i < roles.length; i++){

				List<List<InternalPropnetMove>> newJointMoves = new ArrayList<List<InternalPropnetMove>>();

				List<InternalPropnetMove> legalMoves = this.theMachine.getInternalLegalMoves(state, roles[i]);

				actionsStatistics[i] = new DUCTActionsStatistics(legalMoves);

				for(InternalPropnetMove m : legalMoves){
					for(List<InternalPropnetMove> partialJointMove : allJointMoves){
						// Copy the partial joint move
						List<InternalPropnetMove> extendedJointMove = new ArrayList<InternalPropnetMove>(partialJointMove);
						extendedJointMove.add(m);
						newJointMoves.add(extendedJointMove);
					}
				}
				allJointMoves = newJointMoves;
			}

			return new InternalPropnetDUCTMCTreeNode(allJointMoves, actionsStatistics, null);
		}
	}

	public int getIterations(){
		return this.iterations;
	}

	public int getVisitedNodes(){
		return this.visitedNodes;
	}

}

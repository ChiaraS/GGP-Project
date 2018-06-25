package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.statememorizingdecoupled.StateMemorizingMctsNode;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public abstract class PlayoutStrategy extends Strategy {

	public PlayoutStrategy(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	/**
	 * Performs a single playout from the given state to a terminal state (or until the depth limit is reached).
	 *
	 * @param node the node corresponding to the state. Its given as input because we might be able to find in the
	 * node some information about the state so we don't have to recompute it with the state machine.
	 * @param state state from which to start the playout.
	 * @param maxDepth depth limit.
	 * @return the result of the playout, with final (or also intermediate) goals and played moves (optional).
	 */
	public abstract SimulationResult singlePlayout(MctsNode node, MachineState state, int maxDepth);

	/**
	 * Performs one or multiple (depends on the class) playouts from the given state until either a terminal state
	 * or the depth limit is reached.
	 *
	 * @param jointMove the joint move that led to the state from which we want to start the playout.
	 * @param state state from which to start the playout(s).
	 * @param maxDepth depth limit.
	 * @return a list of results, one for each performed playout, with final (or also intermediate) goals and played
	 * moves (optional).
	 */
	public abstract SimulationResult[] playout(MctsNode node, List<Move> jointMove, MachineState state, int maxDepth);

	/**
	 * Returns a move for the given role in the given state, using the playout strategy implemented by the class.
	 *
	 * @param legalMoves
	 * @param state
	 * @param role
	 * @return
	 * @throws StateMachineException
	 * @throws MoveDefinitionException
	 */
	public abstract Move getMoveForRole(MctsNode node, MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException;

	/**
	 * Returns a joint move for the given state, using the playout strategy implemented by the class.
	 *
	 * @param node the node corresponding to the state. Its given as input because we might be able to find in the
	 * node some information about the state so we don't have to recompute it with the state machine.
	 * @param state
	 * @return
	 * @throws StateMachineException
	 * @throws MoveDefinitionException
	 */
	protected List<Move> getJointMove(MctsNode node, MachineState state) throws MoveDefinitionException, StateMachineException {
		List<Move> jointMove = new ArrayList<Move>();

		// For each role we check if the move for the role must be picked randomly or according to the MAST statistics
		// NOTE that a joint move might be composed of moves that have been picked randomly for some roles and moves that
		// have been picked according to MAST statistics for other roles.
		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			jointMove.add(this.getMoveForRole(node, state, i));
		}

		//System.out.println(Arrays.toString(jointMove.toArray()));

		return jointMove;

		/*
		try {
			return this.moveSelector.getJointMove(node, state);
		} catch (MoveDefinitionException | StateMachineException e) {
			GamerLogger.logError("MctsManager", "Exception getting a joint move using the playout strategy.");
			GamerLogger.logStackTrace("MctsManager", e);
			throw new RuntimeException("Exception getting a joint move using the playout strategy.", e);
		}*/
	}

	protected MachineState getNextState(MctsNode node, MachineState state, List<Move> jointMove) throws TransitionDefinitionException, StateMachineException {
		if(node != null && node instanceof StateMemorizingMctsNode) {
			return ((StateMemorizingMctsNode)node).getNextState(jointMove);
		}else {
			return this.gameDependentParameters.getTheMachine().getNextState(state, jointMove);
		}
	}
}

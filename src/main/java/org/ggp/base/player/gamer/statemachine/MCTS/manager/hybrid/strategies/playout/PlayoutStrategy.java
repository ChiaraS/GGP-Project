package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public abstract class PlayoutStrategy extends Strategy {

	public PlayoutStrategy(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	/**
	 * Performs a single playout from the given state to a terminal state (or until the depth limit is reached).
	 *
	 * @param state state from which to start the playout.
	 * @param maxDepth depth limit.
	 * @return the result of the playout, with final (or also intermediate) goals and played moves (optional).
	 */
	public abstract SimulationResult singlePlayout(MachineState state, int maxDepth);

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
	public abstract SimulationResult[] playout(List<Move> jointMove, MachineState state, int maxDepth);

	/**
	 * Returns a joint move for the given state, using the playout strategy implemented by the class.
	 *
	 * @param state
	 * @return
	 */
	public abstract List<Move> getJointMove(List<List<Move>> legalMovesPerRole, MachineState state);


	/**
	 * Returns a move for the given role in the given state, using the playout strategy implemented by the class.
	 *
	 * @param legalMoves
	 * @param state
	 * @param role
	 * @return
	 */
	public abstract Move getMoveForRole(List<Move> legalMoves, MachineState state, Role role);

}

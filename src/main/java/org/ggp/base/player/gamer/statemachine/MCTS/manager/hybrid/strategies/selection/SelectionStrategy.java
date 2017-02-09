package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsMove;
import org.ggp.base.util.statemachine.structure.MachineState;

public abstract class SelectionStrategy extends Strategy {

	public SelectionStrategy(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	/**
	 * This method selects the next joint move to visit in the given tree node.
	 * Note that the method assumes that the given state is a non-terminal
	 * state and for each player there is at least one legal move in the state.
	 *
	 * @param currentNode the node for which to select an action to visit.
	 * @return the selected move.
	 */
	public abstract MctsJointMove select(MctsNode currentNode, MachineState state);

	/**
	 * This method selects the next move to visit in the given tree node
	 * for the given role.
	 * Note that the method assumes that the given state is a non-terminal
	 * state and for the player there is at least one legal move in the state.
	 *
	 * @param currentNode the node for which to select an action to visit.
	 * @return the selected move.
	 */
	public abstract MctsMove selectPerRole(MctsNode currentNode, MachineState state, int roleIndex);

	/**
	 * If the selection strategy needs to perform some actions for each node that's being visited
	 * even if the strategy itself is not used for selecting the action in the node, this is the
	 * method where it must do that.
	 *
	 * This method is needed mainly for the PlayoutSupportedSelection, because there are some selection
	 * strategies that need to perform some actions for the node even when the PlayoutSupportedSelection
	 * chooses to use the playout strategy to select a move. (An example is the GRAVE strategy that needs
	 * to update the reference to the closest AMAF statistics at every node even if the GRAVE strategy is
	 * not used for that node).
	 *
	 * @param currentNode
	 */
	public abstract void preSelectionActions(MctsNode currentNode);

}

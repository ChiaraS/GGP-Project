package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;

/**
 * Attention!: for the decoupled version of MCTS the choice of expanding a node and of the action to use
 * for doing so can be made directly by the selection strategy. This class exists to be used together with
 * the decoupled TreeNodeFactory to nullify the effects of the expansion strategy and always use only the
 * selection strategy to decide which move to investigate next (either already visited or not).
 * The expansion strategy classes cannot disappear from the code yet, because of the existence of the sequential
 * MCTS. Using the selection to choose if to expand a node or just explore an already explored action in the
 * sequential MCTS requires additional changes of the code not performed yet (TODO!).
 *
 *
 * @author C.Sironi
 *
 */
public class NoExpansion extends ExpansionStrategy {

	public NoExpansion(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

	@Override
	public boolean expansionRequired(MctsNode node) {
		return false;
	}

	@Override
	public MctsJointMove expand(MctsNode node) {
		return null;
	}
}

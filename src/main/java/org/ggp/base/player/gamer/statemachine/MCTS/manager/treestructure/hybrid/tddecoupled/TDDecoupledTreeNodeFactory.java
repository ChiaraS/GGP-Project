package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;

public class TDDecoupledTreeNodeFactory extends DecoupledTreeNodeFactory {

	public TDDecoupledTreeNodeFactory(AbstractStateMachine theMachine) {
		super(theMachine);
	}

	@Override
	protected MCTSNode createActualNewNode(DecoupledMCTSMoveStats[][] ductMovesStats, int[] goals, boolean terminal) {
		return new TDDecoupledMCTSNode(ductMovesStats, goals, terminal);
	}

}

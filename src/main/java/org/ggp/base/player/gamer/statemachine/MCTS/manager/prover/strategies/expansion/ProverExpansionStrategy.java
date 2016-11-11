package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverMCTSJointMove;

public interface ProverExpansionStrategy extends PnStrategy{

	public boolean expansionRequired(MCTSNode node);

	public ProverMCTSJointMove expand(MCTSNode node);

}

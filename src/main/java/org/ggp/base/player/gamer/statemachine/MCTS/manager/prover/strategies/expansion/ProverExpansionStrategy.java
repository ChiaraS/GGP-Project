package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverMCTSJointMove;

public interface ProverExpansionStrategy extends Strategy{

	public boolean expansionRequired(MCTSNode node);

	public ProverMCTSJointMove expand(MCTSNode node);

}

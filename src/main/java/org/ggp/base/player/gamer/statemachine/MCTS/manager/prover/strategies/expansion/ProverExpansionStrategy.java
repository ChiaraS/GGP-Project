package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverMCTSJointMove;

public interface ProverExpansionStrategy extends PnStrategy{

	public boolean expansionRequired(MctsNode node);

	public ProverMCTSJointMove expand(MctsNode node);

}

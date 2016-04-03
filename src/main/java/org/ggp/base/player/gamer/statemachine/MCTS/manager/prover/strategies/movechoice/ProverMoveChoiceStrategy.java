package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCS.manager.prover.ProverCompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;

public interface ProverMoveChoiceStrategy extends Strategy {

	public ProverCompleteMoveStats chooseBestMove(MCTSNode initialNode);

}

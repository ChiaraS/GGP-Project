package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCS.manager.prover.ProverCompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;

public interface ProverMoveChoiceStrategy extends PnStrategy {

	public ProverCompleteMoveStats chooseBestMove(MctsNode initialNode);

}

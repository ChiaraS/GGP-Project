package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;

public abstract class MoveChoiceStrategy extends Strategy {

	public MoveChoiceStrategy(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters);
	}

	public abstract CompleteMoveStats chooseBestMove(MCTSNode initialNode);

}

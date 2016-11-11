package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.MoveEvaluator;

public class UCTSelection extends MoveValueSelection {

	public UCTSelection(GameDependentParameters gameDependentParameters, Random random,
			double valueOffset, MoveEvaluator moveEvaluator) {

		super(gameDependentParameters, random, valueOffset, moveEvaluator);

	}

}

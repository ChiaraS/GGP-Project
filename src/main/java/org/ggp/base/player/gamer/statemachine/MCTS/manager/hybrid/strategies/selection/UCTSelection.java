package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.MoveEvaluator;

public class UCTSelection extends MoveValueSelection {

	public UCTSelection(int numRoles, int myRoleIndex, Random random,
			double valueOffset, MoveEvaluator moveEvaluator) {

		super(numRoles, myRoleIndex, random, valueOffset, moveEvaluator);

	}

}

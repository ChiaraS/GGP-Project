package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class UCTSelection extends MoveValueSelection {

	public UCTSelection(int numRoles, InternalPropnetRole myRole,
			Random random, double valueOffset, double c) {
		super(numRoles, myRole, random, valueOffset, new UCTEvaluator(c));

	}

}

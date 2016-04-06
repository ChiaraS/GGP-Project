package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.MoveEvaluator;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class UCTSelection extends MoveValueSelection {

	public UCTSelection(int numRoles, InternalPropnetRole myRole,
			Random random, double valueOffset, MoveEvaluator moveEvaluator) {

		super(numRoles, myRole, random, valueOffset, moveEvaluator);

	}

}

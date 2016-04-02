package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class UCTSelection extends MoveValueSelection {

	public UCTSelection(int numRoles, InternalPropnetRole myRole,
			Random random, double valueOffset, double c, double defaultValue) {
		super(numRoles, myRole, random, valueOffset, new UCTEvaluator(c, defaultValue));

	}

}

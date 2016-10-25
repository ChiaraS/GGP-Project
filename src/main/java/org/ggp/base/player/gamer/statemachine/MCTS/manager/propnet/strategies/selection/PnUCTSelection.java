package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.PnMoveEvaluator;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class PnUCTSelection extends PnMoveValueSelection {

	public PnUCTSelection(int numRoles, InternalPropnetRole myRole,
			Random random, double valueOffset, PnMoveEvaluator moveEvaluator) {

		super(numRoles, myRole, random, valueOffset, moveEvaluator);

	}

}

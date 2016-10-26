package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.ProverMoveEvaluator;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public class ProverUCTSelection extends ProverMoveValueSelection {

	public ProverUCTSelection(int numRoles, ExplicitRole myRole,
			Random random, double valueOffset, ProverMoveEvaluator moveEvaluator) {
		super(numRoles, myRole, random, valueOffset, moveEvaluator);

	}

}

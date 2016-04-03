package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.ProverUCTEvaluator;
import org.ggp.base.util.statemachine.Role;

public class ProverUCTSelection extends ProverMoveValueSelection {

	public ProverUCTSelection(int numRoles, Role myRole,
			Random random, double valueOffset, double c, double defaultValue) {
		super(numRoles, myRole, random, valueOffset, new ProverUCTEvaluator(c, defaultValue));

	}

}

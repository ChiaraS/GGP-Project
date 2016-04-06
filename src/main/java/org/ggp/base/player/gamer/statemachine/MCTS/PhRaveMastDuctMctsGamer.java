package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.GRAVEBetaComputer;

public class PhRaveMastDuctMctsGamer extends PhGRMastDuctMctsGamer {

	public PhRaveMastDuctMctsGamer() {

		super();

		this.betaComputer = new GRAVEBetaComputer(0.001);
	}

}

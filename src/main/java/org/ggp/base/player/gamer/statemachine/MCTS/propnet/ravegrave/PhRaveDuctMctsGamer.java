package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnGRAVEBetaComputer;

public class PhRaveDuctMctsGamer extends PhGRDuctMctsGamer {

	public PhRaveDuctMctsGamer() {

		super();

		this.betaComputer = new PnGRAVEBetaComputer(0.00001);

	}

}

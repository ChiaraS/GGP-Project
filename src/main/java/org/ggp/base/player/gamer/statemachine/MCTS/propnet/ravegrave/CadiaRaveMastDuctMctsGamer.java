package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnCADIABetaComputer;

public class CadiaRaveMastDuctMctsGamer extends GRMastDuctMctsGamer {

	public CadiaRaveMastDuctMctsGamer() {

		super();

		this.betaComputer = new PnCADIABetaComputer(250);

	}

}

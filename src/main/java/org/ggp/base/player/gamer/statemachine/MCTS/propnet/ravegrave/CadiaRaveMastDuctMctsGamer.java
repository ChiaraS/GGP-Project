package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.CADIABetaComputer;

public class CadiaRaveMastDuctMctsGamer extends GRMastDuctMctsGamer {

	public CadiaRaveMastDuctMctsGamer() {

		super();

		this.betaComputer = new CADIABetaComputer(250);

	}

}

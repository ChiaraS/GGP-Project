package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.CADIABetaComputer;

public class CadiaRaveDuctMctsGamer extends GRDuctMctsGamer {

	public CadiaRaveDuctMctsGamer() {

		super();

		this.betaComputer = new CADIABetaComputer(250);

	}

}

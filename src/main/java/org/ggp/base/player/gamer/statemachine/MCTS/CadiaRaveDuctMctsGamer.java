package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.CADIABetaComputer;

public class CadiaRaveDuctMctsGamer extends GRDuctMctsGamer {

	public CadiaRaveDuctMctsGamer() {
		super();

		this.betaComputer = new CADIABetaComputer(500);


	}

}

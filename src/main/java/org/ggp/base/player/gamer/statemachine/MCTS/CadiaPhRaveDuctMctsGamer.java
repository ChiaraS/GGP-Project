package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.CADIABetaComputer;

public class CadiaPhRaveDuctMctsGamer extends PhGRDuctMctsGamer {

	public CadiaPhRaveDuctMctsGamer() {

		super();

		this.betaComputer = new CADIABetaComputer(250);

	}

}

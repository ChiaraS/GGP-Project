package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.CADIABetaComputer;

public class CadiaPhRaveDuctMctsGamer extends PhGRDuctMctsGamer {

	public CadiaPhRaveDuctMctsGamer() {

		super();

		this.betaComputer = new CADIABetaComputer(250);

	}

}

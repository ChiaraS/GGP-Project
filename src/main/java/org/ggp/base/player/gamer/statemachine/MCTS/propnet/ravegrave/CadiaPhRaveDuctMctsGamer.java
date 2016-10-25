package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnCADIABetaComputer;

public class CadiaPhRaveDuctMctsGamer extends PhGRDuctMctsGamer {

	public CadiaPhRaveDuctMctsGamer() {

		super();

		this.betaComputer = new PnCADIABetaComputer(250);

	}

}

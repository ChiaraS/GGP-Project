package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnCADIABetaComputer;


public class CadiaHraveDuctMctsGamer extends CadiaRaveDuctMctsGamer {

	public CadiaHraveDuctMctsGamer() {

		super();

		this.betaComputer = new PnCADIABetaComputer(50);

		this.minAMAFVisits = Integer.MAX_VALUE;

	}

}

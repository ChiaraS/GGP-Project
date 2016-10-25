package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnCADIABetaComputer;

public class CadiaRefTunerHraveDuctMctsGamer extends CadiaRefTunerGraveDuctMctsGamer {

	public CadiaRefTunerHraveDuctMctsGamer() {

		super();

		this.betaComputer = new PnCADIABetaComputer(50);
	}

}
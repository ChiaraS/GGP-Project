package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.CADIABetaComputer;

public class CadiaRefTunerHraveDuctMctsGamer extends CadiaRefTunerGraveDuctMctsGamer {

	public CadiaRefTunerHraveDuctMctsGamer() {

		super();

		this.betaComputer = new CADIABetaComputer(50);
	}

}

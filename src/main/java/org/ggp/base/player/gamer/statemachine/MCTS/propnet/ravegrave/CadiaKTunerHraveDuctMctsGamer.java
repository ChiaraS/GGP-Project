package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.CADIABetaComputer;

public class CadiaKTunerHraveDuctMctsGamer extends CadiaKTunerRaveDuctMctsGamer {

	public CadiaKTunerHraveDuctMctsGamer() {
		super();

		this.betaComputer = new CADIABetaComputer(50);

		this.minAMAFVisits = Integer.MAX_VALUE;
	}

}

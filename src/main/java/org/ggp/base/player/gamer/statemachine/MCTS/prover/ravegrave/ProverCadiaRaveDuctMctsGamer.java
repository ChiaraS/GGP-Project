package org.ggp.base.player.gamer.statemachine.MCTS.prover.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.CADIABetaComputer;

public class ProverCadiaRaveDuctMctsGamer extends ProverGRDuctMctsGamer {

	public ProverCadiaRaveDuctMctsGamer() {
		super();

		this.betaComputer = new CADIABetaComputer(250);
	}

}

package org.ggp.base.player.gamer.statemachine.MCTS.prover.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnCADIABetaComputer;

public class ProverCadiaRaveDuctMctsGamer extends ProverGRDuctMctsGamer {

	public ProverCadiaRaveDuctMctsGamer() {
		super();

		this.betaComputer = new PnCADIABetaComputer(250);
	}

}

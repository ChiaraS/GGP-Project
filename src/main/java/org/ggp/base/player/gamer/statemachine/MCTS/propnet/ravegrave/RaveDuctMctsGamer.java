package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnGRAVEBetaComputer;

public class RaveDuctMctsGamer extends GRDuctMctsGamer{

	public RaveDuctMctsGamer() {

		super();

		this.betaComputer = new PnGRAVEBetaComputer(1);

	}

}

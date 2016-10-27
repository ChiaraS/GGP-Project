package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.GRAVEBetaComputer;

public class RaveDuctMctsGamer extends GRDuctMctsGamer{

	public RaveDuctMctsGamer() {

		super();

		this.betaComputer = new GRAVEBetaComputer(1);

	}

}

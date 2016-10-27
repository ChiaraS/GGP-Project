package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.GRAVEBetaComputer;

public class RaveMastDuctMctsGamer extends GRMastDuctMctsGamer{

	public RaveMastDuctMctsGamer() {

		super();

		this.betaComputer = new GRAVEBetaComputer(0.00001);

	}

}

package org.ggp.base.player.gamer.statemachine.MCTS.propnet.TDIntegration;

import org.ggp.base.player.gamer.statemachine.MCTS.propnet.UctMctsGamer;

public abstract class TDDuctMctsGamer extends UctMctsGamer {

	protected double qPlayout;

	protected double lambda;

	protected double gamma;

	public TDDuctMctsGamer() {

		this.qPlayout = 0.0;

		this.lambda = 0.8;

		this.gamma = 1.0;

	}

}

package org.ggp.base.player.gamer.statemachine.MCTS.propnet.TDIntegration;

import org.ggp.base.player.gamer.statemachine.MCTS.propnet.UctMctsGamer;

public abstract class TDDuctMctsGamer extends UctMctsGamer {

	protected double qPlayout;

	protected double lambda;

	protected double gamma;

	protected double defaultGlobalMinValue;

	protected double defaultGlobalMaxValue;

	public TDDuctMctsGamer() {

		this.c = 0.7;

		this.valueOffset = 0.01;

		this.qPlayout = 0.0;

		this.lambda = 0.8;

		this.gamma = 1.0;

		this.defaultGlobalMinValue = 0.0;

		this.defaultGlobalMaxValue = 100.0;


		this.hybridManager = true;

	}

}

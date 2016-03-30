package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.MemorizedStandardPlayout;

public class MASTAfterSimulation implements AfterSimulationStrategy {

	private MemorizedStandardPlayout mastPlayout;

	public MASTAfterSimulation(MemorizedStandardPlayout mastPlayout) {
		this.mastPlayout = mastPlayout;
	}

	@Override
	public void afterSimulationActions() {
		this.mastPlayout.clearLastMemorizedPlayout();

	}

	@Override
	public String getStrategyParameters() {
		return null;
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[AFTER_SIM_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[AFTER_SIM_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}



}
package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.MemorizedStandardPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;

public class MASTAfterSimulation implements AfterSimulationStrategy {

	private MemorizedStandardPlayout mastPlayout;

	public MASTAfterSimulation(MemorizedStandardPlayout mastPlayout) {
		this.mastPlayout = mastPlayout;
	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult) {
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

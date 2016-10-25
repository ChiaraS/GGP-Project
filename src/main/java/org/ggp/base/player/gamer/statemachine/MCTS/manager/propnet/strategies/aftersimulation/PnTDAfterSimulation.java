package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.PnTDBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;

public class PnTDAfterSimulation implements PnAfterSimulationStrategy {

	private PnTDBackpropagation backpropagation;

	public PnTDAfterSimulation(PnTDBackpropagation backpropagation) {

		this.backpropagation = backpropagation;
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

	@Override
	public void afterSimulationActions(PnSimulationResult simulationResult) {

		this.backpropagation.resetSimulationParameters();

	}

}

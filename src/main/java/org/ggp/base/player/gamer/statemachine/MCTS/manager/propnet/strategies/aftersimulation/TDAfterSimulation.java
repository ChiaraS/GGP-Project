package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.TDBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;

public class TDAfterSimulation implements AfterSimulationStrategy {

	private TDBackpropagation backpropagation;

	public TDAfterSimulation(TDBackpropagation backpropagation) {

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
	public void afterSimulationActions(SimulationResult simulationResult) {

		this.backpropagation.resetSimulationParameters();

	}

}

package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.TDBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class TDAfterSimulation extends AfterSimulationStrategy {

	private TDBackpropagation backpropagation;

	public TDAfterSimulation(GameDependentParameters gameDependentParameters, TDBackpropagation backpropagation) {

		super(gameDependentParameters);

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

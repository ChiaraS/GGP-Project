package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class EvoGRAVEAfterSimulation extends AfterSimulationStrategy {

	private GRAVEAfterSimulation graveAfterSimulation;

	private EvoAfterSimulation evoAfterSimulation;

	public EvoGRAVEAfterSimulation(GameDependentParameters gameDependentParameters, GRAVEAfterSimulation graveAfterSimulation, EvoAfterSimulation evoAfterSimulation) {

		super(gameDependentParameters);

		this.graveAfterSimulation = graveAfterSimulation;

		this.evoAfterSimulation = evoAfterSimulation;

	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult){

		this.graveAfterSimulation.afterSimulationActions(simulationResult);

		this.evoAfterSimulation.afterSimulationActions(simulationResult);

	}

	@Override
	public String getStrategyParameters() {
		return "(SUB_AFTER_SIM_STRATEGY = " + this.graveAfterSimulation.printStrategy() + ", SUB_AFTER_SIM_STRATEGY = " + this.evoAfterSimulation.printStrategy() + ")";
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

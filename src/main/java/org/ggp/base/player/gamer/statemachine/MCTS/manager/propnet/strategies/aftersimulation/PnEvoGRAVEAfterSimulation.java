package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;


public class PnEvoGRAVEAfterSimulation implements PnAfterSimulationStrategy{

	private PnGRAVEAfterSimulation graveAfterSimulation;

	private PnEvoAfterSimulation evoAfterSimulation;

	public PnEvoGRAVEAfterSimulation(PnGRAVEAfterSimulation graveAfterSimulation, PnEvoAfterSimulation evoAfterSimulation) {

		this.graveAfterSimulation = graveAfterSimulation;

		this.evoAfterSimulation = evoAfterSimulation;

	}

	@Override
	public void afterSimulationActions(PnSimulationResult simulationResult){

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

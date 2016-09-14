package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;


public class EvoGRAVEAfterSimulation implements AfterSimulationStrategy{

	private GRAVEAfterSimulation graveAfterSimulation;

	private EvoAfterSimulation evoAfterSimulation;

	public EvoGRAVEAfterSimulation(GRAVEAfterSimulation graveAfterSimulation, EvoAfterSimulation evoAfterSimulation) {

		this.graveAfterSimulation = graveAfterSimulation;

		this.evoAfterSimulation = evoAfterSimulation;

	}

	@Override
	public void afterSimulationActions(int[] goals){

		this.graveAfterSimulation.afterSimulationActions(goals);

		this.evoAfterSimulation.afterSimulationActions(goals);

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

package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class EvoAfterSimulation implements AfterSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	private InternalPropnetRole myRole;

	public EvoAfterSimulation(SingleParameterEvolutionManager evolutionManager, InternalPropnetRole myRole) {

		this.evolutionManager = evolutionManager;

		this.myRole = myRole;

	}

	@Override
	public void afterSimulationActions(int[] goals) {

		this.evolutionManager.updateFitness(goals[this.myRole.getIndex()]);

	}

	@Override
	public String getStrategyParameters() {

			return this.evolutionManager.printEvolutionManager();

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

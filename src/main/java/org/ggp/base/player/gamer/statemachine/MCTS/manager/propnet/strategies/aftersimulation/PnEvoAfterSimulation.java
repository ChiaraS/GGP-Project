package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class PnEvoAfterSimulation implements PnAfterSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	private InternalPropnetRole myRole;

	public PnEvoAfterSimulation(SingleParameterEvolutionManager evolutionManager, InternalPropnetRole myRole) {

		this.evolutionManager = evolutionManager;

		this.myRole = myRole;

	}

	@Override
	public void afterSimulationActions(PnSimulationResult simulationResult) {

		this.evolutionManager.updateFitness(simulationResult.getTerminalGoals()[this.myRole.getIndex()]);

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

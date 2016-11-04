package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

public class PnEvoAfterSimulation implements PnAfterSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	private CompactRole myRole;

	public PnEvoAfterSimulation(SingleParameterEvolutionManager evolutionManager, CompactRole myRole) {

		this.evolutionManager = evolutionManager;

		this.myRole = myRole;

	}

	@Override
	public void afterSimulationActions(PnSimulationResult simulationResult) {

		int[] goals;

		// We have to check if the EvolutionManager is evolving parameters only for the playing role
		// or for all roles and update the fitness with appropriate goals.
		if(this.evolutionManager.getNumPopulations() == 1){

			goals = new int[1];
			goals[0] = simulationResult.getTerminalGoals()[this.myRole.getIndex()];

		}else{

			goals = simulationResult.getTerminalGoals();

		}

		this.evolutionManager.updateFitness(goals);

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

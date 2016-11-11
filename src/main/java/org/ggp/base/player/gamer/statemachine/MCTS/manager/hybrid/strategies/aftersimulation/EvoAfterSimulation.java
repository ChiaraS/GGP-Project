package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class EvoAfterSimulation extends AfterSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	public EvoAfterSimulation(GameDependentParameters gameDependentParameters, SingleParameterEvolutionManager evolutionManager){

		super(gameDependentParameters);

		this.evolutionManager = evolutionManager;

	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult) {

		int[] goals;

		// We have to check if the EvolutionManager is evolving parameters only for the playing role
		// or for all roles and update the fitness with appropriate goals.
		if(this.evolutionManager.getNumPopulations() == 1){

			goals = new int[1];
			goals[0] = simulationResult.getTerminalGoals()[this.gameDependentParameters.getMyRoleIndex()];

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
